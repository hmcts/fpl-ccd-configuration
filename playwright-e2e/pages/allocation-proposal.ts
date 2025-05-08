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

  async allocationProposalSmokeTest() {
    await expect.soft(this.allocationProposalHeading).toBeVisible();
    await this.radioButton.getByLabel('Circuit Judge').click();
    await this.reasonsForRecommendation.fill('Test');
    await this.continue.click();
    await this.saveAndContinue.click();
    await expect(this.page.getByText('has been updated with event:')).toBeVisible();
  }
}
