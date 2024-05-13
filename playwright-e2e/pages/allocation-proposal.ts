import {type Page, type Locator, expect} from "@playwright/test";
import {BasePage} from "./base-page";

export class AllocationProposal extends BasePage{

  readonly page: Page;
  // readonly continue: Locator;
  // readonly checkYourAnswers: Locator;
  // readonly saveAndContinue: Locator;
  // readonly giveReasonTextBox: Locator;
  readonly allocationProposalLink: Locator;
  readonly allocationProposalHeading: Locator;

  public constructor(page: Page) {
    super(page);
      this.page = page;
    this.allocationProposalHeading = page.getByRole('group', { name: 'Allocation proposal' }).getByRole('heading');
    this.allocationProposalLink = page.getByRole('link', { name: 'Allocation proposal' });
  }

async allocationProposalSmokeTest() {
    await expect(this.allocationProposalHeading).toBeVisible();
   // await this.page.getByRole('heading', { name: 'Add application details' }).isVisible();
    await this.page.getByLabel('Circuit Judge', { exact: true }).check();
    await this.page.getByLabel('Circuit Judge (Section 9)').check();
    await this.page.getByLabel('District Judge').check();
    await this.page.getByLabel('Magistrate').check();
    await this.page.getByLabel('High Court Judge').check();
    await this.page.getByLabel('Circuit Judge', { exact: true }).check();
   // await this.page.getByText('*Give reason (Optional)').click();
   // await this.page.getByLabel('*Give reason (Optional)').click();
    await this.page.getByLabel('*Give reason (Optional)').fill('test');
    await this.clickContinue();
    await this.checkYourAnsAndSubmit();
    // await this.page.getByRole('button', { name: 'Continue' }).click();
    // await this.page.getByRole('heading', { name: 'Check your answers' }).click();
    // await this.page.getByRole('button', { name: 'Save and continue' }).click();
    }
  }
