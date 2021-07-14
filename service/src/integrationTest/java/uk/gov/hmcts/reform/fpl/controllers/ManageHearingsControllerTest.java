package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.fpl.enums.HearingStatus;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.hearing.HearingAttendance.IN_PERSON;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.asDynamicList;

@OverrideAutoConfiguration(enabled = true)
@WebMvcTest(ManageHearingsController.class)
abstract class ManageHearingsControllerTest extends AbstractCallbackTest {

    ManageHearingsControllerTest(String eventName) {
        super(eventName);
    }

    void assertCurrentHearingReListedFrom(CaseData caseData, HearingBooking cancelledHearing) {
        assertThat(caseData.getHearingStartDate()).isNull();
        assertThat(caseData.getHearingEndDate()).isNull();
        assertThat(caseData.getPreviousHearingVenue()).isNull();

        assertThat(caseData.getHearingType()).isEqualTo(cancelledHearing.getType());
        assertThat(caseData.getJudgeAndLegalAdvisor()).isEqualTo(cancelledHearing.getJudgeAndLegalAdvisor());
        assertThat(caseData.getHearingVenue()).isEqualTo(cancelledHearing.getVenue());
        assertThat(caseData.getHearingVenueCustom()).isEqualTo(cancelledHearing.getVenueCustomAddress());
    }

    HearingBooking testHearing(LocalDateTime startDate) {
        return testHearing(startDate, "96");
    }

    HearingBooking testHearing(LocalDateTime startDate, UUID cmoId) {
        return testHearing(startDate, "96", null, cmoId);
    }

    HearingBooking testHearing(HearingStatus status) {
        return testHearing(now().minusDays(2), "96", status);
    }

    HearingBooking testHearing(LocalDateTime startDate, String venue) {
        return testHearing(startDate, venue, null);
    }

    HearingBooking testHearing(LocalDateTime startDate, String venue, HearingStatus status) {
        return testHearing(startDate, venue, status, null);
    }

    HearingBooking testHearing(LocalDateTime startDate, String venue, HearingStatus status, UUID cmoId) {
        return HearingBooking.builder()
            .type(CASE_MANAGEMENT)
            .status(status)
            .startDate(startDate)
            .endDate(startDate.plusDays(1))
            .hearingJudgeLabel("Her Honour Judge Judy")
            .legalAdvisorLabel("")
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                .judgeTitle(JudgeOrMagistrateTitle.HER_HONOUR_JUDGE)
                .judgeLastName("Judy")
                .build())
            .venueCustomAddress(Address.builder().build())
            .caseManagementOrderId(cmoId)
            .venue(venue)
            .attendance(List.of(IN_PERSON))
            .build();
    }

    @SafeVarargs
    final Object dynamicList(UUID selectedId, Element<HearingBooking>... hearings) {
        DynamicList dynamicList = asDynamicList(Arrays.asList(hearings), selectedId, HearingBooking::toLabel);
        return mapper.convertValue(dynamicList, new TypeReference<Map<String, Object>>() {
        });
    }

    @SafeVarargs
    final Object dynamicList(Element<HearingBooking>... hearings) {
        return this.dynamicList(null, hearings);
    }

}
