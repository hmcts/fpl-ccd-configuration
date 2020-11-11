package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.enums.HearingReListOption;
import uk.gov.hmcts.reform.fpl.enums.HearingStatus;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.HearingCancellationReason;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisData;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;
import uk.gov.hmcts.reform.fpl.utils.TestDataHelper;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.apache.commons.lang3.RandomUtils.nextLong;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.enums.HearingOptions.ADJOURN_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.HearingOptions.EDIT_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.HearingOptions.VACATE_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.HearingReListOption.RE_LIST_NOW;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.ISSUE_RESOLUTION;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createPopulatedChildren;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.document;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.asDynamicList;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.findElementsId;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocmosisDocument;

@ActiveProfiles("integration-test")
@WebMvcTest(ManageHearingsController.class)
@OverrideAutoConfiguration(enabled = true)
class ManageHearingsControllerAboutToSubmitTest extends AbstractControllerTest {

    @MockBean
    private DocmosisDocumentGeneratorService docmosisDocumentGeneratorService;

    @MockBean
    private UploadDocumentService uploadDocumentService;

    ManageHearingsControllerAboutToSubmitTest() {
        super("manage-hearings");
    }

    @Test
    void shouldAddNewHearingToHearingDetailsListWhenAddHearingSelected() {
        HearingBooking newHearing = hearing(now().plusDays(2));
        CaseData initialCaseData = CaseData.builder()
            .hearingType(newHearing.getType())
            .hearingVenue(newHearing.getVenue())
            .hearingVenueCustom(newHearing.getVenueCustomAddress())
            .hearingStartDate(newHearing.getStartDate())
            .hearingEndDate(newHearing.getEndDate())
            .judgeAndLegalAdvisor(newHearing.getJudgeAndLegalAdvisor())
            .noticeOfHearingNotes(newHearing.getAdditionalNotes())
            .build();

        CaseData updatedCaseData = extractCaseData(postAboutToSubmitEvent(asCaseDetails(initialCaseData)));

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

        HearingBooking newHearing = hearing(now().plusDays(2));
        CaseData initialCaseData = CaseData.builder()
            .children1(createPopulatedChildren(now().toLocalDate()))
            .caseLocalAuthority("example")
            .sendNoticeOfHearing("Yes")
            .hearingType(newHearing.getType())
            .hearingVenue(newHearing.getVenue())
            .hearingVenueCustom(newHearing.getVenueCustomAddress())
            .hearingStartDate(newHearing.getStartDate())
            .hearingEndDate(newHearing.getEndDate())
            .judgeAndLegalAdvisor(newHearing.getJudgeAndLegalAdvisor())
            .noticeOfHearingNotes(newHearing.getAdditionalNotes())
            .build();

        CaseData updatedCaseData = extractCaseData(postAboutToSubmitEvent(asCaseDetails(initialCaseData)));

        HearingBooking hearingAfterCallback = newHearing.toBuilder().noticeOfHearing(
            DocumentReference.buildFromDocument(document)).build();

        assertThat(updatedCaseData.getHearingDetails()).extracting(Element::getValue)
            .containsExactly(hearingAfterCallback);
        assertThat(updatedCaseData.getFirstHearingFlag()).isNull();
    }

