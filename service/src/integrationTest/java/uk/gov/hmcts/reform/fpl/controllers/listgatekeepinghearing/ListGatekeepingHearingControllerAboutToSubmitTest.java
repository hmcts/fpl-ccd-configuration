package uk.gov.hmcts.reform.fpl.controllers.listgatekeepinghearing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
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
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisData;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisNoticeOfHearing;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisNoticeOfProceeding;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisStandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.service.DocumentSealingService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;
import uk.gov.hmcts.reform.fpl.utils.TestDataHelper;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Map.entry;
import static java.util.Map.ofEntries;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.CAFCASS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionDueDateType.DATE;
import static uk.gov.hmcts.reform.fpl.enums.DirectionDueDateType.DAYS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionType.ATTEND_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.DirectionType.CUSTOM;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.SEALED;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.State.GATEKEEPING;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.GatekeepingOrderRoute.SERVICE;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createPopulatedChildren;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.findElementsId;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.SecureDocumentManagementStoreLoader.document;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocmosisDocument;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocument;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentBinaries;

class ListGatekeepingHearingControllerAboutToSubmitTest extends ListGatekeepingHearingControllerTest {

    public static final String TEST_REASON = "Test reason";

    private static final Document SDO_DOCUMENT = testDocument();
    private static final Document C6_DOCUMENT = testDocument();
    private static final DocumentReference SDO_REFERENCE = DocumentReference.buildFromDocument(SDO_DOCUMENT);
    private static final DocumentReference C6_REFERENCE = DocumentReference.buildFromDocument(C6_DOCUMENT);
    private static final UserDetails USER = UserDetails.builder()
        .forename("John")
        .surname("Smith")
        .build();

    private final Time time = new FixedTimeConfiguration().fixedDateTime(LocalDateTime.of(2021, 3, 3, 0, 0, 0));

    @MockBean
    private DocmosisDocumentGeneratorService docmosisDocumentGeneratorService;

    @MockBean
    private UploadDocumentService uploadDocumentService;

    @MockBean
    private DocumentSealingService sealingService;

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

    @Test
    void shouldAddNewHearingToHearingDetailsListWhenAddHearingSelected() {

        final HearingBooking newHearing = testHearing(now().plusDays(2));
        final CaseData initialCaseData = CaseData.builder()
            .hearingType(newHearing.getType())
            .hearingVenue(newHearing.getVenue())
            .hearingVenueCustom(newHearing.getVenueCustomAddress())
            .hearingStartDate(newHearing.getStartDate())
            .hearingEndDate(newHearing.getEndDate())
            .hearingAttendance(newHearing.getAttendance())
            .judgeAndLegalAdvisor(newHearing.getJudgeAndLegalAdvisor())
            .noticeOfHearingNotes(newHearing.getAdditionalNotes())
            .sendNoticeOfHearing("Yes")
            .build();

        final CaseData updatedCaseData = extractCaseData(postAboutToSubmitEvent(initialCaseData));

        assertThat(updatedCaseData.getHearingDetails()).extracting(Element::getValue).containsExactly(newHearing);
        assertThat(updatedCaseData.getFirstHearingFlag()).isNull();
        assertThat(updatedCaseData.getSelectedHearingId())
            .isIn(findElementsId(newHearing, updatedCaseData.getHearingDetails()));
    }

