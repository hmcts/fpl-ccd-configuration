package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.fpl.config.rd.JudicialUsersConfiguration;
import uk.gov.hmcts.reform.fpl.config.rd.LegalAdviserUsersConfiguration;
import uk.gov.hmcts.reform.fpl.enums.CMOStatus;
import uk.gov.hmcts.reform.fpl.enums.HearingOrderType;
import uk.gov.hmcts.reform.fpl.enums.HearingReListOption;
import uk.gov.hmcts.reform.fpl.enums.HearingStatus;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.HearingCancellationReason;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisData;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundle;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;
import uk.gov.hmcts.reform.fpl.utils.TestDataHelper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.UUID.randomUUID;
import static org.apache.commons.lang3.RandomUtils.nextLong;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;
import static uk.gov.hmcts.reform.fpl.enums.HearingOptions.ADJOURN_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.HearingOptions.EDIT_FUTURE_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.HearingOptions.EDIT_PAST_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.HearingOptions.RE_LIST_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.HearingOptions.VACATE_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.HearingReListOption.RE_LIST_LATER;
import static uk.gov.hmcts.reform.fpl.enums.HearingReListOption.RE_LIST_NOW;
import static uk.gov.hmcts.reform.fpl.enums.HearingStatus.ADJOURNED;
import static uk.gov.hmcts.reform.fpl.enums.HearingStatus.ADJOURNED_AND_RE_LISTED;
import static uk.gov.hmcts.reform.fpl.enums.HearingStatus.ADJOURNED_TO_BE_RE_LISTED;
import static uk.gov.hmcts.reform.fpl.enums.HearingStatus.VACATED;
import static uk.gov.hmcts.reform.fpl.enums.HearingStatus.VACATED_TO_BE_RE_LISTED;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.ISSUE_RESOLUTION;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createPopulatedChildren;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.findElementsId;
import static uk.gov.hmcts.reform.fpl.utils.SecureDocumentManagementStoreLoader.document;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocmosisDocument;

class ManageHearingsControllerAboutToSubmitTest extends ManageHearingsControllerTest {

    public static final String TEST_REASON = "Test reason";

    @MockBean
    private DocmosisDocumentGeneratorService docmosisDocumentGeneratorService;

    @MockBean
    private UploadDocumentService uploadDocumentService;

    @MockBean
    private JudicialUsersConfiguration judicialUsersConfiguration;

    @MockBean
    private LegalAdviserUsersConfiguration legalAdviserUsersConfiguration;

    ManageHearingsControllerAboutToSubmitTest() {
        super("manage-hearings");
    }

    @Test
    void shouldAddNewHearingToHearingDetailsListWhenAddHearingSelected() {
        HearingBooking newHearing = testHearing(now().plusDays(2));
        CaseData initialCaseData = CaseData.builder()
            .hearingType(newHearing.getType())
            .hearingVenue(newHearing.getVenue())
            .hearingVenueCustom(newHearing.getVenueCustomAddress())
            .hearingStartDate(newHearing.getStartDate())
            .hearingEndDate(newHearing.getEndDate())
            .hearingAttendance(newHearing.getAttendance())
            .judgeAndLegalAdvisor(newHearing.getJudgeAndLegalAdvisor())
            .noticeOfHearingNotes(newHearing.getAdditionalNotes())
            .build();

        CaseData updatedCaseData = extractCaseData(postAboutToSubmitEvent(initialCaseData));

        assertThat(updatedCaseData.getHearingDetails()).extracting(Element::getValue).containsExactly(newHearing);
        assertThat(updatedCaseData.getFirstHearingFlag()).isNull();
        assertThat(updatedCaseData.getSelectedHearingId())
            .isIn(findElementsId(newHearing, updatedCaseData.getHearingDetails()));
    }

    @Test
    void shouldIncludeNoticeOfHearingWhenSendNoticeOfHearingSelected() {
        Document document = document();

        given(docmosisDocumentGeneratorService.generateDocmosisDocument(any(DocmosisData.class), any()))
            .willReturn(testDocmosisDocument(TestDataHelper.DOCUMENT_CONTENT));
        given(uploadDocumentService.uploadPDF(any(), any())).willReturn(document);

        HearingBooking newHearing = testHearing(now().plusDays(2));
        CaseData initialCaseData = CaseData.builder()
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

        CaseData updatedCaseData = extractCaseData(postAboutToSubmitEvent(initialCaseData));

        HearingBooking hearingAfterCallback = newHearing.toBuilder().noticeOfHearing(
            DocumentReference.buildFromDocument(document)).build();

        assertThat(updatedCaseData.getHearingDetails()).extracting(Element::getValue)
            .containsExactly(hearingAfterCallback);
        assertThat(updatedCaseData.getFirstHearingFlag()).isNull();
    }

