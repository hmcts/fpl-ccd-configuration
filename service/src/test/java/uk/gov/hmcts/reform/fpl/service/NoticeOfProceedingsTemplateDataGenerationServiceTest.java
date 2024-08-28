package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.OrderStatus;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisHearingBooking;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisNoticeOfProceeding;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisImages.COURT_SEAL;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.COURT_NAME;
import static uk.gov.hmcts.reform.fpl.model.configuration.Language.ENGLISH;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

class NoticeOfProceedingsTemplateDataGenerationServiceTest {

    private static final String FAMILY_MAN_CASE_NUMBER = "Fam Case Num";
    private static final List<Element<Applicant>> APPLICANTS = wrapElements(Applicant.builder()
        .party(ApplicantParty.builder()
            .organisationName("some organisation")
            .build())
        .build());

    private static final HearingBooking HEARING_1 = HearingBooking.builder()
        .startDate(LocalDateTime.of(2021, 1, 1, 14, 0, 0))
        .build();
    private static final HearingBooking HEARING_2 = HearingBooking.builder()
        .startDate(LocalDateTime.of(2021, 1, 3, 14, 0, 0))
        .build();
    private static final List<Element<HearingBooking>> HEARINGS = wrapElements(HEARING_1, HEARING_2);
    private static final long CASE_NUMBER = 1234123412341234L;
    private static final String FORMATTED_CASE_NUMBER = "1234-1234-1234-1234";

    private final Time time = new FixedTimeConfiguration().fixedDateTime(LocalDateTime.of(2021, 3, 13, 13, 13, 13));
    private final CaseDataExtractionService extractionService = mock(CaseDataExtractionService.class);
    private final CourtService courtService = mock(CourtService.class);

    private NoticeOfProceedingsTemplateDataGenerationService underTest;

    @BeforeEach
    void setup() {
        underTest = new NoticeOfProceedingsTemplateDataGenerationService(courtService, extractionService, time);

        when(extractionService.getHearingBookingData(HEARING_1)).thenReturn(
            DocmosisHearingBooking.builder()
                .hearingDate("1 January 2021")
                .hearingTime("2:00pm - 4:30pm")
                .hearingVenue("somewhere")
                .hearingAttendance("In person, remote - phone call")
                .hearingAttendanceDetails("Call: +44 777 777 777")
                .preHearingAttendance("1:00pm")
                .hearingJudgeTitleAndName("should be removed")
                .hearingLegalAdvisorName("should also be removed")
                .build()
        );

        when(courtService.getCourtName(any())).thenReturn(COURT_NAME);
    }

    @Test
    void shouldApplySentenceFormattingWhenMultipleChildrenExistOnCase() {
        CaseData caseData = getCaseData(
            buildChild("Bran", "Stark"), buildChild("Sansa", "Stark"), buildChild("Jon", "Snow")
        );

        DocmosisNoticeOfProceeding templateData = underTest.getTemplateData(caseData);

        assertThat(templateData.getChildrenNames()).isEqualTo("Bran Stark, Sansa Stark and Jon Snow");
    }

    @Test
    void shouldNotApplySentenceFormattingWhenOnlyOneChildExistsOnCase() {
        CaseData caseData = getCaseData(buildChild("Bran", "Stark"));

        DocmosisNoticeOfProceeding templateData = underTest.getTemplateData(caseData);

        assertThat(templateData.getChildrenNames()).isEqualTo("Bran Stark");
    }

    @Test
    void shouldBuildExpectedTemplateData() {
        CaseData caseData = getCaseData(
            buildChild("Bran", "Stark"), buildChild("Sansa", "Stark"), buildChild("Jon", "Snow")
        );

        when(courtService.getCourtSeal(caseData, OrderStatus.SEALED))
                .thenReturn(COURT_SEAL.getValue(ENGLISH));

        DocmosisNoticeOfProceeding templateData = underTest.getTemplateData(caseData);

        DocmosisNoticeOfProceeding expectedData = DocmosisNoticeOfProceeding.builder()
            .courtName("Family Court")
            .familyManCaseNumber(FAMILY_MAN_CASE_NUMBER)
            .ccdCaseNumber(FORMATTED_CASE_NUMBER)
            .applicantName("some organisation")
            .orderTypes("Care order")
            .childrenNames("Bran Stark, Sansa Stark and Jon Snow")
            .hearingBooking(DocmosisHearingBooking.builder()
                .hearingDate("1 January 2021")
                .hearingVenue("somewhere")
                .hearingAttendance("In person, remote - phone call")
                .hearingAttendanceDetails("Call: +44 777 777 777")
                .preHearingAttendance("1:00pm")
                .hearingTime("2:00pm - 4:30pm")
                .build())
            .todaysDate("13 March 2021")
            .crest("[userImage:crest.png]")
            .courtseal("[userImage:familycourtseal.png]")
            .build();

        assertThat(templateData).isEqualTo(expectedData);

        verify(courtService).getCourtName(caseData);
    }

    private CaseData getCaseData(Child... children) {
        return CaseData.builder()
            .id(CASE_NUMBER)
            .familyManCaseNumber(FAMILY_MAN_CASE_NUMBER)
            .caseLocalAuthority(LOCAL_AUTHORITY_1_CODE)
            .children1(wrapElements(children))
            .hearingDetails(HEARINGS)
            .orders(Orders.builder()
                .orderType(List.of(CARE_ORDER))
                .build())
            .applicants(APPLICANTS)
            .build();
    }

    private Child buildChild(String firstName, String lastName) {
        return Child.builder()
            .party(ChildParty.builder()
                .firstName(firstName)
                .lastName(lastName)
                .build())
            .build();
    }
}
