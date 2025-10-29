import { expect, type Locator, type Page } from "@playwright/test";
import { urlConfig } from "../settings/urls";
import { BasePage } from "./base-page";

export class SignInPage extends BasePage {
    readonly page: Page;
    readonly mourl: string;
    readonly emailInputLocator: Locator;
    readonly passwordInputLocator: Locator;
    readonly signinButtonLocator: Locator;
    readonly dropdownLocator: Locator;
    readonly applyLocator: Locator;
    readonly signoutButton: Locator;
    readonly analyticCookie: Locator;
    readonly hideMessage: Locator;

    public constructor(page: Page) {
        super(page);
        this.page = page;
        this.mourl = urlConfig.manageOrgURL;
        this.emailInputLocator = page.getByLabel("Email address");
        this.passwordInputLocator = page.getByLabel("Password");
        this.signinButtonLocator = page.getByRole("button", { name: "Sign in" });
        this.dropdownLocator = this.page.locator('h2[aria-label="Filters"].heading-h2',);
        this.applyLocator = page.getByRole("button", { name: "Apply" });
        this.signoutButton = page.getByText('Sign out');
        this.analyticCookie = page.getByRole('button', { name: 'Accept analytics cookies' });
        this.hideMessage = page.getByText('Hide message');
    }

    async visit() {
        await this.page.goto(`${urlConfig.frontEndBaseURL}`);
    }

    async navigateTOCaseDetails(caseNumber: string) {
        await this.page.goto(`${urlConfig.frontEndBaseURL}/cases/case-details/${urlConfig.jurisdiction}/${urlConfig.caseType}/${caseNumber}`);
        await this.page.waitForLoadState();
    }

    async login(email: string, password: string) {
        await this.emailInputLocator.fill(email);
        await this.passwordInputLocator.fill(password);
        await this.signinButtonLocator.click();
        await this.page.waitForLoadState();
        await this.isSignedIn();
        if (await this.analyticCookie.isVisible()) {
            await this.analyticCookie.click();
        }
        await this.isSignedIn();
        const count = await this.hideMessage.count();

        for (let i = 0; i < count; ++i) {
            await this.hideMessage.nth(0).click();
        }

    }

    async isSignedIn() {
        await expect(this.signoutButton).toBeVisible();
    }

    async logout() {
        await this.signoutButton.click();
        await expect(this.emailInputLocator).toBeVisible();
    }
}