    @Test
    void shouldUpdateExistingFutureHearingInHearingDetailsListWhenEditHearingSelected() {
        HearingBooking existingHearing = testHearing(now().plusDays(2)).toBuilder()
            .type(ISSUE_RESOLUTION)
            .build();

        Element<HearingBooking> hearingElement = element(existingHearing);

        CaseData initialCaseData = CaseData.builder()
            .hearingOption(EDIT_FUTURE_HEARING)
            .hearingType(ISSUE_RESOLUTION)
            .futureHearingDateList(dynamicList(hearingElement.getId(), hearingElement))
            .hearingDetails(List.of(hearingElement))
            .hearingVenue(existingHearing.getVenue())
            .hearingVenueCustom(existingHearing.getVenueCustomAddress())
            .hearingStartDate(existingHearing.getStartDate())
            .hearingEndDate(existingHearing.getEndDate())
            .hearingAttendance(existingHearing.getAttendance())
            .judgeAndLegalAdvisor(existingHearing.getJudgeAndLegalAdvisor())
            .noticeOfHearingNotes(existingHearing.getAdditionalNotes())
            .build();

        CaseData updatedCaseData = extractCaseData(postAboutToSubmitEvent(asCaseDetails(initialCaseData)));

        HearingBooking expectedHearing = existingHearing.toBuilder().type(ISSUE_RESOLUTION).build();

        assertThat(updatedCaseData.getHearingDetails()).extracting(Element::getValue).containsExactly(expectedHearing);
        assertThat(updatedCaseData.getSelectedHearingId()).isEqualTo(hearingElement.getId());
    }

    @Test
    void shouldUpdateExistingPastHearingInHearingDetailsListWhenEditHearingSelected() {
        HearingBooking existingHearing = testHearing(now().minusDays(2)).toBuilder()
            .type(ISSUE_RESOLUTION)
            .build();

        Element<HearingBooking> hearingElement = element(existingHearing);

        CaseData initialCaseData = CaseData.builder()
            .hearingOption(EDIT_PAST_HEARING)
            .hearingType(ISSUE_RESOLUTION)
            .pastHearingDateList(dynamicList(hearingElement.getId(), hearingElement))
            .hearingDetails(List.of(hearingElement))
            .hearingVenue(existingHearing.getVenue())
            .hearingVenueCustom(existingHearing.getVenueCustomAddress())
            .hearingStartDate(existingHearing.getStartDate())
            .hearingEndDate(existingHearing.getEndDate())
            .hearingAttendance(existingHearing.getAttendance())
            .judgeAndLegalAdvisor(existingHearing.getJudgeAndLegalAdvisor())
            .noticeOfHearingNotes(existingHearing.getAdditionalNotes())
            .build();

        CaseData updatedCaseData = extractCaseData(postAboutToSubmitEvent(asCaseDetails(initialCaseData)));

        HearingBooking expectedHearing = existingHearing.toBuilder().type(ISSUE_RESOLUTION).build();

        assertThat(updatedCaseData.getHearingDetails()).extracting(Element::getValue).containsExactly(expectedHearing);
        assertThat(updatedCaseData.getSelectedHearingId()).isEqualTo(hearingElement.getId());
    }