    @Test
    void shouldUpdateExistingHearingInHearingDetailsListWhenEditHearingSelected() {
        HearingBooking existingHearing = hearing(now().plusDays(2));

        Element<HearingBooking> hearingElement = element(existingHearing);

        CaseData initialCaseData = CaseData.builder()
            .hearingOption(EDIT_HEARING)
            .hearingDateList(dynamicList(hearingElement.getId(), hearingElement))
            .hearingDetails(List.of(hearingElement))
            .hearingType(ISSUE_RESOLUTION)
            .hearingVenue(existingHearing.getVenue())
            .hearingVenueCustom(existingHearing.getVenueCustomAddress())
            .hearingStartDate(existingHearing.getStartDate())
            .hearingEndDate(existingHearing.getEndDate())
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
        Element<HearingBooking> pastHearing = element(hearing(LocalDateTime.now().minusDays(1)));
        Element<HearingBooking> pastHearingToBeAdjourned = element(hearing(LocalDateTime.now().minusDays(2)));
        Element<HearingBooking> futureHearing = element(hearing(LocalDateTime.now().plusDays(1)));

        LocalDateTime reListedHearingStartTime = now().plusDays(nextLong(1, 50));
        LocalDateTime reListedHearingEndTime = reListedHearingStartTime.plusDays(nextLong(1, 10));

        HearingCancellationReason adjournmentReason = HearingCancellationReason.builder()
            .reason("Test reason")
            .build();

        CaseData initialCaseData = CaseData.builder()
            .hearingOption(ADJOURN_HEARING)
            .hearingReListOption(RE_LIST_NOW)
            .hearingDateList(dynamicList(futureHearing))
            .pastAndTodayHearingDateList(dynamicList(
                pastHearingToBeAdjourned.getId(), pastHearing,
                pastHearingToBeAdjourned))
            .hearingDetails(List.of(pastHearing, pastHearingToBeAdjourned, futureHearing))
            .hearingType(CASE_MANAGEMENT)
            .hearingVenue(pastHearingToBeAdjourned.getValue().getVenue())
            .hearingVenueCustom(pastHearingToBeAdjourned.getValue().getVenueCustomAddress())
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
        Element<HearingBooking> pastHearing = element(hearing(LocalDateTime.now().minusDays(1)));
        Element<HearingBooking> futureHearing = element(hearing(LocalDateTime.now().plusDays(1)));
        Element<HearingBooking> futureHearingToBeVacated = element(hearing(LocalDateTime.now().plusDays(1)));

        LocalDateTime reListedHearingStartTime = now().plusDays(nextLong(1, 50));
        LocalDateTime reListedHearingEndTime = reListedHearingStartTime.plusDays(nextLong(1, 10));

        HearingCancellationReason vacatedReason = HearingCancellationReason.builder()
            .reason("Test reason")
            .build();

        CaseData initialCaseData = CaseData.builder()
            .hearingOption(VACATE_HEARING)
            .hearingReListOption(RE_LIST_NOW)
            .hearingDateList(dynamicList(futureHearing))
            .futureAndTodayHearingDateList(dynamicList(
                futureHearingToBeVacated.getId(), futureHearing,
                futureHearingToBeVacated))
            .hearingDetails(List.of(pastHearing, futureHearingToBeVacated, futureHearing))
            .hearingType(CASE_MANAGEMENT)
            .hearingVenue(futureHearingToBeVacated.getValue().getVenue())
            .hearingVenueCustom(futureHearingToBeVacated.getValue().getVenueCustomAddress())
            .hearingStartDate(reListedHearingStartTime)
            .hearingEndDate(reListedHearingEndTime)
            .judgeAndLegalAdvisor(futureHearingToBeVacated.getValue().getJudgeAndLegalAdvisor())
            .vacatedReason(vacatedReason)
            .noticeOfHearingNotes(futureHearingToBeVacated.getValue().getAdditionalNotes())
            .children1(ElementUtils.wrapElements(Child.builder().party(ChildParty.builder().build()).build()))
            .build();

        CaseData updatedCaseData = extractCaseData(postAboutToSubmitEvent(asCaseDetails(initialCaseData)));

        HearingBooking expectedReListedHearing = futureHearingToBeVacated.getValue().toBuilder()
            .startDate(reListedHearingStartTime)
            .endDate(reListedHearingEndTime)
            .build();

        Element<HearingBooking> expectedVacatedHearing = element(
            futureHearingToBeVacated.getId(),
            futureHearingToBeVacated.getValue().toBuilder()
                .status(HearingStatus.VACATED_AND_RE_LISTED)
                .cancellationReason(vacatedReason.getReason())
                .build());

        assertThat(updatedCaseData.getHearingDetails()).extracting(Element::getValue)
            .containsExactly(pastHearing.getValue(), futureHearing.getValue(), expectedReListedHearing);
        assertThat(updatedCaseData.getCancelledHearingDetails()).containsExactly(expectedVacatedHearing);
        assertThat(updatedCaseData.getSelectedHearingId())
            .isIn(findElementsId(expectedReListedHearing, updatedCaseData.getHearingDetails()));
    }

