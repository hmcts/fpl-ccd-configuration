import {type Page, type Locator, expect} from "@playwright/test";
import { BasePage } from "./base-page";

export class AllocationProposal extends BasePage{
  readonly allocationProposalLink: Locator;
  readonly allocationProposalHeading: Locator;

  public constructor(page: Page) {
    super(page);
    this.allocationProposalHeading = page.getByRole('group', { name: 'Allocation proposal' }).getByRole('heading');
    this.allocationProposalLink = page.getByRole('link', { name: 'Allocation proposal' });
  }

async allocationProposalSmokeTest() {
    await expect(this.page.locator('h1').filter({ hasText: 'Allocation proposal' })).toHaveText('Allocation proposal');
    await this.page.getByLabel('Circuit Judge', { exact: true }).check();
    await this.page.getByLabel('Circuit Judge (Section 9)').check();
    await this.page.getByLabel('District Judge').check();
    await this.page.getByLabel('Magistrate').check();
    await this.page.getByLabel('High Court Judge').check();
    await this.page.getByLabel('Circuit Judge', { exact: true }).check();
    await this.page.getByLabel('*Give reason (Optional)').fill('test');
    await this.clickContinue();
    await this.checkYourAnsAndSubmit();
    }
  }
