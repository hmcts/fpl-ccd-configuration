package uk.gov.hmcts.reform.fpl.controllers.gatekeepingorder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.controllers.AddGatekeepingOrderController;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.GatekeepingOrderSealDecision;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.StandardDirection;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisData;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisHearingBooking;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisJudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisStandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.event.GatekeepingOrderEventData;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisDocumentGeneratorService;

import java.time.LocalDateTime;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.CAFCASS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.DirectionDueDateType.DAYS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionType.APPOINT_CHILDREN_GUARDIAN;
import static uk.gov.hmcts.reform.fpl.enums.DirectionType.SEND_CASE_SUMMARY;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.SDO;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HER_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.GatekeepingOrderRoute.SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.GatekeepingOrderRoute.UPLOAD;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.GatekeepingOrderRoute.URGENT;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocument;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentBinaries;

@WebMvcTest(AddGatekeepingOrderController.class)
@OverrideAutoConfiguration(enabled = true)
class AddGatekeepingOrderControllerPrepareDecisionMidEventTest extends AbstractCallbackTest {

    private static final String CALLBACK_NAME = "prepare-decision";
    private static final Document DOCUMENT = testDocument();
    private static final DocumentReference DOCUMENT_REFERENCE = DocumentReference.buildFromDocument(DOCUMENT);
    private static final String NEXT_STEPS = "## Next steps\n\n"
        + "Your order will be saved as a draft in 'Draft orders'.\n\n"
        + "You cannot seal and send the order until adding:\n\n";

    @MockBean
    private DocmosisDocumentGeneratorService documentGeneratorService;

    @MockBean
    private UploadDocumentService uploadDocumentService;

    AddGatekeepingOrderControllerPrepareDecisionMidEventTest() {
        super("add-gatekeeping-order");
    }

    @Nested
    class ServiceRoute {

        @BeforeEach
        void setup() {
            final byte[] generatedDocumentBinaries = testDocumentBinaries();
            final String draftOrderFileName = "draft-standard-directions-order.pdf";
            final String baseFileName = "standard-directions-order.pdf";

            given(documentGeneratorService.generateDocmosisDocument(any(DocmosisData.class), any()))
                .willReturn(new DocmosisDocument(baseFileName, generatedDocumentBinaries));

            given(uploadDocumentService.uploadPDF(generatedDocumentBinaries, draftOrderFileName))
                .willReturn(DOCUMENT);
        }

        @Test
        void shouldSetDraftDocumentStandardDirectionsAndNextStepsLabelWhenMandatoryInformationMissing() {
            final String nextSteps = NEXT_STEPS
                + "* the first hearing details\n\n"
                + "* the allocated judge\n\n"
                + "* the judge issuing the order";

            final StandardDirection localAuthorityStandardDirection = StandardDirection.builder()
                .type(SEND_CASE_SUMMARY)
                .title("Send case summary to all parties")
                .description("Send to the court and all parties.")
                .assignee(LOCAL_AUTHORITY)
                .dueDateType(DAYS)
                .daysBeforeHearing(2)
                .build();

            final StandardDirection cafcassStandardDirection = StandardDirection.builder()
                .type(APPOINT_CHILDREN_GUARDIAN)
                .title("Appoint a children's guardian")
                .description("Custom direction")
                .assignee(CAFCASS)
                .dueDateType(DAYS)
                .daysBeforeHearing(2)
                .build();

            final CaseDetails caseDetails = CaseDetails.builder()
                .id(1234567890123456L)
                .data(Map.of(
                    "gatekeepingOrderRouter", SERVICE,
                    "caseLocalAuthority", LOCAL_AUTHORITY_1_CODE,
                    "dateSubmitted", dateNow(),
                    "applicants", getApplicant("Legacy applicant name"),
                    "directionsForLocalAuthority", List.of(localAuthorityStandardDirection.getType()),
                    "directionsForCafcass", List.of(cafcassStandardDirection.getType()),
                    "direction-SEND_CASE_SUMMARY", localAuthorityStandardDirection,
                    "direction-APPOINT_CHILDREN_GUARDIAN", cafcassStandardDirection))
                .build();

            final GatekeepingOrderSealDecision expectedSealDecision = GatekeepingOrderSealDecision.builder()
                .draftDocument(DOCUMENT_REFERENCE)
                .dateOfIssue(dateNow())
                .nextSteps(nextSteps)
                .build();

            final CaseData responseData = extractCaseData(postMidEvent(caseDetails, CALLBACK_NAME));
            final GatekeepingOrderEventData eventData = responseData.getGatekeepingOrderEventData();

            assertThat(eventData.getGatekeepingOrderSealDecision())
                .isEqualTo(expectedSealDecision);

            assertThat(eventData.getStandardDirections())
                .extracting(Element::getValue)
                .contains(localAuthorityStandardDirection, cafcassStandardDirection);
        }

