import { type Page, type Locator, expect } from "@playwright/test";
import { BasePage } from "./base-page";

export class SubmitCase extends BasePage{
  readonly page: Page;
  readonly statementAgree: Locator;
  readonly applicationSentHeading: Locator;
  readonly closeReturnToCase: Locator;
  readonly caseInfoHeading: Locator;

  public constructor(page: Page) {
    super(page);
    this.statementAgree = page.getByLabel('I agree with this statement');
    this.applicationSentHeading = page.getByRole('heading', { name: 'Application sent' });
    this.closeReturnToCase = page.getByRole('button', { name: 'Close and Return to case' });
    this.caseInfoHeading = page.getByRole('heading', { name: 'Case information' });
  }

  async submitCaseSmokeTest() {
    await this.statementAgree.check();
    await this.clickSubmit();
    await this.clickSubmit();
    await expect(this.applicationSentHeading).toBeVisible;
    await this.closeReturnToCase.click();
    await expect(this.caseInfoHeading).toBeVisible;
  }
}
