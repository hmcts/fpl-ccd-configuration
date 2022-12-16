package uk.gov.hmcts.reform.fpl.e2e;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.fpl.e2e.pages.LoginPage;

import static uk.gov.hmcts.reform.fpl.api.ApiTestService.COURT_ADMIN;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LoginE2ETestService extends AbstractE2ETest {

    final LoginPage loginPage;

    @Test
    void hmctsAdminCanLogin() {
        loginPage.login(page, COURT_ADMIN);
    }

}
