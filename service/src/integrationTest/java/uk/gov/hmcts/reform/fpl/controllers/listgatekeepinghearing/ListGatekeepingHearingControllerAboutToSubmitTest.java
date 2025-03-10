package uk.gov.hmcts.reform.fpl.controllers.listgatekeepinghearing;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.fpl.config.rd.JudicialUsersConfiguration;
import uk.gov.hmcts.reform.fpl.config.rd.LegalAdviserUsersConfiguration;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.controllers.ListGatekeepingHearingController;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.Allocation;
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
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisNoticeOfHearing;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisNoticeOfProceeding;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisStandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.TestDataHelper;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.CAFCASS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionDueDateType.DATE;
import static uk.gov.hmcts.reform.fpl.enums.DirectionDueDateType.DAYS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionType.ATTEND_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.DirectionType.CUSTOM;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.SEALED;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.State.GATEKEEPING;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.GatekeepingOrderRoute.SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.GatekeepingOrderRoute.UPLOAD;
import static uk.gov.hmcts.reform.fpl.enums.hearing.HearingAttendance.IN_PERSON;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createPopulatedChildren;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.SecureDocumentManagementStoreLoader.document;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocument;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentBinaries;

@OverrideAutoConfiguration(enabled = true)
@WebMvcTest(ListGatekeepingHearingController.class)
class ListGatekeepingHearingControllerAboutToSubmitTest extends AbstractCallbackTest {

    private static final Document SDO_DOCUMENT = testDocument();
    private static final Document C6_DOCUMENT = testDocument();
    private static final DocumentReference SDO_REFERENCE = DocumentReference.buildFromDocument(SDO_DOCUMENT);
    private static final DocumentReference C6_REFERENCE = DocumentReference.buildFromDocument(C6_DOCUMENT);
    private static final UserDetails USER = UserDetails.builder()
        .forename("John")
        .surname("Smith")
        .build();

    @MockBean
    private Time time;

    @MockBean
    private DocmosisDocumentGeneratorService docmosisDocumentGeneratorService;

    @MockBean
    private UploadDocumentService uploadDocumentService;

    @MockBean
    private JudicialUsersConfiguration judicialUsersConfiguration;

    @MockBean
    private LegalAdviserUsersConfiguration legalAdviserUsersConfiguration;

    ListGatekeepingHearingControllerAboutToSubmitTest() {
        super("list-gatekeeping-hearing");
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
    void shouldBuildSealedSDOAndRemoveTransientFieldsForServiceRoute(
        final String caseLanguageRequirement,
        final LanguageTranslationRequirement expectedTranslationRequirements) {

        when(time.now()).thenReturn(LocalDateTime.of(2021, 3, 3, 0, 0, 0));
        mockDocumentGenerationAndUpload();

        final Allocation allocationDecision = createAllocation("Lay justices", "Reason");
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

        final GatekeepingOrderSealDecision gatekeepingOrderSealDecision = GatekeepingOrderSealDecision.builder()
            .orderStatus(SEALED)
            .dateOfIssue(time.now().toLocalDate())
            .draftDocument(SDO_REFERENCE)
            .build();

        final HearingBooking newHearing = createHearing(now().plusDays(2));

        final Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("children1", createPopulatedChildren(now().toLocalDate()));
        caseDataMap.put("sendNoticeOfHearing", "Yes");
        caseDataMap.put("hearingType", newHearing.getType());
        caseDataMap.put("hearingVenue", newHearing.getVenue());
        caseDataMap.put("hearingVenueCustom", newHearing.getVenueCustomAddress());
        caseDataMap.put("hearingStartDate", newHearing.getStartDate());
        caseDataMap.put("hearingEndDate", newHearing.getEndDate());
        caseDataMap.put("hearingAttendance", newHearing.getAttendance());
        caseDataMap.put("judgeAndLegalAdvisor", newHearing.getJudgeAndLegalAdvisor());
        caseDataMap.put("noticeOfHearingNotes", null);
        caseDataMap.put("languageRequirement", caseLanguageRequirement);
        caseDataMap.put("gatekeepingOrderRouter", SERVICE);
        caseDataMap.put("caseLocalAuthority", LOCAL_AUTHORITY_1_CODE);
        caseDataMap.put("dateSubmitted", dateNow());
        caseDataMap.put("applicants", getApplicant());
        caseDataMap.put("orders", Orders.builder().orderType(List.of(CARE_ORDER)).build());
        caseDataMap.put("gatekeepingOrderIssuingJudge", JudgeAndLegalAdvisor.builder().build());
        caseDataMap.put("gatekeepingOrderSealDecision", gatekeepingOrderSealDecision);
        caseDataMap.put("gatekeepingTranslationRequirements", expectedTranslationRequirements);
        caseDataMap.put("directionsForAllParties", List.of(ATTEND_HEARING));
        caseDataMap.put("direction-ATTEND_HEARING", standardDirection);
        caseDataMap.put("allocationDecision", allocationDecision);
        caseDataMap.put("customDirections", wrapElements(customDirection));

        final CaseDetails caseData = CaseDetails.builder()
            .id(1234123412341234L)
            .state(GATEKEEPING.getLabel())
            .data(caseDataMap)
            .build();

        final StandardDirectionOrder expectedSDO = StandardDirectionOrder.builder()
            .orderDoc(SDO_REFERENCE)
            .unsealedDocumentCopy(SDO_REFERENCE)
            .orderStatus(SEALED)
            .dateOfIssue("3 March 2021")
            .customDirections(wrapElements(customDirection))
            .standardDirections(wrapElements(standardDirection))
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder().build())
            .translationRequirements(expectedTranslationRequirements)
            .build();

        final AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseData);
        final CaseData responseData = extractCaseData(response);

