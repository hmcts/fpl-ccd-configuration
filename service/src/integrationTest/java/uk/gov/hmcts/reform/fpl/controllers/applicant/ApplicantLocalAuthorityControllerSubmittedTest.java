package uk.gov.hmcts.reform.fpl.controllers.applicant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.controllers.ApplicantLocalAuthorityController;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Colleague;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;

import static org.apache.commons.lang3.RandomUtils.nextLong;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;
import static uk.gov.hmcts.reform.fpl.enums.ColleagueRole.SOLICITOR;
import static uk.gov.hmcts.reform.fpl.enums.State.OPEN;
import static uk.gov.hmcts.reform.fpl.enums.State.SUBMITTED;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@WebMvcTest(ApplicantLocalAuthorityController.class)
@OverrideAutoConfiguration(enabled = true)
class ApplicantLocalAuthorityControllerSubmittedTest extends AbstractCallbackTest {

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    ApplicantLocalAuthorityControllerSubmittedTest() {
        super("enter-local-authority");
    }

    @BeforeEach
    void setup() {
        givenSystemUser();
        givenFplService();
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

        verify(coreCaseDataService).performPostSubmitCallback(
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

        verify(coreCaseDataService).performPostSubmitCallback(
            eq(caseData.getId()),
            eq("internal-update-case-summary"),
            any());
    }

    private static Colleague solicitor() {
        return Colleague.builder()
            .role(SOLICITOR)
            .fullName("John Smith")
            .mainContact("Yes")
            .build();
    }

}
