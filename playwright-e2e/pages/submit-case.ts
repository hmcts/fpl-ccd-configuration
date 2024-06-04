import { type Page, type Locator, expect } from "@playwright/test";
import { BasePage } from "./base-page";

export class SubmitCase extends BasePage{
 // readonly page: Page;
  readonly declarationHeading: Locator;
  readonly statementAgree: Locator;
  readonly applicationSentHeading: Locator;
  readonly closeReturnToCase: Locator;
  readonly caseInfoHeading: Locator;
  readonly paymentAmountLocator: Locator;
  readonly paymentAmountText: Locator;

  public constructor(page: Page) {
    super(page);
    this.declarationHeading = page.getByText('Declaration');
    this.statementAgree = page.getByLabel('I agree with this statement');
    this.applicationSentHeading = page.getByRole('heading', { name: 'Application sent' });
    this.closeReturnToCase = page.getByRole('button', { name: 'Close and Return to case' });
    this.caseInfoHeading = page.getByRole('heading', { name: 'Case information' });
    this.paymentAmountLocator = page.locator('dd').filter({ hasText: '£' });
    this.paymentAmountText = page.getByText('£');
  }

  async submitCaseSmokeTest() {
    //first page
    await expect(this.declarationHeading).toHaveText('Declaration');
    await this.statementAgree.check();
    await expect(this.paymentAmountLocator).toHaveText('£2,437.00');
    await this.clickSubmit();
    //second page
    await expect(this.checkYourAnswersHeader).toHaveText('Check your answers');
    await expect(this.declarationHeading).toHaveText('Declaration');
    await expect(this.paymentAmountText).toHaveText('£2,437.00');
    await this.clickSubmit();
    await expect(this.applicationSentHeading).toHaveText('Application sent');
    await this.closeReturnToCase.click();
  }
}
