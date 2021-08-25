package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;

import static uk.gov.hmcts.reform.fpl.enums.HearingOptions.ADJOURN_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.HearingOptions.VACATE_HEARING;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

class ManageHearingsControllerReListHearingMidEventTest extends ManageHearingsControllerTest {

    ManageHearingsControllerReListHearingMidEventTest() {
        super("manage-hearings");
    }

    @Test
    void shouldPrePopulateNewReListedHearingWithAdjournedHearingDetails() {
        Element<HearingBooking> hearingToBeAdjourned = element(testHearing(now()));
        Element<HearingBooking> otherHearing = element(testHearing(now().minusDays(2)));

        CaseData initialCaseData = CaseData.builder()
            .hearingOption(ADJOURN_HEARING)
            .pastAndTodayHearingDateList(hearingToBeAdjourned.getId())
            .hearingDetails(List.of(otherHearing, hearingToBeAdjourned))
            .build();

        CaseData updatedCaseData = extractCaseData(postMidEvent(initialCaseData, "re-list"));

        assertCurrentHearingReListedFrom(updatedCaseData, hearingToBeAdjourned.getValue());
    }

    @Test
    void shouldPrePopulateNewReListedHearingWithVacatedHearingDetails() {
        Element<HearingBooking> hearingToBeVacated = element(testHearing(now()));
        Element<HearingBooking> otherHearing = element(testHearing(now().minusDays(2)));

        CaseData initialCaseData = CaseData.builder()
            .hearingOption(VACATE_HEARING)
            .toVacateHearingDateList(hearingToBeVacated.getId())
            .hearingDetails(List.of(otherHearing, hearingToBeVacated))
            .build();

        CaseData updatedCaseData = extractCaseData(postMidEvent(initialCaseData, "re-list"));

        assertCurrentHearingReListedFrom(updatedCaseData, hearingToBeVacated.getValue());
    }

}
