import { type Page, type Locator, expect } from "@playwright/test";
import { BasePage } from "./base-page";
import { join } from "path";
import { ApplicantDetails } from "./applicant-details";

export class SubmitCase extends BasePage{
  readonly page: Page;
  readonly declarationHeading: Locator;
  readonly statementAgree: Locator;
  readonly applicationSentHeading: Locator;
  readonly closeReturnToCase: Locator;
  readonly caseInfoHeading: Locator;
  readonly teamManagerNameText: Locator;
  readonly paymentAmountLocator: Locator;
  readonly paymentAmountText: Locator;

  public constructor(page: Page) {
    super(page);
    this.declarationHeading = page.getByText('Declaration');
    this.statementAgree = page.getByLabel('I agree with this statement');
    this.applicationSentHeading = page.getByRole('heading', { name: 'Application sent' });
    this.closeReturnToCase = page.getByRole('button', { name: 'Close and Return to case' });
    this.caseInfoHeading = page.getByRole('heading', { name: 'Case information' });
    this.teamManagerNameText = page.getByText(`believe that the facts stated in this application are true`);
    this.paymentAmountLocator = page.locator('dd').filter({ hasText: '£' });
    this.paymentAmountText = page.getByText('£');
  }

  async submitCaseSmokeTest() {
    //first page
    await this.declarationHeading.isVisible();
    await this.teamManagerNameText.isVisible();
    await this.statementAgree.check();
    await this.paymentAmountLocator.isVisible();
    await this.clickSubmit();
    //second page
    await this.checkYourAnswersHeader.isVisible();
    await this.declarationHeading.isVisible();
    await this.teamManagerNameText.isVisible();
    await this.paymentAmountText.isVisible();
    await this.clickSubmit();
    await this.applicationSentHeading.isVisible();
    await this.closeReturnToCase.click();
    await this.caseInfoHeading.isVisible();
  }
}
