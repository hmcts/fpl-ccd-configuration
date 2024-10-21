package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.HearingStatus;
import uk.gov.hmcts.reform.fpl.events.SendNoticeOfHearingVacated;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.EventService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

class ManageHearingsControllerVacatedSubmittedTest extends ManageHearingsControllerTest {
    private static final long CASE_ID = 12345L;
    private static final LocalDate VACATED_DATE = LocalDate.of(2024, 1, 1);
    private static final LocalDateTime HEARING_START_DATE = LocalDateTime.of(2050, 5, 20, 13, 0);

    @MockBean
    private EventService eventPublisher;

    @Captor
    private ArgumentCaptor<SendNoticeOfHearingVacated> sendNoticeOfHearingVacatedCaptor;
    @Captor
    private ArgumentCaptor<Object> anyObjectCaptor;

    ManageHearingsControllerVacatedSubmittedTest() {
        super("manage-hearings");
    }

    @Test
    void shouldTriggerSendNoticeOfHearingVacatedEventWhenNotBeingRelisted() {
        HearingBooking hearingVacated =
            testHearing(HEARING_START_DATE, "Hearing Venue", HearingStatus.VACATED_TO_BE_RE_LISTED).toBuilder()
                .cancellationReason("cancel")
                .vacatedDate(VACATED_DATE)
                .build();

        Element<HearingBooking> hearingVacatedElement = element(hearingVacated);

        CaseDetails caseDetails = CaseDetails.builder()
            .jurisdiction(JURISDICTION)
            .caseTypeId(CASE_TYPE)
            .id(CASE_ID)
            .data(Map.of(
                "cancelledHearingId", hearingVacatedElement.getId(),
                "cancelledHearingDetails", List.of(hearingVacatedElement),
                "caseLocalAuthority", LOCAL_AUTHORITY_1_CODE
            ))
            .state("Submitted")
            .build();

        postSubmittedEvent(caseDetails);

        verify(eventPublisher, times(1)).publishEvent(sendNoticeOfHearingVacatedCaptor.capture());

        SendNoticeOfHearingVacated actualSendNoticeOfHearingVacated = sendNoticeOfHearingVacatedCaptor.getValue();
        assertThat(actualSendNoticeOfHearingVacated.getVacatedHearing()).isEqualTo(hearingVacated);
        assertThat(actualSendNoticeOfHearingVacated.isRelisted()).isFalse();
    }

    @Test
    void shouldTriggerSendNoticeOfHearingVacatedEventWhenBeingRelisted() {
        HearingBooking hearingRelisted = testHearing(HEARING_START_DATE, "Hearing Venue");

        HearingBooking hearingVacated =
            testHearing(HEARING_START_DATE, "Hearing Venue", HearingStatus.VACATED_TO_BE_RE_LISTED).toBuilder()
                .cancellationReason("cancel")
                .vacatedDate(VACATED_DATE)
                .build();

        Element<HearingBooking> hearingRelistedElement = element(hearingRelisted);
        Element<HearingBooking> hearingVacatedElement = element(hearingVacated);

        CaseDetails caseDetails = CaseDetails.builder()
            .jurisdiction(JURISDICTION)
            .caseTypeId(CASE_TYPE)
            .id(CASE_ID)
            .data(Map.of(
                "selectedHearingId", hearingRelistedElement.getId(),
                "hearingDetails", List.of(hearingRelistedElement),
                "cancelledHearingId", hearingVacatedElement.getId(),
                "cancelledHearingDetails", List.of(hearingVacatedElement),
                "caseLocalAuthority", LOCAL_AUTHORITY_1_CODE
            ))
            .state("Submitted")
            .build();

        postSubmittedEvent(caseDetails);

        verify(eventPublisher, times(1)).publishEvent(sendNoticeOfHearingVacatedCaptor.capture());

        SendNoticeOfHearingVacated actualSendNoticeOfHearingVacated = sendNoticeOfHearingVacatedCaptor.getValue();
        assertThat(actualSendNoticeOfHearingVacated.getVacatedHearing()).isEqualTo(hearingVacated);
        assertThat(actualSendNoticeOfHearingVacated.isRelisted()).isTrue();
    }

    @Test
    void shouldNotTriggerSendNoticeOfHearingVacatedEventWhenHousekeeping() {
        HearingBooking hearingHouseKeep =
            testHearing(HEARING_START_DATE, "Hearing Venue", HearingStatus.VACATED_TO_BE_RE_LISTED).toBuilder()
                .housekeepReason("housekeep")
                .vacatedDate(VACATED_DATE)
                .build();

        Element<HearingBooking> hearingHouseKeepElement = element(hearingHouseKeep);

        CaseDetails caseDetails = CaseDetails.builder()
            .jurisdiction(JURISDICTION)
            .caseTypeId(CASE_TYPE)
            .id(CASE_ID)
            .data(Map.of(
                "cancelledHearingId", hearingHouseKeepElement.getId(),
                "cancelledHearingDetails", List.of(hearingHouseKeepElement),
                "caseLocalAuthority", LOCAL_AUTHORITY_1_CODE
            ))
            .state("Submitted")
            .build();

        postSubmittedEvent(caseDetails);

        verify(eventPublisher, times(2)).publishEvent(anyObjectCaptor.capture());
        assertThat(anyObjectCaptor.getAllValues().stream()
            .map(eventPublished -> eventPublished instanceof SendNoticeOfHearingVacated)
            .toList()).containsOnly(false);
    }
}
