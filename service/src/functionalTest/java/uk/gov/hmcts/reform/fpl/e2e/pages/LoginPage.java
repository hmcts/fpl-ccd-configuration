package uk.gov.hmcts.reform.fpl.e2e.pages;

import com.microsoft.playwright.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.User;
import uk.gov.hmcts.reform.fpl.util.TestConfiguration;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LoginPage {

    private final TestConfiguration testConfiguration;

    public void login(Page page, User user) {
        page.navigate(testConfiguration.getXuiUrl());
        page.fill("#username", user.getName());
        page.fill("#password", "Password12");
        page.locator("input:has-text(\"Sign in\")").click();
        page.waitForSelector("exui-header");

    }
}
