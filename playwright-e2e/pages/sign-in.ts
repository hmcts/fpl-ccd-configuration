import { type Page, type Locator, expect } from "@playwright/test";
import { UrlConfig } from "../settings/urls";

export class SignInPage {
  readonly page: Page;
  readonly url: string = UrlConfig.aatUrl;
  readonly emailInputLocator: Locator;
  readonly passwordInputLocator: Locator;
  readonly signinButtonLocator: Locator;
  readonly dropdownLocator: Locator;
  readonly applyLocator: Locator;

  public constructor(page: Page) {
    this.page = page;

    this.emailInputLocator = page.getByLabel("Email address");
    this.passwordInputLocator = page.getByLabel("Password");
    this.signinButtonLocator = page.getByRole("button", { name: "Sign in" });
    this.dropdownLocator = this.page.locator(
      'h2[aria-label="Filters"].heading-h2',
    );
    this.applyLocator = page.getByRole("button", { name: "Apply" });
  }

  async visit() {
    await this.page.goto(this.url);
  }

  async login(email: string, password: string) {
    await this.emailInputLocator.fill(email);
    await this.passwordInputLocator.fill(password);
    await this.signinButtonLocator.click();
  }

  async isSignedIn() {
    await this.applyLocator.click({ timeout: 65000 });
    await expect(this.applyLocator).toBeVisible();
  }
}
