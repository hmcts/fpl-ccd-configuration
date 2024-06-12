import { type Page, type Locator, expect } from "@playwright/test";
import { urlConfig } from "../settings/urls";
import {BasePage} from "./base-page";

export class SignInPage extends BasePage{
  readonly page: Page;
  readonly url: string;
  readonly mourl: string;
  readonly emailInputLocator: Locator;
  readonly passwordInputLocator: Locator;
  readonly signinButtonLocator: Locator;
  readonly dropdownLocator: Locator;
  readonly applyLocator: Locator;
  readonly logoutButton: Locator;

  public constructor(page: Page) {
    super(page);
    this.page = page;
    this.url = urlConfig.frontEndBaseURL;
    this.mourl = urlConfig.manageOrgURL;
    this.emailInputLocator = page.getByLabel("Email address");
    this.passwordInputLocator = page.getByLabel("Password");
    this.signinButtonLocator = page.getByRole("button", { name: "Sign in" });
    this.dropdownLocator = this.page.locator('h2[aria-label="Filters"].heading-h2',);
    this.applyLocator = page.getByRole("button", { name: "Apply" });
    this.logoutButton = page.getByText('Sign out');
  }

  async visit(url:string = this.url) {
    await this.page.goto(url);
  }
  async navigateTOCaseDetails(caseNumber:string) {
    await this.page.goto(`${urlConfig.frontEndBaseURL}case-details/${caseNumber}`);
    await this.page.waitForLoadState("load");
    await  this.tabNavigation('History');
  }

  async login(email: string, password: string) {
    await this.emailInputLocator.fill(email);
    await this.passwordInputLocator.fill(password);
    await this.signinButtonLocator.click();
    await this.page.getByRole("button", {name: 'Reject analytics cookies'}).click();
  }

  async isSignedIn() {
    await this.applyLocator.click();
    await expect(this.applyLocator).toBeVisible();
  }

  async logout(){
      await this.logoutButton.click();
      await expect(this.emailInputLocator).toBeVisible();
  }

  async isLoggedInMO(){
       await expect(this.page.getByRole('heading', { name: 'Organisation' })).toBeVisible();
  }
}

