package uk.gov.hmcts.reform.fpl.service.email.content;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.HearingVenue;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.notify.hearing.NoticeOfHearingTemplate;
import uk.gov.hmcts.reform.fpl.service.CaseDataExtractionService;
import uk.gov.hmcts.reform.fpl.service.HearingVenueLookUpService;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;
import uk.gov.hmcts.reform.fpl.utils.TestDataHelper;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.enums.TabUrlAnchor.HEARINGS;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ContextConfiguration(classes = {NoticeOfHearingEmailContentProvider.class})
class NoticeOfHearingEmailContentProviderTest extends AbstractEmailContentProviderTest {
    private static final String VENUE = "Crown Building, Aberdare Hearing Centre, Aberdare, CF44 7DW";
    private static final String HEARING_TIME = "some time";
    private static final String PRE_HEARING_ATTENDANCE = "Pre hearing attendance";
    private static final String RESPONDENT_LAST_NAME = "Wilson";
    private static final byte[] APPLICATION_BINARY = TestDataHelper.DOCUMENT_CONTENT;
    private static final String ENCODED_BINARY = Base64.getEncoder().encodeToString(APPLICATION_BINARY);
    private static final String FAMILY_MAN_ID = "123";
    private static final String START_DATE = "12 December 2021";
    private static final String CHILD_LAST_NAME = "Sanguinius";
    private static final CaseData CASE_DATA = mock(CaseData.class);
    private static final HearingBooking HEARING_BOOKING = mock(HearingBooking.class);

    @MockBean
    private CaseDataExtractionService extractionService;
    @MockBean
    private HearingVenueLookUpService venueLookUp;
    @MockBean
    private EmailNotificationHelper helper;
    @Autowired
    private NoticeOfHearingEmailContentProvider underTest;

    @BeforeEach
    void mocks() {
        DocumentReference noticeOfHearing = mock(DocumentReference.class);
        HearingVenue venue = mock(HearingVenue.class);
        List<Element<Child>> children = wrapElements(mock(Child.class));

        when(CASE_DATA.getFamilyManCaseNumber()).thenReturn(FAMILY_MAN_ID);
        when(CASE_DATA.getId()).thenReturn(Long.valueOf(CASE_REFERENCE));
        when(CASE_DATA.getAllChildren()).thenReturn(children);
        when(CASE_DATA.getAllRespondents()).thenReturn(wrapElements(Respondent.builder()
            .party(RespondentParty.builder().lastName(RESPONDENT_LAST_NAME).build())
            .build()
        ));

        when(HEARING_BOOKING.getType()).thenReturn(CASE_MANAGEMENT);
        when(HEARING_BOOKING.getNoticeOfHearing()).thenReturn(noticeOfHearing);
        when(HEARING_BOOKING.getStartDate()).thenReturn(LocalDateTime.of(2021, 12, 12, 0, 0, 0));
        when(HEARING_BOOKING.getPreAttendanceDetails()).thenReturn(PRE_HEARING_ATTENDANCE);

        when(venueLookUp.getHearingVenue(HEARING_BOOKING)).thenReturn(venue);
        when(venueLookUp.buildHearingVenue(venue)).thenReturn(VENUE);

        when(extractionService.getHearingTime(HEARING_BOOKING)).thenReturn(HEARING_TIME);

        when(helper.getEldestChildLastName(children)).thenReturn(CHILD_LAST_NAME);

        when(noticeOfHearing.getBinaryUrl()).thenReturn("/testUrl");
        when(documentDownloadService.downloadDocument("/testUrl")).thenReturn(APPLICATION_BINARY);
    }

    @Test
    void shouldReturnExpectedNewHearingTemplateWithDigitalPreference() {
        NoticeOfHearingTemplate expectedTemplateData = buildExpectedDigitalTemplate();

        assertThat(underTest.buildNewNoticeOfHearingNotification(CASE_DATA, HEARING_BOOKING, DIGITAL_SERVICE))
            .isEqualTo(expectedTemplateData);
    }

    @Test
    void shouldReturnExpectedNewHearingTemplateWithEmailPreference() {
        NoticeOfHearingTemplate expectedTemplateData = buildExpectedEmailTemplate();

        assertThat(underTest.buildNewNoticeOfHearingNotification(CASE_DATA, HEARING_BOOKING, EMAIL))
            .isEqualTo(expectedTemplateData);
    }

    private NoticeOfHearingTemplate buildCommonParameters() {
        return NoticeOfHearingTemplate.builder()
            .hearingType(CASE_MANAGEMENT.getLabel().toLowerCase())
            .hearingDate(START_DATE)
            .hearingVenue(VENUE)
            .hearingTime(HEARING_TIME)
            .preHearingTime(PRE_HEARING_ATTENDANCE)
            .familyManCaseNumber(FAMILY_MAN_ID)
            .lastName(RESPONDENT_LAST_NAME)
            .childLastName(CHILD_LAST_NAME)
            .build();
    }

    private NoticeOfHearingTemplate buildExpectedDigitalTemplate() {
        return buildCommonParameters().toBuilder()
            .caseUrl(caseUrl(CASE_REFERENCE, HEARINGS))
            .documentLink(DOC_URL)
            .digitalPreference(YES.getValue())
            .build();
    }

    private NoticeOfHearingTemplate buildExpectedEmailTemplate() {
        return buildCommonParameters().toBuilder()
            .caseUrl("")
            .documentLink(
                new HashMap<>() {{
                    put("retention_period", null);
                    put("filename", null);
                    put("confirm_email_before_download", null);
                    put("file", ENCODED_BINARY);
                }}
            )
            .digitalPreference(NO.getValue())
            .build();
    }
}
