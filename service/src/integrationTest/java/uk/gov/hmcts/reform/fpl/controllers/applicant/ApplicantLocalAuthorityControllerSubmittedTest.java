package uk.gov.hmcts.reform.fpl.controllers.applicant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.controllers.ApplicantLocalAuthorityController;
import uk.gov.hmcts.reform.fpl.enums.UserRole;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Colleague;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.service.UserService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;

import java.util.List;

import static org.apache.commons.lang3.RandomUtils.nextLong;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.APPLICANTS_DETAILS_UPDATED;
import static uk.gov.hmcts.reform.fpl.enums.ColleagueRole.SOLICITOR;
import static uk.gov.hmcts.reform.fpl.enums.State.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.State.OPEN;
import static uk.gov.hmcts.reform.fpl.enums.State.SUBMITTED;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@WebMvcTest(ApplicantLocalAuthorityController.class)
@OverrideAutoConfiguration(enabled = true)
class ApplicantLocalAuthorityControllerSubmittedTest extends AbstractCallbackTest {

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    @MockBean
    private UserService userService;

    @MockBean
    private NotificationService notificationService;

    ApplicantLocalAuthorityControllerSubmittedTest() {
        super("enter-local-authority");
    }

    @BeforeEach
    void setup() {
        givenSystemUser();
        givenFplService();
        given(userService.hasAnyIdamRolesFrom(List.of(UserRole.HMCTS_SUPERUSER)))
            .willReturn(false);
    }

    @Test
    void shouldUpdateTaskListWhenCaseInOpenState() {
        final CaseData caseDataBefore = CaseData.builder()
            .id(nextLong())
            .caseLocalAuthority(LOCAL_AUTHORITY_1_CODE)
            .state(OPEN)
            .build();

        final CaseData caseData = caseDataBefore.toBuilder()
            .localAuthorities(wrapElements(LocalAuthority.builder()
                .colleagues(wrapElements(solicitor()))
                .build()))
            .build();

        postSubmittedEvent(toCallBackRequest(caseData, caseDataBefore));

        verify(coreCaseDataService, timeout(ASYNC_METHOD_CALL_TIMEOUT)).performPostSubmitCallback(
            eq(caseData.getId()),
            eq("internal-update-task-list"),
            any());
    }

    @Test
    void shouldUpdateCaseSummaryWhenCaseNotInOpenState() {
        final CaseData caseDataBefore = CaseData.builder()
            .id(nextLong())
            .caseLocalAuthority(LOCAL_AUTHORITY_1_CODE)
            .state(SUBMITTED)
            .build();

        final CaseData caseData = caseDataBefore.toBuilder()
            .localAuthorities(wrapElements(LocalAuthority.builder()
                .designated("Yes")
                .colleagues(wrapElements(solicitor()))
                .build()))
            .build();

        postSubmittedEvent(toCallBackRequest(caseData, caseDataBefore));

        verify(coreCaseDataService, timeout(ASYNC_METHOD_CALL_TIMEOUT)).performPostSubmitCallback(
            eq(caseData.getId()),
            eq("internal-update-case-summary"),
            any());
    }

    @Test
    void shouldNotifyApplicantsWhenUpdatedBySuperuser() {
        final CaseData caseDataBefore = CaseData.builder()
            .id(nextLong())
            .caseLocalAuthority(LOCAL_AUTHORITY_1_CODE)
            .state(CASE_MANAGEMENT)
            .build();

        final CaseData caseData = caseDataBefore.toBuilder()
            .localAuthorities(wrapElements(LocalAuthority.builder()
                .designated("Yes")
                .colleagues(wrapElements(solicitor()))
                .build()))
            .build();

        given(userService.hasAnyIdamRolesFrom(List.of(UserRole.HMCTS_SUPERUSER)))
            .willReturn(true);

        postSubmittedEvent(toCallBackRequest(caseData, caseDataBefore));

        verify(notificationService).sendEmail(
            eq(APPLICANTS_DETAILS_UPDATED),
            anyCollection(),
            any(),
            eq(caseData.getId()));
    }

    private static Colleague solicitor() {
        return Colleague.builder()
            .role(SOLICITOR)
            .fullName("John Smith")
            .mainContact("Yes")
            .build();
    }

}
