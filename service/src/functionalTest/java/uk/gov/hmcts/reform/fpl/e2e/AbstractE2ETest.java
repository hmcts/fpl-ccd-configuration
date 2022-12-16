package uk.gov.hmcts.reform.fpl.e2e;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.AbstractApiTest;
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
@ContextConfiguration(classes = {
    TestConfiguration.class,
    CaseConverter.class,
    ObjectMapperApiTestConfig.class,
    AuthenticationService.class,
    ScenarioService.class,
    CaseService.class,
    DocumentService.class,
    PaymentService.class,
    EmailService.class,
    LoginPage.class,
    CaseDetailsPage.class
})
@Slf4j
@RunWith(SpringIntegrationSerenityRunner.class)
@SpringBootTest
public abstract class AbstractE2ETest extends AbstractApiTest {

    @Rule
    public TestName name = new TestName();

    @Autowired
    protected TestConfiguration testConfiguration;

    @Autowired
    protected LoginPage loginPage;

    @Autowired
    protected CaseDetailsPage caseDetailsPage;

    static Playwright playwright;
    static Browser browser;

    BrowserContext context;
    Page page;

    @BeforeClass
    public static void beforeAll() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch();
    }

    @AfterClass
    public static void afterAll() {
        playwright.close();
    }

    @Before
    public void beforeEach() {
        context = browser.newContext();
        page = context.newPage();
    }

    @After
    public void afterEach() {
        page.screenshot(new Page.ScreenshotOptions()
            .setPath(Paths.get("test-results/e2e/screenshots/" + name.getMethodName() + ".png")));

        context.close();
    }

    public void goToNextPage() {
        page.locator("button:has-text(\"Continue\")").click();
    }

    public void saveAndContinue() {
        page.locator("button:has-text(\"Save and continue\")").click();
        page.waitForSelector(".hmcts-banner--success");
    }

    public void openTab(Long caseId, String tabName) {
        page.locator(".mat-tab-label-content:has-text(\"" + tabName + "\")").click();
    }

}