    @Test
    void shouldAdjournAndReListHearing() {
        Element<HearingBooking> pastHearing = element(testHearing(LocalDateTime.now().minusDays(1)));
        Element<HearingBooking> pastHearingToBeAdjourned = element(testHearing(LocalDateTime.now().minusDays(2)));
        Element<HearingBooking> futureHearing = element(testHearing(LocalDateTime.now().plusDays(1)));

        LocalDateTime reListedHearingStartTime = now().plusDays(nextLong(1, 50));
        LocalDateTime reListedHearingEndTime = reListedHearingStartTime.plusDays(nextLong(1, 10));

        HearingCancellationReason adjournmentReason = HearingCancellationReason.builder()
            .reason(TEST_REASON)
            .build();

        CaseData initialCaseData = CaseData.builder()
            .hearingOption(ADJOURN_HEARING)
            .hearingReListOption(RE_LIST_NOW)
            .pastHearingDateList(dynamicList(pastHearing, pastHearingToBeAdjourned))
            .futureHearingDateList(dynamicList(futureHearing))
            .pastAndTodayHearingDateList(dynamicList(
                pastHearingToBeAdjourned.getId(), pastHearing,
                pastHearingToBeAdjourned))
            .hearingDetails(List.of(pastHearing, pastHearingToBeAdjourned, futureHearing))
            .hearingType(CASE_MANAGEMENT)
            .hearingVenue(pastHearingToBeAdjourned.getValue().getVenue())
            .hearingVenueCustom(pastHearingToBeAdjourned.getValue().getVenueCustomAddress())
            .hearingAttendance(pastHearingToBeAdjourned.getValue().getAttendance())
            .hearingStartDate(reListedHearingStartTime)
            .hearingEndDate(reListedHearingEndTime)
            .judgeAndLegalAdvisor(pastHearingToBeAdjourned.getValue().getJudgeAndLegalAdvisor())
            .adjournmentReason(adjournmentReason)
            .noticeOfHearingNotes(pastHearingToBeAdjourned.getValue().getAdditionalNotes())
            .children1(ElementUtils.wrapElements(Child.builder().party(ChildParty.builder().build()).build()))
            .build();

        CaseData updatedCaseData = extractCaseData(postAboutToSubmitEvent(asCaseDetails(initialCaseData)));

        HearingBooking expectedReListedHearing = pastHearingToBeAdjourned.getValue().toBuilder()
            .startDate(reListedHearingStartTime)
            .endDate(reListedHearingEndTime)
            .build();

        Element<HearingBooking> expectedAdjournedHearing = element(
            pastHearingToBeAdjourned.getId(),
            pastHearingToBeAdjourned.getValue().toBuilder()
                .status(HearingStatus.ADJOURNED_AND_RE_LISTED)
                .cancellationReason(adjournmentReason.getReason())
                .build());

        assertThat(updatedCaseData.getHearingDetails()).extracting(Element::getValue)
            .containsExactly(pastHearing.getValue(), futureHearing.getValue(), expectedReListedHearing);
        assertThat(updatedCaseData.getCancelledHearingDetails()).containsExactly(expectedAdjournedHearing);
        assertThat(updatedCaseData.getSelectedHearingId())
            .isIn(findElementsId(expectedReListedHearing, updatedCaseData.getHearingDetails()));
    }

