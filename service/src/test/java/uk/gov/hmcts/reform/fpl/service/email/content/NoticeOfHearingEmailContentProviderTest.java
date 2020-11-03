package uk.gov.hmcts.reform.fpl.service.email.content;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.notify.hearing.NoticeOfHearingTemplate;
import uk.gov.hmcts.reform.fpl.service.CaseDataExtractionService;
import uk.gov.hmcts.reform.fpl.service.HearingVenueLookUpService;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;
import uk.gov.hmcts.reform.fpl.utils.TestDataHelper;

import java.time.LocalDateTime;
import java.time.format.FormatStyle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.NotifyAttachedDocumentLinkHelper.generateAttachedDocumentLink;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@ContextConfiguration(classes = {NoticeOfHearingEmailContentProvider.class, CaseDataExtractionService.class,
    HearingVenueLookUpService.class, LookupTestConfig.class, FixedTimeConfiguration.class})
class NoticeOfHearingEmailContentProviderTest extends AbstractEmailContentProviderTest {

    @Autowired
    private NoticeOfHearingEmailContentProvider noticeOfHearingEmailContentProvider;

    @Autowired
    private CaseDataExtractionService caseDataExtractionService;

    @Autowired
    private Time time;

    private CaseData caseData;
    private HearingBooking hearingBooking;
    private static final byte[] APPLICATION_BINARY = TestDataHelper.DOCUMENT_CONTENT;
    private static final String FAMILY_MAN_ID = "123";

    @BeforeEach
    void setUp() {

        LocalDateTime futureDate = time.now().plusDays(1);
        caseData = buildCaseDetails();
        hearingBooking = buildHearingBooking(futureDate, futureDate.plusDays(1));
        given(documentDownloadService.downloadDocument(anyString())).willReturn(APPLICATION_BINARY);
    }

    @Test
    void shouldReturnExpectedNewHearingTemplateWithDigitalPreference() {
        NoticeOfHearingTemplate expectedTemplateData = buildExpectedTemplate(hearingBooking, YES);

        assertThat(noticeOfHearingEmailContentProvider
            .buildNewNoticeOfHearingNotification(caseData, hearingBooking, DIGITAL_SERVICE))
            .isEqualToComparingFieldByField(expectedTemplateData);
    }

    @Test
    void shouldReturnExpectedNewHearingTemplateWithEmailPreference() {
        NoticeOfHearingTemplate expectedTemplateData = buildExpectedTemplate(hearingBooking, NO);

        assertThat(noticeOfHearingEmailContentProvider
            .buildNewNoticeOfHearingNotification(caseData, hearingBooking, EMAIL))
            .isEqualToComparingFieldByField(expectedTemplateData);
    }

    private HearingBooking buildHearingBooking(LocalDateTime startDate, LocalDateTime endDate) {
        return HearingBooking.builder()
            .type(CASE_MANAGEMENT)
            .startDate(startDate)
            .venue("Venue")
            .endDate(endDate)
            .noticeOfHearing(testDocumentReference())
            .build();
    }

    private NoticeOfHearingTemplate buildExpectedTemplate(HearingBooking hearingBooking,
                                                          YesNo hasDigitalPreference) {
        return NoticeOfHearingTemplate.builder()
            .hearingType(CASE_MANAGEMENT.getLabel().toLowerCase())
            .hearingDate(formatLocalDateToString(hearingBooking.getStartDate().toLocalDate(), FormatStyle.LONG))
            .hearingVenue("Crown Building, Aberdare Hearing Centre, Aberdare, CF44 7DW")
            .hearingTime(caseDataExtractionService.getHearingTime(hearingBooking))
            .preHearingTime(caseDataExtractionService.extractPrehearingAttendance(hearingBooking))
            .caseUrl(hasDigitalPreference.equals(YES) ? caseUrl(CASE_REFERENCE, "HearingTab") : "")
            .familyManCaseNumber(FAMILY_MAN_ID)
            .documentLink(generateAttachedDocumentLink(APPLICATION_BINARY)
                .map(JSONObject::toMap)
                .orElse(null))
            .digitalPreference(hasDigitalPreference.getValue())
            .respondentLastName("Wilson")
            .build();
    }

    private CaseData buildCaseDetails() {
        return CaseData.builder()
            .id(Long.valueOf(CASE_REFERENCE))
            .respondents1(wrapElements(Respondent.builder()
                .party(RespondentParty.builder().lastName("Wilson").build())
                .build()))
            .familyManCaseNumber(FAMILY_MAN_ID)
            .build();

    }
}
