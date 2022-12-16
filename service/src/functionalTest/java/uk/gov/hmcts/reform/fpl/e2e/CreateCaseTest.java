package uk.gov.hmcts.reform.fpl.e2e;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.SelectOption;
import com.microsoft.playwright.options.WaitForSelectorState;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.fpl.e2e.pages.LoginPage;
import uk.gov.hmcts.reform.fpl.util.TestConfiguration;

import static uk.gov.hmcts.reform.fpl.api.ApiTestService.LA_SWANSEA_USER_1;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CreateCaseTest extends AbstractE2ETest {

    final TestConfiguration testConfiguration;
    final LoginPage loginPage;

    Long caseId;

    @Test
    @Order(1)
    public void createInitialCase() {
        page.navigate(testConfiguration.getXuiUrl());
        loginPage.login(page, LA_SWANSEA_USER_1);

        page.waitForSelector("xuilib-loading-spinner",
            new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN));

        page.click("text=Create case");

        page.selectOption("#cc-jurisdiction", new SelectOption().setLabel("Public Law"));
        page.selectOption("#cc-case-type", new SelectOption().setLabel("Public Law Applications"));
        page.selectOption("#cc-event", new SelectOption().setLabel("Start application"));
        goToNextPage("Start");

        page.fill("#caseName", "Test case");
        goToNextPage();

        saveAndContinue();

        page.waitForSelector("text=Test case");

        var url = page.url().split("/");
        caseId = Long.parseLong(url[url.length - 1].split("#")[0]);
    }

}
