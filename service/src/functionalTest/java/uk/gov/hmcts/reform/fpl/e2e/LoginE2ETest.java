package uk.gov.hmcts.reform.fpl.e2e;

import org.junit.Test;

public class LoginE2ETest extends AbstractE2ETest {

    @Test
    public void hmctsAdminCanLogin() {
        loginPage.login(page, COURT_ADMIN);
    }

}
