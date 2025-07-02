package uk.gov.hmcts.reform.fpl.controllers.gatekeepingorder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.controllers.AddGatekeepingOrderController;
import uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.GatekeepingOrderRoute;
import uk.gov.hmcts.reform.fpl.model.Allocation;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Court;
import uk.gov.hmcts.reform.fpl.model.CustomDirection;
import uk.gov.hmcts.reform.fpl.model.GatekeepingOrderSealDecision;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.model.StandardDirection;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisNoticeOfProceeding;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisStandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.event.GatekeepingOrderEventData;
import uk.gov.hmcts.reform.fpl.service.DocumentSealingService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Map.entry;
import static java.util.Map.ofEntries;
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
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.MAGISTRATES;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.SEALED;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.State.GATEKEEPING;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.GatekeepingOrderRoute.SERVICE;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocument;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentBinaries;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testHearing;

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
    private static final UserDetails USER = UserDetails.builder()
        .forename("John")
        .surname("Smith")
        .build();

    @MockBean
    private DocumentSealingService sealingService;

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
        givenCurrentUser(USER);
    }

    @Test
    void shouldPopulateAllocationDecisionWhenSubmitting() {

        Allocation allocationDecision = createAllocation("Lay justices", "Reason");
        CaseData caseData = CaseData.builder()
            .gatekeepingOrderRouter(SERVICE)
            .gatekeepingOrderEventData(GatekeepingOrderEventData.builder()
                .gatekeepingOrderSealDecision(GatekeepingOrderSealDecision.builder()
                    .orderStatus(DRAFT)
                    .draftDocument(SDO_REFERENCE)
                    .build())
                .build())
            .allocationDecision(allocationDecision)
            .build();

        CaseDetails caseDetails = asCaseDetails(caseData);

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(caseDetails);

        assertThat(callbackResponse.getData()).containsKey("allocationDecision");
    }

    @Test
    void shouldBuildDraftSDOWithExistingDraftDocumentWhenOrderStatusIsDraft() {

        Allocation allocationDecision = createAllocation("Lay justices", "Reason");
        CaseData caseData = CaseData.builder()
            .gatekeepingOrderRouter(SERVICE)
            .gatekeepingOrderEventData(GatekeepingOrderEventData.builder()
                .gatekeepingOrderSealDecision(GatekeepingOrderSealDecision.builder()
                    .orderStatus(DRAFT)
                    .draftDocument(SDO_REFERENCE)
                    .build())
                .build())
            .allocationDecision(allocationDecision)
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

    @ParameterizedTest
    @MethodSource("caseTranslationRequirement")
    void shouldBuildSealedSDOAndRemoveTransientFieldsWhenOrderStatusIsSealed(
        String caseLanguageRequirement,
        LanguageTranslationRequirement expectedTranslationRequirements) {

        Allocation allocationDecision = createAllocation("Lay justices", "Reason");
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
            .state(GATEKEEPING.getLabel())
            .data(ofEntries(
                entry("languageRequirement", caseLanguageRequirement),
                entry("gatekeepingOrderRouter", SERVICE),
                entry("caseLocalAuthority", LOCAL_AUTHORITY_1_CODE),
                entry("dateSubmitted", dateNow()),
                entry("applicants", getApplicant()),
                entry("hearingDetails", wrapElements(hearingBooking)),
                entry("orders", Orders.builder().orderType(List.of(CARE_ORDER)).build()),
                entry("gatekeepingOrderIssuingJudge", JudgeAndLegalAdvisor.builder().build()),
                entry("gatekeepingOrderSealDecision", gatekeepingOrderSealDecision),
                entry("gatekeepingTranslationRequirements", expectedTranslationRequirements),
                entry("directionsForAllParties", List.of(ATTEND_HEARING)),
                entry("direction-ATTEND_HEARING", standardDirection),
                entry("allocationDecision", allocationDecision),
                entry("customDirections", wrapElements(customDirection))))
            .build();

        StandardDirectionOrder expectedSDO = StandardDirectionOrder.builder()
            .orderDoc(SDO_REFERENCE)
            .unsealedDocumentCopy(SDO_REFERENCE)
            .orderStatus(SEALED)
            .dateOfIssue("3 March 2021")
            .customDirections(wrapElements(customDirection))
            .standardDirections(wrapElements(standardDirection))
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder().build())
            .translationRequirements(expectedTranslationRequirements)
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseData);
        CaseData responseData = extractCaseData(response);

        assertThat(responseData.getStandardDirectionOrder()).isEqualTo(expectedSDO);
        assertThat(response.getData()).doesNotContainKey("standardDirections");
    }

    @Test
    void shouldUpdateStateAndOrderDocWhenSDOIsSealedThroughUploadRouteAndRemoveRouterAndSendNoticeOfProceedings() {
        DocumentReference document = DocumentReference.builder().filename("final.docx").build();
        Court court = Court.builder().build();
        givenCurrentUserWithName("adam");
        Allocation allocationDecision = createAllocation("Lay justices", "Reason");
        CaseData responseCaseData = extractCaseData(
            postAboutToSubmitEvent(validCaseDetailsForUploadRoute(document, court, allocationDecision))
        );

        assertThat(responseCaseData.getSdoRouter()).isNull();
    }

    private static Stream<Arguments> translationRequirements() {
        return Stream.of(
            Arguments.of(LanguageTranslationRequirement.NO),
            Arguments.of(LanguageTranslationRequirement.WELSH_TO_ENGLISH),
            Arguments.of(LanguageTranslationRequirement.ENGLISH_TO_WELSH)
        );
    }

    private static Stream<Arguments> caseTranslationRequirement() {
        return Stream.of(
            Arguments.of(YesNo.YES.getValue(), LanguageTranslationRequirement.ENGLISH_TO_WELSH),
            Arguments.of(YesNo.NO.getValue(), LanguageTranslationRequirement.NO),
            Arguments.of("", LanguageTranslationRequirement.NO)
        );
    }

    private List<Element<Applicant>> getApplicant() {
        return wrapElements(Applicant.builder()
            .party(ApplicantParty.builder()
                .organisationName("")
                .build())
            .build());
    }

    private CaseData validCaseDetailsForUploadRoute(
        DocumentReference document, Court court, Allocation allocation
    ) {
        CaseData.CaseDataBuilder builder = CaseData.builder()
            .id(1234123412341234L)
            .court(court)
            .hearingDetails(wrapElements(testHearing()))
            .gatekeepingOrderEventData(GatekeepingOrderEventData.builder()
                .gatekeepingOrderSealDecision(GatekeepingOrderSealDecision.builder()
                    .orderStatus(SEALED)
                    .draftDocument(document)
                    .build())
                .build())
            .caseLocalAuthority(LOCAL_AUTHORITY_1_CODE)
            .familyManCaseNumber("1234")
            .orders(Orders.builder().orderType(List.of(CARE_ORDER)).build())
            .gatekeepingOrderRouter(GatekeepingOrderRoute.UPLOAD)
            .state(GATEKEEPING)
            .allocationDecision(allocation)
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                .useAllocatedJudge("Yes")
                .legalAdvisorName("Chris Newport")
                .build())
            .allocatedJudge(Judge.builder()
                .judgeTitle(MAGISTRATES)
                .judgeFullName("John Walker")
                .build());

        return builder.build();
    }

    private Allocation createAllocation(String proposal, String judgeLevelRadio) {
        Allocation allocationDecision = Allocation.builder()
            .proposalV2(proposal)
            .judgeLevelRadio(judgeLevelRadio)
            .build();
        return allocationDecision;
    }
}