    @Test
    void shouldIncludeNoticeOfHearing() {

        final Document document = document();

        given(docmosisDocumentGeneratorService.generateDocmosisDocument(any(DocmosisData.class), any()))
            .willReturn(testDocmosisDocument(TestDataHelper.DOCUMENT_CONTENT));
        given(uploadDocumentService.uploadPDF(any(), any())).willReturn(document);

        final HearingBooking newHearing = testHearing(now().plusDays(2));
        final CaseData initialCaseData = CaseData.builder()
            .id(1234123412341234L)
            .children1(createPopulatedChildren(now().toLocalDate()))
            .caseLocalAuthority(LOCAL_AUTHORITY_1_CODE)
            .sendNoticeOfHearing("Yes")
            .hearingType(newHearing.getType())
            .hearingVenue(newHearing.getVenue())
            .hearingVenueCustom(newHearing.getVenueCustomAddress())
            .hearingStartDate(newHearing.getStartDate())
            .hearingEndDate(newHearing.getEndDate())
            .hearingAttendance(newHearing.getAttendance())
            .judgeAndLegalAdvisor(newHearing.getJudgeAndLegalAdvisor())
            .noticeOfHearingNotes(newHearing.getAdditionalNotes())
            .build();

        final CaseData updatedCaseData = extractCaseData(postAboutToSubmitEvent(initialCaseData));

        final HearingBooking hearingAfterCallback = newHearing.toBuilder().noticeOfHearing(
            DocumentReference.buildFromDocument(document)).build();

        assertThat(updatedCaseData.getHearingDetails()).extracting(Element::getValue)
            .containsExactly(hearingAfterCallback);
        assertThat(updatedCaseData.getFirstHearingFlag()).isNull();
    }

    @ParameterizedTest
    @MethodSource("caseTranslationRequirement")
    void shouldBuildSealedSDOAndRemoveTransientFieldsWhenOrderStatusIsSealed(
        final String caseLanguageRequirement,
        final LanguageTranslationRequirement expectedTranslationRequirements) {

        mockDocuments();

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

        final HearingBooking hearingBooking = HearingBooking.builder()
            .startDate(LocalDateTime.now())
            .endDate(LocalDateTime.now().plusDays(1))
            .venue("Venue").build();

        final GatekeepingOrderSealDecision gatekeepingOrderSealDecision = GatekeepingOrderSealDecision.builder()
            .orderStatus(SEALED)
            .dateOfIssue(time.now().toLocalDate())
            .draftDocument(SDO_REFERENCE)
            .build();

        final HearingBooking newHearing = testHearing(now().plusDays(2));

        final CaseDetails caseData = CaseDetails.builder()
            .id(1234123412341234L)
            .state(GATEKEEPING.getLabel())
            .data(ofEntries(
                entry("hearingType", newHearing.getType()),
                entry("hearingVenue", newHearing.getVenue()),
                entry("hearingVenueCustom", newHearing.getVenueCustomAddress()),
                entry("hearingStartDate", newHearing.getStartDate()),
                entry("hearingEndDate", newHearing.getEndDate()),
                entry("hearingAttendance", newHearing.getAttendance()),
                entry("judgeAndLegalAdvisor", newHearing.getJudgeAndLegalAdvisor()),
                entry("noticeOfHearingNotes", ""),
                entry("sendNoticeOfHearing", "Yes"),
                entry("children1", createPopulatedChildren(now().toLocalDate())),

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

        //TODO: Add hearing assertions
//        final HearingBooking hearingAfterCallback = newHearing.toBuilder().noticeOfHearing(
//            DocumentReference.buildFromDocument(document())).build();
//
//        assertThat(responseData.getHearingDetails()).extracting(Element::getValue)
//            .containsExactly(hearingAfterCallback);
//        assertThat(responseData.getFirstHearingFlag()).isNull();
    }

    private void mockDocuments() {

        final byte[] sdoBinaries = testDocumentBinaries();
        final byte[] c6Binaries = testDocumentBinaries();
        final String sealedOrderFileName = "standard-directions-order.pdf";
        final String c6FileName = "c6.pdf";

        final byte[] documentContent = TestDataHelper.DOCUMENT_CONTENT;

        given(docmosisDocumentGeneratorService.generateDocmosisDocument(any(DocmosisNoticeOfHearing.class), any()))
            .willReturn(new DocmosisDocument("filename.pdf", documentContent));

        given(docmosisDocumentGeneratorService.generateDocmosisDocument(any(DocmosisStandardDirectionOrder.class), any()))
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
        Allocation allocationDecision = Allocation.builder()
            .proposal(proposal)
            .judgeLevelRadio(judgeLevelRadio)
            .build();
        return allocationDecision;
    }
}
