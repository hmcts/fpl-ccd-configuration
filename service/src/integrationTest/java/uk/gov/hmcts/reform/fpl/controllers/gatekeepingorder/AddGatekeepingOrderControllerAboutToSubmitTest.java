package uk.gov.hmcts.reform.fpl.controllers.gatekeepingorder;

import org.assertj.core.api.AssertionsForClassTypes;
import org.assertj.core.api.AssertionsForInterfaceTypes;
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
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.controllers.AddGatekeepingOrderController;
import uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement;
import uk.gov.hmcts.reform.fpl.enums.OrderStatus;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.GatekeepingOrderRoute;
import uk.gov.hmcts.reform.fpl.model.Allocation;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CustomDirection;
import uk.gov.hmcts.reform.fpl.model.GatekeepingOrderSealDecision;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Judge;
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
import uk.gov.hmcts.reform.fpl.model.document.SealType;
import uk.gov.hmcts.reform.fpl.model.event.GatekeepingOrderEventData;
import uk.gov.hmcts.reform.fpl.model.order.UrgentHearingOrder;
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
import static uk.gov.hmcts.reform.fpl.enums.State.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.State.GATEKEEPING;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.GatekeepingOrderRoute.SERVICE;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocument;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentBinaries;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;
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
    void shouldBuildDraftSDOWithExistingDraftDocumentWhenOrderStatusIsDraft() {
        CaseData caseData = CaseData.builder()
            .gatekeepingOrderRouter(SERVICE)
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

    @ParameterizedTest
    @MethodSource("caseTranslationRequirement")
    void shouldBuildSealedSDOAndRemoveTransientFieldsWhenOrderStatusIsSealed(
        String caseLanguageRequirement,
        LanguageTranslationRequirement expectedTranslationRequirements) {

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
        assertThat(responseData.getState()).isEqualTo(CASE_MANAGEMENT);
        assertThat(responseData.getNoticeOfProceedingsBundle())
            .extracting(Element::getValue)
            .containsExactly(DocumentBundle.builder().document(C6_REFERENCE)
                .translationRequirements(expectedTranslationRequirements)
                .build());
        assertThat(response.getData()).doesNotContainKeys("gatekeepingOrderRouter", "customDirections",
            "standardDirections", "gatekeepingOrderIssuingJudge", "gatekeepingOrderSealDecision");
    }


    @ParameterizedTest
    @MethodSource("translationRequirements")
    void shouldBuildUrgentHearingOrderAndAddAllocationDecision(
        LanguageTranslationRequirement translationRequirements) {

        final DocumentReference urgentReference = testDocumentReference();
        final DocumentReference sealedUrgentReference = testDocumentReference();
        final Allocation allocation = Allocation.builder()
            .judgeLevelRadio("No")
            .proposal("Section 9 circuit judge")
            .proposalReason("some reason")
            .allocationProposalPresent("Yes")
            .build();

        CaseData caseData = CaseData.builder()
            .hearingDetails(wrapElements(HearingBooking.builder()
                .startDate(now())
                .endDate(now().plusDays(1))
                .venue("EXAMPLE")
                .build()))
            .caseLocalAuthority(LOCAL_AUTHORITY_1_CODE)
            .familyManCaseNumber("1234")
            .orders(Orders.builder().orderType(List.of(CARE_ORDER)).build())
            .gatekeepingOrderRouter(GatekeepingOrderRoute.URGENT)
            .gatekeepingOrderEventData(GatekeepingOrderEventData.builder()
                .urgentHearingAllocation(allocation)
                .urgentHearingOrderDocument(urgentReference)
                .urgentGatekeepingTranslationRequirements(translationRequirements)
                .build())
            .state(GATEKEEPING)
            .id(1234123412341234L)
            .build();

        given(sealingService.sealDocument(urgentReference, SealType.ENGLISH)).willReturn(sealedUrgentReference);

        CaseData responseData = extractCaseData(postAboutToSubmitEvent(caseData));

        Allocation expectedAllocation = allocation.toBuilder().judgeLevelRadio(null).build();

        AssertionsForClassTypes.assertThat(responseData.getAllocationDecision()).isEqualTo(expectedAllocation);
        AssertionsForInterfaceTypes.assertThat(responseData.getNoticeOfProceedingsBundle())
            .extracting(Element::getValue)
            .containsExactly(DocumentBundle.builder().document(C6_REFERENCE)
                .translationRequirements(translationRequirements)
                .build()
            );
        AssertionsForClassTypes.assertThat(responseData.getUrgentHearingOrder()).isEqualTo(
            UrgentHearingOrder.builder()
                .allocation("Section 9 circuit judge")
                .order(sealedUrgentReference)
                .unsealedOrder(urgentReference)
                .dateAdded(dateNow())
                .translationRequirements(translationRequirements)
                .build()
        );
    }

    @Test
    void shouldUpdateStateAndOrderDocWhenSDOIsSealedThroughUploadRouteAndRemoveRouterAndSendNoticeOfProceedings() {
        DocumentReference sealedDocument = DocumentReference.builder().filename("sealed.pdf").build();
        DocumentReference document = DocumentReference.builder().filename("final.docx").build();

        givenCurrentUserWithName("adam");
        given(sealingService.sealDocument(document, SealType.ENGLISH)).willReturn(sealedDocument);

        CaseData responseCaseData = extractCaseData(
            postAboutToSubmitEvent(validCaseDetailsForUploadRoute(document, SEALED))
        );

        assertThat(responseCaseData.getStandardDirectionOrder().getLastUploadedOrder()).isEqualTo(document);
        assertThat(responseCaseData.getNoticeOfProceedingsBundle())
            .extracting(Element::getValue)
            .containsExactly(DocumentBundle.builder().document(C6_REFERENCE).build());
        assertThat(responseCaseData.getState()).isEqualTo(State.CASE_MANAGEMENT);
        assertThat(responseCaseData.getSdoRouter()).isNull();
    }

    private List<Element<Applicant>> getApplicant() {
        return wrapElements(Applicant.builder()
            .party(ApplicantParty.builder()
                .organisationName("")
                .build())
            .build());
    }

    private CaseData validCaseDetailsForUploadRoute(DocumentReference document, OrderStatus status) {
        CaseData.CaseDataBuilder builder = CaseData.builder()
            .id(1234123412341234L)
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


}
