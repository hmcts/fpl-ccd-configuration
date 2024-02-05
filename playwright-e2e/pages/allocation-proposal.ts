import { type Page, type Locator, expect } from "@playwright/test";
import test from "node:test";

export class AllocationProposal {
  //static AllocationProposalHeading() {
    //throw new Error("Method not implemented.");
 // }
 // static AlocationProposalHeading: any;
 // static   alocationProposalHeading() {throw new Error("Method not implemented.");
  //}
  readonly page: Page;
  readonly AllocationProposalLink: Locator;
  readonly AllocationProposalHeading: Locator;
  readonly continue: Locator;
  readonly checkYourAnswers: Locator;
  readonly saveAndContinue: Locator;
  readonly  giveReasonTextBox: Locator;
  static allocationProposalNeeded: any;
  allocationProposalLink: any;

  public constructor(page: Page) {
    this.page = page;
    this.AllocationProposalHeading = page.getByRole("heading", {name: "AllocationProposal needed",});
    this.AllocationProposalLink = page.locator(".govuk-template__body.js-enabled");
    this.allocationProposalNeeded();
  }

async allocationProposalNeeded() {
    await this.page.getByRole('heading', { name: 'Add application details' }).isVisible();
    await this.page.getByRole('link', { name: 'Allocation proposal' }).click();
    await this.page.getByLabel('Circuit Judge', { exact: true }).check();
    await this.page.getByLabel('Circuit Judge', { exact: true }).check();
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
      async riskAndHarmToChildren() {
        await this.allocationProposalLink.isVisible();
        await this.allocationProposalLink.click();
      }
    }

      



    


























   
  

  







































































































































