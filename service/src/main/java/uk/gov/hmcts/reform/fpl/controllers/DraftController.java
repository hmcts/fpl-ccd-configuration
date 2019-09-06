package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.swagger.annotations.Api;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
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
import uk.gov.hmcts.reform.fpl.model.Order;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.DocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.springframework.util.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.utils.SubmittedFormFilenameHelper.buildFileName;

@Api
@RestController
@RequestMapping("/callback/draft-SDO")
public class DraftController {

    private final ObjectMapper mapper;


    private final DocumentGeneratorService documentGeneratorService;
    private final UploadDocumentService uploadDocumentService;

    @Autowired
    public DraftController(ObjectMapper mapper,
                           DocumentGeneratorService documentGeneratorService,
                           UploadDocumentService uploadDocumentService) {
        this.mapper = mapper;
        this.documentGeneratorService = documentGeneratorService;
        this.uploadDocumentService = uploadDocumentService;
    }


    @PostMapping("/about-to-start")
    @SuppressWarnings("unchecked")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackrequest) throws IOException {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        //pre populate standard directions
        if (caseData.getStandardDirectionOrder() == null) {
            List<Element<Direction>> directions = ImmutableList.of(
                Element.<Direction>builder()
                    .id(UUID.randomUUID())
                    .value(Direction.builder()
                        .type("Arrange an advocates' meeting")
                        .text("Hardcoded directions\n • First document \n • Second document")
                        .assignee("cafcassDirections")
                        .readOnly("No")
                        .build())
                    .build(),
                Element.<Direction>builder()
                    .id(UUID.randomUUID())
                    .value(Direction.builder()
                        .type("Mandatory order title")
                        .text("Not editable direction")
                        .assignee("allParties")
                        .readOnly("Yes")
                        .build())
                    .build()
            );

            caseDetails.getData().put("standardDirectionOrder", Order.builder().directions(directions).build());
        } else {

//            // need to repopulate readOnly data
            List<Map<String, Object>> directions = (List<Map<String, Object>>) ((Map<String, Object>) caseDetails.getData().get("standardDirectionOrder")).get("directions");
            for (Map<String, Object> direction : directions) {
                Map<String, Object> value = (Map<String, Object>) direction.get("value");

                String type = (String) value.get("type");

                if (type.equals("Mandatory order title")) {
                    value.put("readOnly", "Yes");
                } else {
                    value.put("readOnly", "No");
                }
            }
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse midEvent(
        @RequestHeader(value = "authorization") String authorization,
        @RequestHeader(value = "user-id") String userId,
        @RequestBody CallbackRequest callbackrequest) throws IOException {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);


        for (Element<Direction> directionElement : caseData.getStandardDirectionOrder().getDirections()) {
            Direction direction = directionElement.getValue();
            if (direction.getText() == null) {
                direction.setText("Hardcoded hidden value");
            }
        }


        // sort directions by role
        Map<String, List<Element<Direction>>> directionsByRole = caseData.getStandardDirectionOrder().getDirections().stream()
            .collect(groupingBy(direction -> direction.getValue().getAssignee()));
//
        // add directions into their respective pre defined collections
        caseDetails.getData().putAll(directionsByRole);
//
        DocmosisDocument docmosisDocument = documentGeneratorService.generateDocmosisDocument(preparePlaceholders(caseData), DocmosisTemplates.SDO);
        byte[] bytes = docmosisDocument.getBytes();

        Document document = uploadDocumentService.uploadPDF(userId, authorization, bytes, "Draft.pdf");

        Map<String, Object> data = caseDetails.getData();

        data.put("sdo", ImmutableMap.<String, String>builder()
            .put("document_url", document.links.self.href)
            .put("document_binary_url", document.links.binary.href)
            .put("document_filename", "Draft.pdf")
            .build());


        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(
        @RequestHeader(value = "authorization") String authorization,
        @RequestHeader(value = "user-id") String userId,
        @RequestBody CallbackRequest callbackrequest) throws IOException {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);


        for (Element<Direction> directionElement : caseData.getStandardDirectionOrder().getDirections()) {
            Direction direction = directionElement.getValue();
            if (direction.getText() == null) {
                direction.setText("Hardcoded hidden value");
            }
        }

        // sort directions by role
        Map<String, List<Element<Direction>>> directionsByRole = caseData.getStandardDirectionOrder().getDirections().stream()
            .collect(groupingBy(direction -> direction.getValue().getAssignee()));
//
        // add directions into their respective pre defined collections
        caseDetails.getData().putAll(directionsByRole);
//
//        // populate CMOs collection of collections
//        if (isEmpty(caseData.getCmoCollection())) {
//            // when empty, add the first thing
//            caseDetails.getData().put("cmoCollection", ImmutableList.of(Element.builder()
//                .value(caseData.getOrder())
//                .build()));
//        } else {
//            // when second CMO, add to list
//            caseData.getCmoCollection().add(0, Element.<Order>builder()
//                .id(UUID.randomUUID())
//                .value(caseData.getOrder())
//                .build());
//
//            caseDetails.getData().put("cmoCollection", caseData.getCmoCollection());
//        }
//
//        // remove old CMO
//        caseDetails.getData().remove("cmo");

        DocmosisDocument docmosisDocument = documentGeneratorService.generateDocmosisDocument(preparePlaceholders(caseData), DocmosisTemplates.SDO);
        byte[] bytes = docmosisDocument.getBytes();

        Document document = uploadDocumentService.uploadPDF(userId, authorization, bytes, "Draft.pdf");

        Map<String, Object> data = caseDetails.getData();

        data.put("sdo", ImmutableMap.<String, String>builder()
            .put("document_url", document.links.self.href)
            .put("document_binary_url", document.links.binary.href)
            .put("document_filename", "Draft.pdf")
            .build());


        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    private Map<String, Object> preparePlaceholders(CaseData caseData) {
        HearingBooking hearingBooking = caseData.getHearingDetails() != null && caseData.getHearingDetails().get(0) != null ? caseData.getHearingDetails().get(0).getValue() :
            HearingBooking.builder().build();

        return Map.of(
            "familyManCaseId", StringUtils.defaultIfEmpty(caseData.getFamilyManCaseNumber(), "DEFAULT"),
            "generationDateStr", StringUtils.defaultIfEmpty(LocalDate.now().toString(), "DEFAULT"),
            "hearingVenue", StringUtils.defaultIfEmpty(hearingBooking.getVenue(), "DEFAULT"),
            "hearingDate", StringUtils.defaultIfEmpty(hearingBooking.getDate() != null ? hearingBooking.getDate().toString() : "DEFAULT", "DEFAULT"),
            "preHearingAttendance", StringUtils.defaultIfEmpty(hearingBooking.getPreHearingAttendance(), "DEFAULT"),
            "hearingTime", StringUtils.defaultIfEmpty(hearingBooking.getTime(), "DEFAULT"),
            "complianceDeadline", StringUtils.defaultIfEmpty(caseData.getDateSubmitted().plusWeeks(26).toString(), "DEFAULT"), //REFACTOR !!!
            "children", prepareChildren(caseData), //REFACTOR !!!,
            "directions", prepareDirections(caseData)
        );
    }

    private List<Map<String, String>> prepareDirections(CaseData caseData) {
        return caseData.getStandardDirectionOrder().getDirections()
            .stream()
            .map(Element::getValue)
            .map(direction -> Map.of(
                "title", direction.getType() + " comply by: " + (direction.getCompleteBy() != null ? direction.getCompleteBy().toString() : " unknown"),
                "body", direction.getText()
            )).collect(toList());
    }

    private List<Map<String, String>> prepareChildren(CaseData caseData) {
        return caseData.getAllChildren()
            .stream()
            .map(Element::getValue)
            .map(Child::getParty)
            .map(child -> Map.of(
                "name", child.getFirstName() + " " + child.getLastName(),
                "gender", child.getGender(),
                "dateOfBirth", child.getDateOfBirth().toString()
            ))
            .collect(toList());
    }
}