    @ParameterizedTest
    @EnumSource(value = HearingReListOption.class, names = {"RE_LIST_NOW"}, mode = EnumSource.Mode.EXCLUDE)
    void shouldAdjournHearing(HearingReListOption adjournmentOption) {
        Element<HearingBooking> pastHearingToBeAdjourned = element(hearing(LocalDateTime.now().minusDays(2)));

        HearingCancellationReason adjournmentReason = HearingCancellationReason.builder()
            .reason("Test reason")
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
                .status(HearingStatus.ADJOURNED)
                .cancellationReason(adjournmentReason.getReason())
                .build());

        assertThat(updatedCaseData.getHearingDetails()).isNull();
        assertThat(updatedCaseData.getCancelledHearingDetails()).containsExactly(expectedAdjournedHearing);
        assertThat(updatedCaseData.getSelectedHearingId()).isNull();
    }

    @ParameterizedTest
    @EnumSource(value = HearingReListOption.class, names = {"RE_LIST_NOW"}, mode = EnumSource.Mode.EXCLUDE)
    void shouldVacateHearing(HearingReListOption adjournmentOption) {
        Element<HearingBooking> futureHearingToBeAdjourned = element(hearing(LocalDateTime.now().plusDays(2)));

        HearingCancellationReason vacatedReason = HearingCancellationReason.builder()
            .reason("Test reason")
            .build();

        CaseData initialCaseData = CaseData.builder()
            .selectedHearingId(randomUUID())
            .hearingOption(VACATE_HEARING)
            .hearingReListOption(adjournmentOption)
            .futureAndTodayHearingDateList(dynamicList(futureHearingToBeAdjourned.getId(), futureHearingToBeAdjourned))
            .hearingDetails(List.of(futureHearingToBeAdjourned))
            .vacatedReason(vacatedReason)
            .build();

        CaseData updatedCaseData = extractCaseData(postAboutToSubmitEvent(asCaseDetails(initialCaseData)));

        Element<HearingBooking> expectedVacatedHearing = element(
            futureHearingToBeAdjourned.getId(),
            futureHearingToBeAdjourned.getValue().toBuilder()
                .status(HearingStatus.VACATED)
                .cancellationReason(vacatedReason.getReason())
                .build());

        assertThat(updatedCaseData.getHearingDetails()).isNull();
        assertThat(updatedCaseData.getCancelledHearingDetails()).containsExactly(expectedVacatedHearing);
        assertThat(updatedCaseData.getSelectedHearingId()).isNull();
    }

    private static HearingBooking hearing(LocalDateTime startDate) {
        return HearingBooking.builder()
            .type(CASE_MANAGEMENT)
            .startDate(startDate)
            .endDate(startDate.plusDays(1))
            .hearingJudgeLabel("Her Honour Judge Judy")
            .legalAdvisorLabel("")
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                .judgeTitle(JudgeOrMagistrateTitle.HER_HONOUR_JUDGE)
                .judgeLastName("Judy")
                .build())
            .venueCustomAddress(Address.builder().build())
            .venue("96")
            .build();
    }

    @SafeVarargs
    private Object dynamicList(UUID selectedId, Element<HearingBooking>... hearings) {
        DynamicList dynamicList = asDynamicList(Arrays.asList(hearings), selectedId, HearingBooking::toLabel);
        return mapper.convertValue(dynamicList, new TypeReference<Map<String, Object>>() {
        });
    }

    @SafeVarargs
    private Object dynamicList(Element<HearingBooking>... hearings) {
        return dynamicList(null, hearings);
    }

}
