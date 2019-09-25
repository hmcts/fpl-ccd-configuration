package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.Order;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.CaseDataExtractionService;
import uk.gov.hmcts.reform.fpl.model.configuration.OrderDefinition;
import uk.gov.hmcts.reform.fpl.service.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.OrdersLookupService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;

import java.util.List;
import java.util.Map;

import java.io.IOException;
import java.util.ArrayList;

import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.groupingBy;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Api
@RestController
@RequestMapping("/callback/draft-SDO")
public class DraftController {
    private final ObjectMapper mapper;
    private final DocmosisDocumentGeneratorService documentGeneratorService;
    private final UploadDocumentService uploadDocumentService;
    private final CaseDataExtractionService caseDataExtractionService;
    private final OrdersLookupService ordersLookupService;

    @Autowired
    public DraftController(ObjectMapper mapper,
                           DocmosisDocumentGeneratorService documentGeneratorService,
                           UploadDocumentService uploadDocumentService,
                           CaseDataExtractionService caseDataExtractionService,
                           OrdersLookupService ordersLookupService) {
        this.mapper = mapper;
        this.documentGeneratorService = documentGeneratorService;
        this.uploadDocumentService = uploadDocumentService;
        this.ordersLookupService = ordersLookupService;
        this.caseDataExtractionService = caseDataExtractionService;
    }

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(
        @RequestBody CallbackRequest callbackrequest) throws IOException {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        //pre populate all standard directions
        if (caseData.getAllParties() == null) {
            OrderDefinition standardDirectionOrder = ordersLookupService.getStandardDirectionOrder();

            Map<String, List<Element<Direction>>> directions = standardDirectionOrder.getDirections()
                .stream()
                .map(direction ->
                    Element.<Direction>builder()
                        .id(randomUUID())
                        .value(Direction.builder()
                            .type(direction.getTitle())
                            .text(direction.getText())
                            .assignee(direction.getAssignee())
                            .readOnly(booleanToYesOrNo(direction.getDisplay().isShowDateOnly()))
                            .build())
                        .build()
                )
                .collect(groupingBy(element -> element.getValue().getAssignee()));

            directions.forEach((key, value) -> caseDetails.getData().put(key, value));
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse midEvent(
        @RequestHeader(value = "authorization") String authorization,
        @RequestHeader(value = "user-id") String userId,
        @RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = addDirectionsToOrder(callbackrequest.getCaseDetails());
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        caseData.getStandardDirectionOrder().getDirections().forEach(direction -> {
            if (isBlank(direction.getValue().getText())) {
                direction.getValue().setText("Hardcoded hidden value");
            }
        });

        Map<String, Object> templateData = caseDataExtractionService
            .getDraftStandardOrderDirectionTemplateData(caseData);

        DocmosisDocument docmosisDocument =
            documentGeneratorService.generateDocmosisDocument(templateData, DocmosisTemplates.SDO);

        Document document = uploadDocumentService.uploadPDF(userId, authorization, docmosisDocument.getBytes(),
            "Draft.pdf");

        caseDetails.getData().put("sdo", DocumentReference.builder()
            .url(document.links.self.href)
            .binaryUrl(document.links.binary.href)
            .filename("Draft.pdf")
            .build());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    private CaseDetails addDirectionsToOrder(CaseDetails caseDetails) {
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        List<Element<Direction>> directions = new ArrayList<>();
        directions.addAll(addReadOnlyValuesToDirections(caseData.getAllParties()));
        directions.addAll(addReadOnlyValuesToDirections(caseData.getCourtDirections()));
        directions.addAll(addReadOnlyValuesToDirections(caseData.getLocalAuthorityDirections()));
        directions.addAll(addReadOnlyValuesToDirections(caseData.getCafcassDirections()));
        directions.addAll(addReadOnlyValuesToDirections(caseData.getOtherPartiesDirections()));
        directions.addAll(addReadOnlyValuesToDirections(caseData.getParentsAndRespondentsDirections()));

        caseDetails.getData().put("standardDirectionOrder", Order.builder().directions(directions).build());

        return caseDetails;
    }

    private List<Element<Direction>> addReadOnlyValuesToDirections(List<Element<Direction>> directions) {
        directions.forEach(direction -> {
            if (direction.getValue().getType().equals("Mandatory order title")) {
                direction.getValue().setReadOnly("Yes");
            } else {
                direction.getValue().setReadOnly("No");
            }
        });

        return directions;
    }

    private String booleanToYesOrNo(boolean value) {
        return value ? "Yes" : "No";
    }
}