    @Test
    void shouldVacateAndReListHearing() {
        UUID draftCMOId = UUID.randomUUID();

        Element<HearingBooking> pastHearing = element(testHearing(LocalDateTime.now().minusDays(1)));
        Element<HearingBooking> futureHearing = element(testHearing(LocalDateTime.now().plusDays(1)));
        Element<HearingBooking> futureHearingToBeVacated = element(
            testHearing(LocalDateTime.now().plusDays(1), draftCMOId));

        LocalDateTime reListedHearingStartTime = now().plusDays(nextLong(1, 50));
        LocalDateTime reListedHearingEndTime = reListedHearingStartTime.plusDays(nextLong(1, 10));
        LocalDate vacatedHearingDate = now().minusDays(1).toLocalDate();

        HearingCancellationReason vacatedReason = HearingCancellationReason.builder()
            .reason(TEST_REASON)
            .build();

        HearingOrder draftCMO = HearingOrder.builder()
            .hearing(futureHearingToBeVacated.getValue().toLabel())
            .status(CMOStatus.DRAFT).type(HearingOrderType.DRAFT_CMO)
            .build();

        Element<HearingOrdersBundle> hearingOrdersBundleElement = element(
            UUID.randomUUID(), HearingOrdersBundle.builder()
                .hearingId(futureHearingToBeVacated.getId())
                .hearingName(futureHearingToBeVacated.getValue().toLabel())
                .orders(newArrayList(element(draftCMOId, draftCMO)))
                .build());

        CaseData initialCaseData = CaseData.builder()
            .hearingOption(VACATE_HEARING)
            .hearingReListOption(RE_LIST_NOW)
            .vacateHearingDateList(dynamicList(
                futureHearingToBeVacated.getId(), pastHearing, futureHearingToBeVacated, futureHearing))
            .hearingDetails(List.of(pastHearing, futureHearingToBeVacated, futureHearing))
            .hearingType(CASE_MANAGEMENT)
            .hearingVenue(futureHearingToBeVacated.getValue().getVenue())
            .hearingVenueCustom(futureHearingToBeVacated.getValue().getVenueCustomAddress())
            .hearingAttendance(futureHearing.getValue().getAttendance())
            .hearingStartDate(reListedHearingStartTime)
            .hearingEndDate(reListedHearingEndTime)
            .judgeAndLegalAdvisor(futureHearingToBeVacated.getValue().getJudgeAndLegalAdvisor())
            .vacatedReason(vacatedReason)
            .vacatedHearingDate(vacatedHearingDate)
            .noticeOfHearingNotes(futureHearingToBeVacated.getValue().getAdditionalNotes())
            .children1(ElementUtils.wrapElements(Child.builder().party(ChildParty.builder().build()).build()))
            .hearingOrdersBundlesDrafts(newArrayList(hearingOrdersBundleElement))
            .build();

        CaseData updatedCaseData = extractCaseData(postAboutToSubmitEvent(asCaseDetails(initialCaseData)));

        HearingBooking expectedReListedHearing = futureHearingToBeVacated.getValue().toBuilder()
            .startDate(reListedHearingStartTime)
            .endDate(reListedHearingEndTime)
            .caseManagementOrderId(null)
            .build();

        Element<HearingBooking> expectedVacatedHearing = element(
            futureHearingToBeVacated.getId(),
            futureHearingToBeVacated.getValue().toBuilder()
                .caseManagementOrderId(draftCMOId)
                .status(HearingStatus.VACATED_AND_RE_LISTED)
                .cancellationReason(vacatedReason.getReason())
                .vacatedDate(vacatedHearingDate)
                .build());

        assertThat(updatedCaseData.getHearingDetails()).extracting(Element::getValue)
            .containsExactlyInAnyOrder(pastHearing.getValue(), futureHearing.getValue(), expectedReListedHearing);
        assertThat(updatedCaseData.getCancelledHearingDetails()).containsExactly(expectedVacatedHearing);
        assertThat(updatedCaseData.getSelectedHearingId())
            .isIn(findElementsId(expectedReListedHearing, updatedCaseData.getHearingDetails()));
    }

    @Test
    void shouldReListCancelledHearing() {
        Element<HearingBooking> adjournedHearing = element(testHearing(ADJOURNED_TO_BE_RE_LISTED));
        Element<HearingBooking> vacatedHearing = element(testHearing(VACATED_TO_BE_RE_LISTED));

        LocalDateTime reListedHearingStartTime = now().plusDays(nextLong(1, 50));
        LocalDateTime reListedHearingEndTime = reListedHearingStartTime.plusDays(nextLong(1, 10));

        CaseData initialCaseData = CaseData.builder()
            .hearingOption(RE_LIST_HEARING)
            .toReListHearingDateList(dynamicList(adjournedHearing.getId(), adjournedHearing, vacatedHearing))
            .cancelledHearingDetails(List.of(adjournedHearing, vacatedHearing))
            .hearingType(CASE_MANAGEMENT)
            .hearingVenue(adjournedHearing.getValue().getVenue())
            .hearingVenueCustom(adjournedHearing.getValue().getVenueCustomAddress())
            .hearingAttendance(adjournedHearing.getValue().getAttendance())
            .hearingStartDate(reListedHearingStartTime)
            .hearingEndDate(reListedHearingEndTime)
            .judgeAndLegalAdvisor(adjournedHearing.getValue().getJudgeAndLegalAdvisor())
            .noticeOfHearingNotes(adjournedHearing.getValue().getAdditionalNotes())
            .children1(ElementUtils.wrapElements(Child.builder().party(ChildParty.builder().build()).build()))
            .build();

        CaseData updatedCaseData = extractCaseData(postAboutToSubmitEvent(initialCaseData));

        HearingBooking expectedReListedHearing = adjournedHearing.getValue().toBuilder()
            .startDate(reListedHearingStartTime)
            .endDate(reListedHearingEndTime)
            .status(null)
            .build();

        Element<HearingBooking> expectedAdjournedHearing = element(
            adjournedHearing.getId(),
            adjournedHearing.getValue().toBuilder()
                .status(ADJOURNED_AND_RE_LISTED)
                .build());

        assertThat(updatedCaseData.getHearingDetails()).extracting(Element::getValue)
            .containsExactly(expectedReListedHearing);
        assertThat(updatedCaseData.getCancelledHearingDetails())
            .containsExactlyInAnyOrder(expectedAdjournedHearing, vacatedHearing);
        assertThat(updatedCaseData.getSelectedHearingId())
            .isIn(findElementsId(expectedReListedHearing, updatedCaseData.getHearingDetails()));
    }

