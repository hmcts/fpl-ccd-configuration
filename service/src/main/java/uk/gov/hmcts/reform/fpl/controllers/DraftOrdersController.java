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
import uk.gov.hmcts.reform.fpl.enums.DirectionAssignee;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.Order;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.CaseDataExtractionService;
import uk.gov.hmcts.reform.fpl.service.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Objects.isNull;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.CAFCASS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.COURT;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.OTHERS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.PARENTS_AND_RESPONDENTS;

@Api
@RestController
@RequestMapping("/callback/draft-SDO")
public class DraftOrdersController {
    private final ObjectMapper mapper;
    private final DocmosisDocumentGeneratorService documentGeneratorService;
    private final UploadDocumentService uploadDocumentService;
    private final CaseDataExtractionService caseDataExtractionService;

    @Autowired
    public DraftOrdersController(ObjectMapper mapper,
                                 DocmosisDocumentGeneratorService documentGeneratorService,
                                 UploadDocumentService uploadDocumentService,
                                 CaseDataExtractionService caseDataExtractionService) {
        this.mapper = mapper;
        this.documentGeneratorService = documentGeneratorService;
        this.uploadDocumentService = uploadDocumentService;
        this.caseDataExtractionService = caseDataExtractionService;
    }

    //TODO: prepopulate dates for orders 5 and 9 as hearing date
    //TODO: prepopulate dates for orders 11 and 15 as hearing date -2

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        if (!isNull(caseData.getStandardDirectionOrder())) {
            Map<String, List<Element<Direction>>> directions = caseData.getStandardDirectionOrder().getDirections()
                .stream()
                .filter(x -> x.getValue().getCustom() == null)
                .collect(groupingBy(directionElement -> directionElement.getValue().getAssignee().getValue()));

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
        @RequestBody CallbackRequest callbackrequest) throws IOException {
        CaseDetails caseDetails = addDirectionsToOrder(callbackrequest.getCaseDetails());
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

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

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetailsBefore = addDirectionsToOrder(callbackrequest.getCaseDetailsBefore());
        CaseData caseDataBefore = mapper.convertValue(caseDetailsBefore.getData(), CaseData.class);

        CaseDetails caseDetailsAfter = addDirectionsToOrder(callbackrequest.getCaseDetails());
        CaseData caseDataWithValuesRemoved = mapper.convertValue(caseDetailsAfter.getData(), CaseData.class);

        // persist read only, removable values and text
        caseDataWithValuesRemoved.getStandardDirectionOrder().getDirections()
            .forEach((directionWithValueRemoved) -> caseDataBefore.getStandardDirectionOrder().getDirections()
                .stream()
                .filter(direction -> direction.getId().equals(directionWithValueRemoved.getId()))
                .peek(direction -> {
                    directionWithValueRemoved.getValue().setReadOnly(direction.getValue().getReadOnly());
                    directionWithValueRemoved.getValue().setDirectionRemovable(
                        direction.getValue().getDirectionRemovable());
                })
                .filter(direction -> !direction.getValue().getReadOnly().equals("No"))
                .forEach(direction -> directionWithValueRemoved.getValue().setText(direction.getValue().getText())));

        caseDetailsAfter.getData().put("standardDirectionOrder", caseDataWithValuesRemoved.getStandardDirectionOrder());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetailsAfter.getData())
            .build();
    }

    //TODO: refactor this method. Makes me want to vomit
    private CaseDetails addDirectionsToOrder(CaseDetails caseDetails) {
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);
        List<Element<Direction>> directions = new ArrayList<>();

        directions.addAll(filterDirectionsNotRequired(caseData.getAllParties()));

        if (!isNull(caseData.getAllPartiesCustom())) {
            directions.addAll(assignCustomDirections(caseData.getAllPartiesCustom(), ALL_PARTIES));
        }

        directions.addAll(filterDirectionsNotRequired(caseData.getLocalAuthorityDirections()));

        if (!isNull(caseData.getLocalAuthorityDirectionsCustom())) {
            directions.addAll(assignCustomDirections(caseData.getLocalAuthorityDirectionsCustom(), LOCAL_AUTHORITY));
        }

        directions.addAll(filterDirectionsNotRequired(caseData.getParentsAndRespondentsDirections()));

        if (!isNull(caseData.getParentsAndRespondentsCustom())) {
            directions.addAll(
                assignCustomDirections(caseData.getParentsAndRespondentsCustom(), PARENTS_AND_RESPONDENTS));
        }

        directions.addAll(filterDirectionsNotRequired(caseData.getCafcassDirections()));

        if (!isNull(caseData.getCafcassDirectionsCustom())) {
            directions.addAll(assignCustomDirections(caseData.getCafcassDirectionsCustom(), CAFCASS));
        }

        directions.addAll(filterDirectionsNotRequired(caseData.getOtherPartiesDirections()));

        if (!isNull(caseData.getOtherPartiesDirectionsCustom())) {
            directions.addAll(assignCustomDirections(caseData.getCafcassDirectionsCustom(), OTHERS));
        }

        directions.addAll(filterDirectionsNotRequired(caseData.getCourtDirections()));

        if (!isNull(caseData.getAllPartiesCustom())) {
            directions.addAll(assignCustomDirections(caseData.getCourtDirections(), COURT));
        }

        caseDetails.getData().put("standardDirectionOrder", Order.builder().directions(directions).build());

        return caseDetails;
    }

    private List<Element<Direction>> assignCustomDirections(List<Element<Direction>> directions,
                                                            DirectionAssignee assignee) {
        return directions.stream()
            .map(element -> Element.<Direction>builder()
                .value(element.getValue().toBuilder()
                    .assignee(assignee)
                    .custom("Yes")
                    .build())
                .build())
            .collect(toList());
    }

    // TODO: what do we do with directions where a user has said it is not needed? Currently are removed. This is wrong.
    @SuppressWarnings("LineLength")
    private List<Element<Direction>> filterDirectionsNotRequired(List<Element<Direction>> directions) {
        return directions.stream()
            .filter(directionElement -> directionElement.getValue().getDirectionNeeded() == null || directionElement.getValue().getDirectionNeeded().equals("Yes"))
            .collect(toList());
    }
}
