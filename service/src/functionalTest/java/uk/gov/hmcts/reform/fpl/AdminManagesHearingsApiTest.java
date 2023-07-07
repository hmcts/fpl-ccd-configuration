package uk.gov.hmcts.reform.fpl;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.fpl.enums.HearingOptions;
import uk.gov.hmcts.reform.fpl.enums.HearingType;
import uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement;
import uk.gov.hmcts.reform.fpl.enums.hearing.HearingAttendance;
import uk.gov.hmcts.reform.fpl.model.CallbackResponse;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.service.DocumentService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HER_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.hearing.HearingPresence.IN_PERSON;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

public class AdminManagesHearingsApiTest extends AbstractApiTest {

    //public static final String INPUT_FILE = "admin-manage-hearings/input-case-details.json";
    public static final String INPUT_FILE =  "admin-manage-hearings/input-case-data-new.json";
    //public static final String EXPECTED_FILE = "admin-manage-hearings/%s/expected.txt";
    private final LocalDate todaysDate = LocalDate.now();
    private final LocalDateTime currentDateTime = LocalDateTime.now();
    private CaseData startingCaseData;

    @Autowired
    private DocumentService documentService;

    @Test
    public void adminCreatesHearing() {
        startingCaseData = createCase(INPUT_FILE, LA_SWANSEA_USER_1);
        callAboutToSubmit(startingCaseData);
    }

    private void callAboutToSubmit(CaseData caseData) {
        LocalDateTime hearingStartDate = currentDateTime.plusDays(5);
        LocalDateTime hearingEndDate = hearingStartDate.plusDays(5);
        HearingBooking hearingBooking = HearingBooking.builder()
            .startDate(hearingStartDate)
            .endDate(hearingEndDate)
            .type(HearingType.CASE_MANAGEMENT)
            .venue("96")
            .presence(IN_PERSON)
            .hearingJudgeLabel("Her Honour Judge Moley")
            .allocatedJudgeLabel("Her Honour Judge Moley")
            .attendance(List.of(HearingAttendance.IN_PERSON))
            .attendanceDetails("Court 7")
            .preAttendanceDetails("30 minutes before hearing")
            .additionalNotes("please make sure that you attend this...")
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                .judgeFullName("Her Honour Judge Moley")
                .judgeTitle(HER_HONOUR_JUDGE)
                .legalAdvisorName("Rupert Robert")
                .judgeEmailAddress("moley@example.com")
                .build())
            .build();

        CaseData updatedCase = caseData.toBuilder()
            .hearingOption(HearingOptions.NEW_HEARING)
            .hearingStartDate(hearingBooking.getStartDate())
            .hearingEndDate(hearingBooking.getEndDate())
            .judgeAndLegalAdvisor(hearingBooking.getJudgeAndLegalAdvisor())
            .hearingType(hearingBooking.getType())
            .sendNoticeOfHearing("No")
            .build();

        HearingBooking expectedHearingBooking = HearingBooking.builder()
            .type(HearingType.CASE_MANAGEMENT)
            .preAttendanceDetails("1 hour before the hearing")
            .startDate(hearingBooking.getStartDate())
            .endDate(hearingBooking.getEndDate())
            .endDateDerived("No")
            .allocatedJudgeLabel("Her Honour Judge Reed")
            .legalAdvisorLabel("Rupert Robert")
            .others(emptyList())
            .othersNotified("")
            .hearingJudgeLabel("")
            .judgeAndLegalAdvisor(hearingBooking.getJudgeAndLegalAdvisor())
            .translationRequirements(LanguageTranslationRequirement.NO)
            .build();

        CallbackResponse response = callback(updatedCase, COURT_ADMIN, "manage-hearings/about-to-submit");

        assertThat(response.getCaseData().getHearingDetails().get(1).getValue()).isEqualTo(expectedHearingBooking);

    }
}
