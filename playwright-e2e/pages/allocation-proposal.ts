import {type Page, type Locator, expect} from "@playwright/test";

export class AllocationProposal {

  readonly page: Page;
  readonly allocationProposalLink: Locator;
  readonly allocationProposalHeading: Locator;

  public constructor(page: Page) {
    this.page = page;
    this.allocationProposalHeading = page.getByRole('group', { name: 'Allocation proposal' }).getByRole('heading');
    this.allocationProposalLink = page.getByRole('link', { name: 'Allocation proposal' });
  }

async allocationProposalSmokeTest() {

    //expect(this.page.locator('h1').filter({ hasText: 'Allocation proposal' })).toBeVisible(),
    await this.page.getByLabel('Circuit Judge', { exact: true }).check()
    await this.page.getByLabel('Circuit Judge (Section 9)').check();
    await this.page.getByLabel('District Judge').check();
    await this.page.getByLabel('Magistrate').check();
    await this.page.getByLabel('High Court Judge').check();
    await this.page.getByLabel('Circuit Judge', { exact: true }).check();
    await this.page.getByText('*Give reason (Optional)').click();
    await this.page.getByLabel('*Give reason (Optional)').click();
    await this.page.getByLabel('*Give reason (Optional)').fill('test');
    await this.page.getByRole('button', { name: 'Continue' }).click();
    await this.page.getByRole('heading', { name: 'Check your answers' }).click();
    await this.page.getByRole('button', { name: 'Save and continue' }).click();
    }
  }
