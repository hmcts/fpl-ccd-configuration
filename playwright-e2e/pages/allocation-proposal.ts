import { type Page, type Locator, expect } from "@playwright/test";

export class AllocationProposal {

  readonly page: Page;
  readonly allocationProposalLink: Locator;
  readonly allocationProposalHeading: Locator;
  readonly radioButton: Locator;
  readonly reasonsForRecommendation: Locator;
  readonly continue: Locator;
  readonly saveAndContinue: Locator;

  public constructor(page: Page) {
    this.page = page;
    this.allocationProposalHeading = page.getByRole('group', { name: 'Allocation proposal' }).getByRole('heading');
    this.allocationProposalLink = page.getByRole('link', { name: 'Allocation proposal' });
    this.radioButton = page.getByRole('group', { name: 'Which level of judge do you recommend for this case' });
    this.reasonsForRecommendation = page.getByLabel('Reasons for recommendation');
    this.continue = page.getByRole('button', { name: 'Continue' });
    this.saveAndContinue = page.getByRole('button', { name: 'Save and continue' });
  }

  async allocationProposalSmokeTest(): Promise<void> {
      await this.radioButton.getByLabel('Circuit Judge').click();
      await this.reasonsForRecommendation.fill('Test');
      await Promise.all([
          this.page.waitForResponse(response =>
              response.url().includes('validate') &&
              response.request().method() === 'POST' &&
              response.status() === 200
          ),
          this.continue.click()
      ]);
      await Promise.all([
         this.page.waitForResponse(response =>
             response.url().includes('/get') &&
             response.request().method() === 'GET' &&
             response.status() === 200
         ),
          this.saveAndContinue.click()
      ]);
  }


}