        @Test
        void shouldGenerateDraftDocumentWithLocalAuthorityApplicantWhenOrderCanBeSealed() {
            CaseData caseData = buildBaseCaseData().toBuilder()
                .gatekeepingOrderRouter(SERVICE)
                .allocatedJudge(allocatedJudge())
                .gatekeepingOrderEventData(GatekeepingOrderEventData.builder()
                    .gatekeepingOrderIssuingJudge(issuingJudge())
                    .build())
                .hearingDetails(hearingBookings())
                .localAuthorities(getLocalAuthority("Local authority name"))
                .applicants(getApplicant("Legacy applicant name"))
                .build();

            GatekeepingOrderSealDecision expectedSealDecision = GatekeepingOrderSealDecision.builder()
                .draftDocument(DOCUMENT_REFERENCE)
                .dateOfIssue(dateNow())
                .nextSteps(null)
                .build();

            DocmosisStandardDirectionOrder expectedDocumentCustomization = expectedDocumentCustomization().toBuilder()
                .applicantName("Local authority name")
                .build();

            CaseData responseData = extractCaseData(postMidEvent(caseData, CALLBACK_NAME));

            assertThat(responseData.getGatekeepingOrderEventData().getGatekeepingOrderSealDecision())
                .isEqualTo(expectedSealDecision);

            verify(documentGeneratorService).generateDocmosisDocument(expectedDocumentCustomization, SDO);
        }

        @Test
        void shouldGenerateDraftDocumentWithLegacyApplicantWhenOrderCanBeSealed() {
            CaseData caseData = buildBaseCaseData().toBuilder()
                .gatekeepingOrderRouter(SERVICE)
                .allocatedJudge(allocatedJudge())
                .gatekeepingOrderEventData(GatekeepingOrderEventData.builder()
                    .gatekeepingOrderIssuingJudge(issuingJudge())
                    .build())
                .hearingDetails(hearingBookings())
                .localAuthorities(null)
                .applicants(getApplicant("Legacy applicant name"))
                .build();

            GatekeepingOrderSealDecision expectedSealDecision = GatekeepingOrderSealDecision.builder()
                .draftDocument(DOCUMENT_REFERENCE)
                .dateOfIssue(dateNow())
                .nextSteps(null)
                .build();

            DocmosisStandardDirectionOrder expectedDocumentCustomization = expectedDocumentCustomization().toBuilder()
                .applicantName("Legacy applicant name")
                .build();

            CaseData responseData = extractCaseData(postMidEvent(caseData, CALLBACK_NAME));

            assertThat(responseData.getGatekeepingOrderEventData().getGatekeepingOrderSealDecision())
                .isEqualTo(expectedSealDecision);

            verify(documentGeneratorService).generateDocmosisDocument(expectedDocumentCustomization, SDO);
        }
    }

    @Nested
    class UploadRoute {

        @Test
        void shouldSetDraftDocumentStandardDirectionsAndNextStepsLabelWhenMandatoryInformationMissing() {
            final String nextSteps = NEXT_STEPS
                + "* the first hearing details\n\n"
                + "* the allocated judge\n\n"
                + "* the judge issuing the order";

            final CaseData caseDetails = CaseData.builder()
                .gatekeepingOrderRouter(UPLOAD)
                .preparedSDO(DOCUMENT_REFERENCE)
                .build();

            final GatekeepingOrderSealDecision expectedSealDecision = GatekeepingOrderSealDecision.builder()
                .draftDocument(DOCUMENT_REFERENCE)
                .dateOfIssue(dateNow())
                .nextSteps(nextSteps)
                .build();

            final CaseData responseData = extractCaseData(postMidEvent(caseDetails, CALLBACK_NAME));
            final GatekeepingOrderEventData eventData = responseData.getGatekeepingOrderEventData();

            assertThat(eventData.getGatekeepingOrderSealDecision()).isEqualTo(expectedSealDecision);
        }

