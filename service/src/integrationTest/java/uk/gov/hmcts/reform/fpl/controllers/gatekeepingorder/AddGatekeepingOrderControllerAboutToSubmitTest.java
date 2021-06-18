package uk.gov.hmcts.reform.fpl.controllers.gatekeepingorder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.controllers.AddGatekeepingOrderController;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CustomDirection;
import uk.gov.hmcts.reform.fpl.model.GatekeepingOrderSealDecision;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.model.StandardDirection;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisNoticeOfProceeding;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisStandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.event.GatekeepingOrderEventData;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.CAFCASS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionDueDateType.DATE;
import static uk.gov.hmcts.reform.fpl.enums.DirectionDueDateType.DAYS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionType.ATTEND_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.DirectionType.CUSTOM;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.SEALED;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.State.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocument;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentBinaries;

@WebMvcTest(AddGatekeepingOrderController.class)
@OverrideAutoConfiguration(enabled = true)
class AddGatekeepingOrderControllerAboutToSubmitTest extends AbstractCallbackTest {

    AddGatekeepingOrderControllerAboutToSubmitTest() {
        super("add-gatekeeping-order");
    }

    private static final Document SDO_DOCUMENT = testDocument();
    private static final Document C6_DOCUMENT = testDocument();
    private static final DocumentReference SDO_REFERENCE = DocumentReference.buildFromDocument(SDO_DOCUMENT);
    private static final DocumentReference C6_REFERENCE = DocumentReference.buildFromDocument(C6_DOCUMENT);

    @MockBean
    private DocmosisDocumentGeneratorService documentGeneratorService;

    @MockBean
    private UploadDocumentService uploadDocumentService;

    private final Time time = new FixedTimeConfiguration().fixedDateTime(LocalDateTime.of(2021, 3, 3, 0, 0, 0));

    @BeforeEach
    void setup() {
        final byte[] sdoBinaries = testDocumentBinaries();
        final byte[] c6Binaries = testDocumentBinaries();
        final String sealedOrderFileName = "standard-directions-order.pdf";
        final String c6FileName = "c6.pdf";

        given(documentGeneratorService.generateDocmosisDocument(any(DocmosisStandardDirectionOrder.class), any()))
            .willReturn(new DocmosisDocument(sealedOrderFileName, sdoBinaries));

        given(documentGeneratorService.generateDocmosisDocument(any(DocmosisNoticeOfProceeding.class), any()))
            .willReturn(new DocmosisDocument(c6FileName, c6Binaries));

        given(uploadDocumentService.uploadPDF(sdoBinaries, sealedOrderFileName)).willReturn(SDO_DOCUMENT);
        given(uploadDocumentService.uploadPDF(c6Binaries, c6FileName)).willReturn(C6_DOCUMENT);
    }

    @Test
    void shouldBuildDraftSDOWithExistingDraftDocumentWhenOrderStatusIsDraft() {
        CaseData caseData = CaseData.builder()
            .gatekeepingOrderEventData(GatekeepingOrderEventData.builder()
                .gatekeepingOrderSealDecision(GatekeepingOrderSealDecision.builder()
                    .orderStatus(DRAFT)
                    .draftDocument(SDO_REFERENCE)
                    .build())
                .build())

            .build();

        StandardDirectionOrder expectedSDO = StandardDirectionOrder.builder()
            .orderDoc(SDO_REFERENCE)
            .orderStatus(DRAFT)
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder().build())
            .standardDirections(emptyList())
            .build();

        CaseData responseData = extractCaseData(postAboutToSubmitEvent(caseData));

        assertThat(responseData.getStandardDirectionOrder()).isEqualTo(expectedSDO);
    }

    @Test
    void shouldBuildSealedSDOAndRemoveTransientFieldsWhenOrderStatusIsSealed() {
        final CustomDirection customDirection =
            CustomDirection.builder()
                .type(CUSTOM)
                .assignee(CAFCASS)
                .title("Test direction")
                .dueDateType(DAYS)
                .daysBeforeHearing(2)
                .build();

        final StandardDirection standardDirection =
            StandardDirection.builder()
                .type(ATTEND_HEARING)
                .assignee(ALL_PARTIES)
                .dueDateType(DATE)
                .title("Attend the pre-hearing and hearing")
                .description("Parties and their legal representatives must attend the pre-hearing and hearing")
                .dateToBeCompletedBy(LocalDateTime.of(2030, 1, 10, 12, 0, 0))
                .daysBeforeHearing(0)
                .build();

        final HearingBooking hearingBooking = HearingBooking.builder()
            .startDate(LocalDateTime.now())
            .endDate(LocalDateTime.now().plusDays(1))
            .venue("Venue").build();

        final GatekeepingOrderSealDecision gatekeepingOrderSealDecision = GatekeepingOrderSealDecision.builder()
            .orderStatus(SEALED)
            .dateOfIssue(time.now().toLocalDate())
            .draftDocument(SDO_REFERENCE)
            .build();

        CaseDetails caseData = CaseDetails.builder()
            .id(1234123412341234L)
            .data(Map.of(
                "caseLocalAuthority", LOCAL_AUTHORITY_1_CODE,
                "dateSubmitted", dateNow(),
                "applicants", getApplicant(),
                "hearingDetails", wrapElements(hearingBooking),
                "orders", Orders.builder().orderType(List.of(CARE_ORDER)).build(),
                "gatekeepingOrderIssuingJudge", JudgeAndLegalAdvisor.builder().build(),
                "gatekeepingOrderSealDecision", gatekeepingOrderSealDecision,
                "directionsForAllParties", List.of(ATTEND_HEARING),
                "direction-ATTEND_HEARING", standardDirection,
                "customDirections", wrapElements(customDirection)))
            .build();

        StandardDirectionOrder expectedSDO = StandardDirectionOrder.builder()
            .orderDoc(SDO_REFERENCE)
            .unsealedDocumentCopy(SDO_REFERENCE)
            .orderStatus(SEALED)
            .dateOfIssue("3 March 2021")
            .customDirections(wrapElements(customDirection))
            .standardDirections(wrapElements(standardDirection))
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder().build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseData);
        CaseData responseData = extractCaseData(response);

        assertThat(responseData.getStandardDirectionOrder()).isEqualTo(expectedSDO);
        assertThat(responseData.getState()).isEqualTo(CASE_MANAGEMENT);
        assertThat(responseData.getNoticeOfProceedingsBundle())
            .extracting(Element::getValue)
            .containsExactly(DocumentBundle.builder().document(C6_REFERENCE).build());
        assertThat(response.getData()).doesNotContainKeys("gatekeepingOrderRouter", "customDirections",
            "standardDirections", "gatekeepingOrderIssuingJudge", "gatekeepingOrderSealDecision");
    }

    private List<Element<Applicant>> getApplicant() {
        return wrapElements(Applicant.builder()
            .party(ApplicantParty.builder()
                .organisationName("")
                .build())
            .build());
    }
}
