package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
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
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Order;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.configuration.OrderDefinition;
import uk.gov.hmcts.reform.fpl.service.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.OrdersLookupService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Api
@RestController
@RequestMapping("/callback/draft-SDO")
public class DraftController {
    private final ObjectMapper mapper;
    private final DocmosisDocumentGeneratorService documentGeneratorService;
    private final UploadDocumentService uploadDocumentService;
    private final OrdersLookupService ordersLookupService;


    @Autowired

    public DraftController(ObjectMapper mapper,
                           DocmosisDocumentGeneratorService documentGeneratorService,
                           UploadDocumentService uploadDocumentService,
                           OrdersLookupService ordersLookupService) {
        this.mapper = mapper;
        this.documentGeneratorService = documentGeneratorService;
        this.uploadDocumentService = uploadDocumentService;
        this.ordersLookupService = ordersLookupService;
    }

    private String booleanToYesOrNo(boolean value) {
        return value ? "Yes" : "No";
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

        DocmosisDocument docmosisDocument =
            documentGeneratorService.generateDocmosisDocument(preparePlaceholders(caseData), DocmosisTemplates.SDO);
        byte[] bytes = docmosisDocument.getBytes();

        Document document = uploadDocumentService.uploadPDF(userId, authorization, bytes, "Draft.pdf");

        Map<String, Object> data = caseDetails.getData();

        data.put("sdo", ImmutableMap.<String, String>builder()
            .put("document_url", document.links.self.href)
            .put("document_binary_url", document.links.binary.href)
            .put("document_filename", "Draft.pdf")
            .build());

        System.out.println("caseDetails = " + caseDetails.getData());

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

    @SuppressWarnings("LineLength")
    private Map<String, Object> preparePlaceholders(CaseData caseData) {
        HearingBooking hearingBooking =
            caseData.getHearingDetails() != null && caseData.getHearingDetails().get(0) != null ? caseData.getHearingDetails().get(0).getValue() : HearingBooking.builder().build();

        return Map.of(
            "familyManCaseId", defaultIfEmpty(caseData.getFamilyManCaseNumber(), "DEFAULT"),
            "generationDateStr", defaultIfEmpty(LocalDate.now().toString(), "DEFAULT"),
            "hearingVenue", defaultIfEmpty(hearingBooking.getVenue(), "DEFAULT"),
            "hearingDate", defaultIfEmpty(hearingBooking.getDate() != null ? hearingBooking.getDate().toString() : "DEFAULT", "DEFAULT"),
            "preHearingAttendance", defaultIfEmpty(hearingBooking.getPreHearingAttendance(), "DEFAULT"),
            "hearingTime", defaultIfEmpty(hearingBooking.getTime(), "DEFAULT"),
            "complianceDeadline", defaultIfEmpty(caseData.getDateSubmitted().plusWeeks(26).toString(), "DEFAULT"), //REFACTOR !!!
            "children", prepareChildren(caseData), //REFACTOR !!!,
            "directions", prepareDirections(caseData)
        );
    }

    @SuppressWarnings("LineLength")
    private List<Map<String, String>> prepareDirections(CaseData caseData) {
        return caseData.getStandardDirectionOrder().getDirections()
            .stream()
            .map(Element::getValue)
            .map(direction -> Map.of(
                "title", direction.getType() + " comply by: " + (direction.getCompleteBy() != null ? formatDate(direction) : " unknown"),
                "body", direction.getText()))
            .collect(toList());
    }

    private String formatDate(@NotNull @Valid Direction direction) {
        return direction.getCompleteBy().format(DateTimeFormatter.ofPattern("h:mma, d MMMM yyyy"))
            .replace("AM", "am")
            .replace("PM", "pm");
    }

    private List<Map<String, String>> prepareChildren(CaseData caseData) {
        return caseData.getAllChildren()
            .stream()
            .map(Element::getValue)
            .map(Child::getParty)
            .map(child -> Map.of(
                "name", child.getFirstName() + " " + child.getLastName(),
                "gender", defaultIfEmpty(child.getGender(), "unknown"),
                "dateOfBirth", child.getDateOfBirth().toString()))
            .collect(toList());
    }
}