        @Test
        void shouldSetOnlyDraftDocumentWhenOrderCanBeSealed() {
            final CaseData caseData = buildBaseCaseData().toBuilder()
                .gatekeepingOrderRouter(UPLOAD)
                .preparedSDO(DOCUMENT_REFERENCE)
                .allocatedJudge(allocatedJudge())
                .gatekeepingOrderEventData(GatekeepingOrderEventData.builder()
                    .gatekeepingOrderIssuingJudge(issuingJudge())
                    .build())
                .hearingDetails(hearingBookings())
                .build();

            final GatekeepingOrderSealDecision expectedSealDecision = GatekeepingOrderSealDecision.builder()
                .draftDocument(DOCUMENT_REFERENCE)
                .dateOfIssue(dateNow())
                .build();

            final CaseData responseData = extractCaseData(postMidEvent(caseData, CALLBACK_NAME));
            final GatekeepingOrderEventData eventData = responseData.getGatekeepingOrderEventData();

            assertThat(eventData.getGatekeepingOrderSealDecision()).isEqualTo(expectedSealDecision);
        }
    }

    @Nested
    class UrgentRoute {
        @Test
        void shouldThrowExceptionWhenInvokedForInvalidRoute() {
            final CaseData caseData = CaseData.builder()
                .gatekeepingOrderRouter(URGENT)
                .build();

            assertThatThrownBy(() -> postMidEvent(caseData, CALLBACK_NAME))
                .hasMessageContaining("The prepare-decision callback does not support urgent route");
        }
    }

    private CaseData buildBaseCaseData() {
        return CaseData.builder()
            .caseLocalAuthority(LOCAL_AUTHORITY_1_CODE)
            .id(1234123412341234L)
            .dateSubmitted(dateNow())
            .localAuthorities(getLocalAuthority(""))
            .applicants(getApplicant(""))
            .build();
    }

    private List<Element<Applicant>> getApplicant(String name) {
        return wrapElements(Applicant.builder()
            .party(ApplicantParty.builder()
                .organisationName(name)
                .build())
            .build());
    }

    private List<Element<LocalAuthority>> getLocalAuthority(String name) {
        return wrapElements(LocalAuthority.builder()
            .name(name)
            .build());
    }

    private List<Element<HearingBooking>> hearingBookings() {
        return wrapElements(HearingBooking.builder().startDate(LocalDateTime.now()).build());
    }

    private Judge allocatedJudge() {
        return Judge.builder()
            .judgeTitle(HIS_HONOUR_JUDGE)
            .judgeLastName("Davidson")
            .judgeEmailAddress("davidson@example.com")
            .build();
    }

    private JudgeAndLegalAdvisor issuingJudge() {
        return JudgeAndLegalAdvisor.builder()
            .judgeTitle(HER_HONOUR_JUDGE)
            .judgeLastName("Judy")
            .judgeEmailAddress("judy@example.com")
            .build();
    }

    private DocmosisStandardDirectionOrder expectedDocumentCustomization() {
        return DocmosisStandardDirectionOrder.builder()
            .ccdCaseNumber("1234-1234-1234-1234")
            .dateOfIssue("<date will be added on issue>")
            .draftbackground("[userImage:draft-watermark.png]")
            .crest("[userImage:crest.png]")
            .complianceDeadline(formatLocalDateToString(dateNow().plusWeeks(26), FormatStyle.LONG))
            .judgeAndLegalAdvisor(DocmosisJudgeAndLegalAdvisor.builder()
                .judgeTitleAndName("Her Honour Judge Judy")
                .legalAdvisorName("")
                .build())
            .children(emptyList())
            .directions(emptyList())
            .respondents(emptyList())
            .applicantName("x")
            .hearingBooking(DocmosisHearingBooking.builder()
                .build())
            .courtName("Family Court")
            .build();
    }
}