    @ParameterizedTest
    @EnumSource(value = HearingReListOption.class, names = {"RE_LIST_NOW"}, mode = EnumSource.Mode.EXCLUDE)
    void shouldAdjournHearing(HearingReListOption adjournmentOption) {
        Element<HearingBooking> pastHearingToBeAdjourned = element(testHearing(LocalDateTime.now().minusDays(2)));

        HearingCancellationReason adjournmentReason = HearingCancellationReason.builder()
            .reason(TEST_REASON)
            .build();

        CaseData initialCaseData = CaseData.builder()
            .selectedHearingId(randomUUID())
            .hearingOption(ADJOURN_HEARING)
            .hearingReListOption(adjournmentOption)
            .pastAndTodayHearingDateList(dynamicList(pastHearingToBeAdjourned.getId(), pastHearingToBeAdjourned))
            .hearingDetails(List.of(pastHearingToBeAdjourned))
            .adjournmentReason(adjournmentReason)
            .build();

        CaseData updatedCaseData = extractCaseData(postAboutToSubmitEvent(asCaseDetails(initialCaseData)));

        Element<HearingBooking> expectedAdjournedHearing = element(
            pastHearingToBeAdjourned.getId(),
            pastHearingToBeAdjourned.getValue().toBuilder()
                .status(adjournmentOption == RE_LIST_LATER ? ADJOURNED_TO_BE_RE_LISTED : ADJOURNED)
                .cancellationReason(adjournmentReason.getReason())
                .build());

        assertThat(updatedCaseData.getHearingDetails()).isNull();
        assertThat(updatedCaseData.getCancelledHearingDetails()).containsExactly(expectedAdjournedHearing);
        assertThat(updatedCaseData.getSelectedHearingId()).isNull();
    }

    @ParameterizedTest
    @EnumSource(value = HearingReListOption.class, names = {"RE_LIST_NOW"}, mode = EnumSource.Mode.EXCLUDE)
    void shouldVacateHearing(HearingReListOption adjournmentOption) {
        Element<HearingBooking> futureHearingToBeAdjourned = element(testHearing(LocalDateTime.now().plusDays(2)));
        LocalDate vacatedHearingDate = now().minusDays(1).toLocalDate();

        HearingCancellationReason vacatedReason = HearingCancellationReason.builder()
            .reason(TEST_REASON)
            .build();

        CaseData initialCaseData = CaseData.builder()
            .selectedHearingId(randomUUID())
            .hearingOption(VACATE_HEARING)
            .hearingReListOption(adjournmentOption)
            .vacateHearingDateList(dynamicList(futureHearingToBeAdjourned.getId(), futureHearingToBeAdjourned))
            .hearingDetails(List.of(futureHearingToBeAdjourned))
            .vacatedReason(vacatedReason)
            .vacatedHearingDate(vacatedHearingDate)
            .build();

        CaseData updatedCaseData = extractCaseData(postAboutToSubmitEvent(asCaseDetails(initialCaseData)));

        Element<HearingBooking> expectedVacatedHearing = element(
            futureHearingToBeAdjourned.getId(),
            futureHearingToBeAdjourned.getValue().toBuilder()
                .status(adjournmentOption == RE_LIST_LATER ? VACATED_TO_BE_RE_LISTED : VACATED)
                .cancellationReason(vacatedReason.getReason())
                .vacatedDate(vacatedHearingDate)
                .build());

        assertThat(updatedCaseData.getHearingDetails()).isNull();
        assertThat(updatedCaseData.getCancelledHearingDetails()).containsExactly(expectedVacatedHearing);
        assertThat(updatedCaseData.getSelectedHearingId()).isNull();
    }

}