        assertThat(responseData.getStandardDirectionOrder()).isEqualTo(expectedSDO);
        assertThat(responseData.getNoticeOfProceedingsBundle())
            .extracting(Element::getValue)
            .containsExactly(DocumentBundle.builder().document(C6_REFERENCE)
                .translationRequirements(expectedTranslationRequirements)
                .build());
        assertThat(response.getData()).doesNotContainKeys("customDirections",
            "standardDirections", "gatekeepingOrderIssuingJudge");

        final HearingBooking hearingAfterCallback = newHearing.toBuilder().noticeOfHearing(
            DocumentReference.buildFromDocument(document())).build();

        assertThat(responseData.getHearingDetails()).extracting(Element::getValue)
            .containsExactly(hearingAfterCallback);
        assertThat(responseData.getFirstHearingFlag()).isNull();
        assertThat(responseData.getSelectedHearingId())
            .isEqualTo(responseData.getHearingDetails().get(0).getId());
    }

    @ParameterizedTest
    @MethodSource("caseTranslationRequirement")
    void shouldBuildSealedSDOAndRemoveTransientFieldsForUploadRoute(
        final String caseLanguageRequirement,
        final LanguageTranslationRequirement expectedTranslationRequirements) {

        when(time.now()).thenReturn(LocalDateTime.of(2021, 3, 3, 0, 0, 0));
        mockDocumentGenerationAndUpload();

        final Allocation allocationDecision = createAllocation("Lay justices", "Reason");
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

        final GatekeepingOrderSealDecision gatekeepingOrderSealDecision = GatekeepingOrderSealDecision.builder()
            .orderStatus(SEALED)
            .dateOfIssue(time.now().toLocalDate())
            .draftDocument(SDO_REFERENCE)
            .build();

        final HearingBooking newHearing = createHearing(now().plusDays(2));

        final Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("children1", createPopulatedChildren(now().toLocalDate()));
        caseDataMap.put("sendNoticeOfHearing", "Yes");
        caseDataMap.put("hearingType", newHearing.getType());
        caseDataMap.put("hearingVenue", newHearing.getVenue());
        caseDataMap.put("hearingVenueCustom", newHearing.getVenueCustomAddress());
        caseDataMap.put("hearingStartDate", newHearing.getStartDate());
        caseDataMap.put("hearingEndDate", newHearing.getEndDate());
        caseDataMap.put("hearingAttendance", newHearing.getAttendance());
        caseDataMap.put("judgeAndLegalAdvisor", newHearing.getJudgeAndLegalAdvisor());
        caseDataMap.put("noticeOfHearingNotes", null);
        caseDataMap.put("languageRequirement", caseLanguageRequirement);
        caseDataMap.put("gatekeepingOrderRouter", UPLOAD);
        caseDataMap.put("caseLocalAuthority", LOCAL_AUTHORITY_1_CODE);
        caseDataMap.put("dateSubmitted", dateNow());
        caseDataMap.put("applicants", getApplicant());
        caseDataMap.put("orders", Orders.builder().orderType(List.of(CARE_ORDER)).build());
        caseDataMap.put("gatekeepingOrderIssuingJudge", JudgeAndLegalAdvisor.builder().build());
        caseDataMap.put("gatekeepingOrderSealDecision", gatekeepingOrderSealDecision);
        caseDataMap.put("gatekeepingTranslationRequirements", expectedTranslationRequirements);
        caseDataMap.put("directionsForAllParties", List.of(ATTEND_HEARING));
        caseDataMap.put("direction-ATTEND_HEARING", standardDirection);
        caseDataMap.put("allocationDecision", allocationDecision);
        caseDataMap.put("customDirections", wrapElements(customDirection));

        final CaseDetails caseData = CaseDetails.builder()
            .id(1234123412341234L)
            .state(GATEKEEPING.getLabel())
            .data(caseDataMap)
            .build();

        final StandardDirectionOrder expectedSDO = StandardDirectionOrder.builder()
            .orderDoc(null)
            .unsealedDocumentCopy(null)
            .orderStatus(SEALED)
            .dateOfUpload(LocalDate.of(2021, 3, 3))
            .customDirections(wrapElements(customDirection))
            .standardDirections(wrapElements(standardDirection))
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder().build())
            .translationRequirements(expectedTranslationRequirements)
            .uploader("John Smith")
            .build();

        final AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseData);
        final CaseData responseData = extractCaseData(response);

        assertThat(responseData.getStandardDirectionOrder()).isEqualTo(expectedSDO);
        assertThat(responseData.getNoticeOfProceedingsBundle())
            .extracting(Element::getValue)
            .containsExactly(DocumentBundle.builder().document(C6_REFERENCE)
                .translationRequirements(expectedTranslationRequirements)
                .build());
        assertThat(response.getData()).doesNotContainKeys("customDirections",
            "standardDirections", "gatekeepingOrderIssuingJudge");

        final HearingBooking hearingAfterCallback = newHearing.toBuilder().noticeOfHearing(
            DocumentReference.buildFromDocument(document())).build();

        assertThat(responseData.getHearingDetails()).extracting(Element::getValue)
            .containsExactly(hearingAfterCallback);
        assertThat(responseData.getFirstHearingFlag()).isNull();
        assertThat(responseData.getSelectedHearingId())
            .isEqualTo(responseData.getHearingDetails().get(0).getId());
    }

    private void mockDocumentGenerationAndUpload() {

        final byte[] sdoBinaries = testDocumentBinaries();
        final byte[] c6Binaries = testDocumentBinaries();
        final String sealedOrderFileName = "standard-directions-order.pdf";
        final String c6FileName = "c6.pdf";

        final byte[] documentContent = TestDataHelper.DOCUMENT_CONTENT;

        given(docmosisDocumentGeneratorService.generateDocmosisDocument(any(DocmosisNoticeOfHearing.class), any()))
            .willReturn(new DocmosisDocument("filename.pdf", documentContent));

        given(docmosisDocumentGeneratorService
            .generateDocmosisDocument(any(DocmosisStandardDirectionOrder.class), any()))
            .willReturn(new DocmosisDocument(sealedOrderFileName, sdoBinaries));

        given(docmosisDocumentGeneratorService.generateDocmosisDocument(any(DocmosisNoticeOfProceeding.class), any()))
            .willReturn(new DocmosisDocument(c6FileName, c6Binaries));

        given(uploadDocumentService.uploadPDF(eq(documentContent), any())).willReturn(document());
        given(uploadDocumentService.uploadPDF(sdoBinaries, sealedOrderFileName)).willReturn(SDO_DOCUMENT);
        given(uploadDocumentService.uploadPDF(c6Binaries, c6FileName)).willReturn(C6_DOCUMENT);
        givenCurrentUser(USER);
    }

    private List<Element<Applicant>> getApplicant() {
        return wrapElements(Applicant.builder()
            .party(ApplicantParty.builder()
                .organisationName("")
                .build())
            .build());
    }

    private Allocation createAllocation(String proposal, String judgeLevelRadio) {
        return Allocation.builder()
            .proposalV2(proposal)
            .judgeLevelRadio(judgeLevelRadio)
            .build();
    }

    private HearingBooking createHearing(final LocalDateTime startDate) {
        return HearingBooking.builder()
            .type(CASE_MANAGEMENT)
            .startDate(startDate)
            .endDate(startDate.plusDays(1))
            .endDateDerived("No")
            .hearingJudgeLabel("Her Honour Judge Judy")
            .legalAdvisorLabel("")
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                .judgeTitle(JudgeOrMagistrateTitle.HER_HONOUR_JUDGE)
                .judgeLastName("Judy")
                .build())
            .others(emptyList())
            .venueCustomAddress(Address.builder().build())
            .attendance(List.of(IN_PERSON))
            .othersNotified("")
            .build();
    }
}
