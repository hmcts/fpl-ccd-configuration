package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.OrderAction;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Recital;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisCaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisChild;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisDirection;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisHearingBooking;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisJudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisRecital;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisRepresentative;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisRepresentedBy;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisRespondent;
import uk.gov.hmcts.reform.fpl.service.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.CASE_MANAGEMENT_ORDER_LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.ORDER_ACTION;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseDetails;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.document;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ActiveProfiles("integration-test")
@WebMvcTest(ActionCaseManagementOrderController.class)
@OverrideAutoConfiguration(enabled = true)
public class ActionCaseManagementOrderControllerMidEventTest extends AbstractControllerTest {
    private static final byte[] PDF = {1, 2, 3, 4, 5};

    @MockBean
    private UploadDocumentService uploadService;

    @MockBean
    private DocmosisDocumentGeneratorService generatorService;

    @Captor
    private ArgumentCaptor<DocmosisCaseManagementOrder> captor;

    ActionCaseManagementOrderControllerMidEventTest() {
        super("action-cmo");
    }

    @Test
    void shouldPostExpectedTemplateDataAndAddDocumentReferenceToOrderActionWhenGeneratingCaseManagementOrder() {
        Document document = document();
        DocmosisDocument docmosisDocument = new DocmosisDocument("case-management-order.pdf", PDF);

        given(generatorService.generateDocmosisDocument(captor.capture(), any())).willReturn(docmosisDocument);
        given(uploadService.uploadPDF(any(), any())).willReturn(document);

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(getCaseDetails());

        verify(uploadService).uploadPDF(PDF, "draft-case-management-order.pdf");

        //need to pass in captor for draft image string
        assertThat(captor.getValue()).isEqualToComparingFieldByField(expectedTemplateData(captor.getValue()));

        assertThat(getDocumentReference(callbackResponse)).isEqualTo(
            DocumentReference.builder()
                .binaryUrl(document.links.binary.href)
                .filename(document.originalDocumentName)
                .url(document.links.self.href)
                .build());
    }

    private CaseDetails getCaseDetails() {
        CaseDetails caseDetails = populatedCaseDetails();

        caseDetails.getData().put("dateOfIssue", LocalDate.now());

        caseDetails.getData().put("recitals", wrapElements(Recital.builder()
            .title("example recital")
            .description("description")
            .build()));

        caseDetails.getData().put("allPartiesCustomCMO", wrapElements(Direction.builder()
            .directionType("Example title")
            .directionText("Example text")
            .dateToBeCompletedBy(LocalDateTime.of(2099, 1, 1, 10, 0, 0))
            .build()));

        caseDetails.getData().put(CASE_MANAGEMENT_ORDER_LOCAL_AUTHORITY.getKey(), CaseManagementOrder.builder()
                .id(UUID.fromString("51d02c7f-2a51-424b-b299-a90b98bb1774"))
                .build());

        return caseDetails;
    }

    private DocmosisCaseManagementOrder expectedTemplateData(DocmosisCaseManagementOrder order) {

        return DocmosisCaseManagementOrder.builder()
            .familyManCaseNumber("12345")
            .courtName("Family Court")
            .judgeAndLegalAdvisor(expectedJudgeAndLegalAdvisor())
            .dateOfIssue(formatLocalDateToString(LocalDate.now(), FormatStyle.LONG))
            .complianceDeadline("18 September 2020")
            .representatives(expectedRepresentatives())
            .respondents(expectedRespondents())
            .respondentsProvided(true)
            .children(expectedChildren())
            .applicantName("London Borough of Southwark")
            .hearingBooking(expectedHearing())
            .draftbackground(order.getDraftbackground())
            .recitals(expectedRecitals())
            .recitalsProvided(true)
            .directions(expectedDirections())
            .build();
    }

    private List<DocmosisDirection> expectedDirections() {
        return List.of(DocmosisDirection.builder()
            .assignee(ALL_PARTIES)
            .title("2. Example title by 10:00am, 1 January 2099")
            .body("Example text")
            .build());
    }

    private List<DocmosisRecital> expectedRecitals() {
        return List.of(DocmosisRecital.builder()
            .title("example recital")
            .body("description")
            .build());
    }

    private DocmosisHearingBooking expectedHearing() {
        String willAppearOnIssuedCMO = "This will appear on the issued CMO";

        return DocmosisHearingBooking.builder()
            .hearingDate(willAppearOnIssuedCMO)
            .hearingVenue(willAppearOnIssuedCMO)
            .preHearingAttendance(willAppearOnIssuedCMO)
            .hearingTime(willAppearOnIssuedCMO)
            .build();
    }

    private List<DocmosisChild> expectedChildren() {
        return List.of(
            DocmosisChild.builder()
                .name("Tom Reeves")
                .gender("Boy")
                .dateOfBirth("15 June 2018")
                .build(),
            DocmosisChild.builder()
                .name("Sarah Reeves")
                .gender("Girl")
                .dateOfBirth("2 February 2002")
                .build()
        );
    }

    private List<DocmosisRespondent> expectedRespondents() {
        return List.of(
            DocmosisRespondent.builder()
                .name("Paul Smith")
                .relationshipToChild("Uncle")
                .build(),
            DocmosisRespondent.builder()
                .name("James Smith")
                .relationshipToChild("Brother")
                .build(),
            DocmosisRespondent.builder()
                .name("An Other")
                .relationshipToChild("Cousin")
                .build());
    }

    private DocmosisJudgeAndLegalAdvisor expectedJudgeAndLegalAdvisor() {
        return DocmosisJudgeAndLegalAdvisor.builder()
            .judgeTitleAndName("His Honour Judge Walker")
            .legalAdvisorName("John Smith")
            .build();
    }

    private List<DocmosisRepresentative> expectedRepresentatives() {
        return List.of(DocmosisRepresentative.builder()
            .name("London Borough of Southwark")
            .representedBy(List.of(DocmosisRepresentedBy.builder()
                .name("Brian Banks")
                .email("brian@banks.com")
                .phoneNumber("020 2772 5772")
                .build()))
            .build());
    }

    private DocumentReference getDocumentReference(AboutToStartOrSubmitCallbackResponse callbackResponse) {
        Map<String, Object> responseCaseData = callbackResponse.getData();

        return mapper.convertValue(responseCaseData.get(ORDER_ACTION.getKey()), OrderAction.class).getDocument();
    }
}
