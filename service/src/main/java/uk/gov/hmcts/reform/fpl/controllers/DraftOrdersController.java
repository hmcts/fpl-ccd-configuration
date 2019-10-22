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
import uk.gov.hmcts.reform.fpl.service.DirectionHelperService;
import uk.gov.hmcts.reform.fpl.service.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.OrdersLookupService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

@Api
@RestController
@RequestMapping("/callback/draft-standard-directions")
public class DraftOrdersController {
    private final ObjectMapper mapper;
    private final DocmosisDocumentGeneratorService docmosisService;
    private final UploadDocumentService uploadDocumentService;
    private final CaseDataExtractionService caseDataExtractionService;
    private final DirectionHelperService directionHelperService;
    private final OrdersLookupService ordersLookupService;

    @Autowired
    public DraftOrdersController(ObjectMapper mapper,
                                 DocmosisDocumentGeneratorService docmosisService,
                                 UploadDocumentService uploadDocumentService,
                                 CaseDataExtractionService caseDataExtractionService,
                                 DirectionHelperService directionHelperService,
                                 OrdersLookupService ordersLookupService) {
        this.mapper = mapper;
        this.docmosisService = docmosisService;
        this.uploadDocumentService = uploadDocumentService;
        this.caseDataExtractionService = caseDataExtractionService;
        this.directionHelperService = directionHelperService;
        this.ordersLookupService = ordersLookupService;
    }

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        if (!isNull(caseData.getStandardDirectionOrder())) {
            Map<String, List<Element<Direction>>> directions = directionHelperService.sortDirectionsByAssignee(
                caseData.getStandardDirectionOrder().getDirections());

            directions.forEach((key, value) -> caseDetails.getData().put(key, value));
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(
        @RequestHeader(value = "authorization") String authorization,
        @RequestHeader(value = "user-id") String userId,
        @RequestBody CallbackRequest callbackRequest) throws IOException {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        CaseData updated = caseData.toBuilder()
            .standardDirectionOrder(Order.builder()
                .directions(directionHelperService.combineAllDirections(caseData))
                .build())
            .build();

        directionHelperService.persistHiddenDirectionValues(
            getConfigDirectionsWithHiddenValues(), updated.getStandardDirectionOrder().getDirections());

        Document document = getDocument(
            authorization,
            userId,
            caseDataExtractionService.getStandardOrderDirectionData(updated)
        );

        Order.OrderBuilder orderBuilder = updated.getStandardDirectionOrder().toBuilder()
            .orderDoc(DocumentReference.builder()
                .url(document.links.self.href)
                .binaryUrl(document.links.binary.href)
                .filename("draft-standard-directions-order.pdf")
                .build());

        caseDetails.getData().put("standardDirectionOrder", orderBuilder.build());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(
        @RequestBody CallbackRequest callbackRequest) throws IOException {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        CaseData updated = caseData.toBuilder()
            .standardDirectionOrder(Order.builder()
                .directions(directionHelperService.combineAllDirections(caseData))
                .orderDoc(caseData.getStandardDirectionOrder().getOrderDoc())
                .build())
            .build();

        directionHelperService.persistHiddenDirectionValues(
            getConfigDirectionsWithHiddenValues(), updated.getStandardDirectionOrder().getDirections());

        caseDetails.getData().put("standardDirectionOrder", updated.getStandardDirectionOrder());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    private List<Element<Direction>> getConfigDirectionsWithHiddenValues() throws IOException {
        return ordersLookupService.getStandardDirectionOrder().getDirections()
            .stream()
            .map(direction -> directionHelperService.constructDirectionForCCD(direction, LocalDateTime.now()))
            .collect(Collectors.toList());
    }

    private Document getDocument(@RequestHeader("authorization") String authorization,
                                 @RequestHeader("user-id") String userId,
                                 Map<String, Object> templateData) {
        DocmosisDocument document = docmosisService.generateDocmosisDocument(templateData, DocmosisTemplates.SDO);

        return uploadDocumentService.uploadPDF(userId, authorization, document.getBytes(),
            "draft-standard-directions-order.pdf");
    }
}
