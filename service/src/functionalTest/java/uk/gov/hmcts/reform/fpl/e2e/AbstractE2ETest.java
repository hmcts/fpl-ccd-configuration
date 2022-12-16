package uk.gov.hmcts.reform.fpl.e2e;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.fpl.api.ApiTestService;
import uk.gov.hmcts.reform.fpl.e2e.pages.CaseDetailsPage;
import uk.gov.hmcts.reform.fpl.e2e.pages.LoginPage;
import uk.gov.hmcts.reform.fpl.service.AuthenticationService;
import uk.gov.hmcts.reform.fpl.service.CaseConverter;
import uk.gov.hmcts.reform.fpl.service.CaseService;
import uk.gov.hmcts.reform.fpl.service.DocumentService;
import uk.gov.hmcts.reform.fpl.service.EmailService;
import uk.gov.hmcts.reform.fpl.service.PaymentService;
import uk.gov.hmcts.reform.fpl.service.ScenarioService;
import uk.gov.hmcts.reform.fpl.util.ObjectMapperApiTestConfig;
import uk.gov.hmcts.reform.fpl.util.TestConfiguration;

import java.nio.file.Paths;

@ActiveProfiles("test-${env:local}")
@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {
    LoginPage.class,
    CaseDetailsPage.class,
    ApiTestService.class,
    AuthenticationService.class,
    CaseService.class,
    DocumentService.class,
    EmailService.class,
    PaymentService.class,
    ScenarioService.class,
    TestConfiguration.class,
    ObjectMapperApiTestConfig.class,
    CaseConverter.class
})
abstract class AbstractE2ETest {

    static Playwright playwright;
    static Browser browser;

    BrowserContext context;
    Page page;

    @BeforeAll
    static void beforeAll() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch();
    }

    @AfterAll
    static void afterAll() {
        playwright.close();
    }

    @BeforeEach
    void beforeEach() {
        context = browser.newContext(new Browser.NewContextOptions().setIgnoreHTTPSErrors(true));
        page = context.newPage();
    }

    @AfterEach
    void afterEach() {
        page.screenshot(new Page.ScreenshotOptions()
            .setPath(Paths.get("test-results/e2e/screenshots/screenshot.png")));

        context.close();
    }

    public void goToNextPage(String label) {
        page.locator("button:has-text(\"" + label + "\")").click();
    }

    public void goToNextPage() {
        goToNextPage("Continue");
    }

    public void saveAndContinue() {
        page.locator("button:has-text(\"Save and continue\")").click();
        page.waitForSelector(".hmcts-banner--success");
    }

    public void openTab(Long caseId, String tabName) {
        page.locator(".mat-tab-label-content:has-text(\"" + tabName + "\")").click();
    }

}
