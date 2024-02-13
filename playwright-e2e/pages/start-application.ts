import { type Page, type Locator, expect } from "@playwright/test";

export class StartApplication {
  respondentsDetailsLink: Locator;
  respondentsDetailsHeader: Locator;
  respondentsDetailsHasBeenUpdated() {
    throw new Error("Method not implemented.");
  }

  readonly page: Page;
  readonly addApplicationDetailsHeading: Locator;
  readonly changeCaseNameLink: Locator;
  readonly ordersAndDirectionsSoughtLink: Locator;
  readonly factorsAffectingParentingLink: Locator;
  readonly hearingUrgencyLink: Locator;
  readonly addGroundsForTheApplicationHeading: Locator;
  readonly groundsForTheApplicationLink: Locator;
  readonly riskAndHarmToChildrenLink: Locator;
  readonly hearingUrgencyHeader: Locator;
  readonly groundsForTheApplicationHeading: Locator;
  readonly groundsForTheApplicationHasBeenUpdatedFinished: Locator;

  readonly respondentsDetailsHeading: Locator;
  readonly respondentsDetailsHasBeenUpdatedFinished: Locator;
  readonly allocationProposalHasBeenUpdatedFinished: Locator;
  readonly allocationProposalLink: Locator;
  readonly allocationProposalHeading: Locator;

  public constructor(page: Page) {
    this.page = page;
    this.addApplicationDetailsHeading = page.getByRole("heading", { name: "Add application details"} );
    this.ordersAndDirectionsSoughtLink = page.getByRole("heading", { name: "Orders and directions sought",});
    this.factorsAffectingParentingLink = page.getByRole("heading", { name: "Factors affecting parenting",});
    this.hearingUrgencyLink = page.getByRole('link', { name: 'Hearing urgency' });
    this.hearingUrgencyHeader = page.getByRole('heading', { name: 'Hearing urgency' });
    this.groundsForTheApplicationLink = page.getByRole('link', { name: 'Grounds for the application' });
    this.groundsForTheApplicationHeading = page.getByRole('heading', { name: 'Grounds for the application' });
    this.groundsForTheApplicationHasBeenUpdatedFinished = page.locator('xpath=//*[@id="taskListLabel"]/dt/ccd-markdown/div/markdown/div/p[4]/img');
    this.riskAndHarmToChildrenLink = page.getByRole('link', { name: 'Risk and harm to children' }
    this.respondentsDetailsLink = page.getByRole('link',{ name: 'Responsdents details'});
    this.respondentsDetailsHeader = page.getByRole('heading', { name: 'Respondents detals' });


    this.allocationProposalHasBeenUpdatedFinished = page.locator('p').filter({ hasText: 'Allocation proposal' }).getByRole('img');
    this.allocationProposalHeading = page.getByRole('group', { name: 'Allocation proposal' }).getByRole('heading');
    this.allocationProposalLink = page.getByRole('link', { name: 'Allocation proposal' });

  }

  async addApplicationDetails(){
    await expect (this.addApplicationDetailsHeading).toBeVisible();
  }

  async ordersAndDirectionsSought() {
    await this.ordersAndDirectionsSoughtLink.isVisible();
    await this.ordersAndDirectionsSoughtLink.click();
  }

  async hearingUrgency() {
    await this.hearingUrgencyLink.isVisible();
    await this.hearingUrgencyLink.click();
    await expect (this.hearingUrgencyHeader).toBeVisible();
  }

  async groundsForTheApplication() {
    await this.groundsForTheApplicationLink.isVisible();
    await this.groundsForTheApplicationLink.click();
    await expect (this.groundsForTheApplicationHeading).toBeVisible();
  }

  async groundsForTheApplicationHasBeenUpdated() {
    await expect (this.groundsForTheApplicationHasBeenUpdatedFinished).toBeVisible;
  }

  async riskAndHarmToChildren() {
    await this.riskAndHarmToChildrenLink.isVisible();
    await this.riskAndHarmToChildrenLink.click();
  }

  async respondentsDetails() {
    await this.respondentsDetailsLink.isVisible();
    await this.respondentsDetailsLink.click();
    await expect (this.respondentsDetailsHeading).toBeVisible();

  }

  async allocationProposal(){
    await this.allocationProposalLink.isVisible();
    await this.allocationProposalLink.click();
  }

  async allocationProposalHasBeenUpdated(){
    await expect (this.allocationProposalHasBeenUpdatedFinished).toBeVisible;
  }
}
