import { type Page, type Locator, expect } from "@playwright/test";

export class StartApplication {
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
  readonly allocationProposalFinished: Locator;
  readonly allocationProposalLink: Locator;
  readonly allocationProposalHeading: Locator;
  readonly addApplicationDocsHeading: Locator;
  readonly uploadDocumentsLink: Locator;
  readonly upLoadDocsInProgress: Locator;
  readonly respondentsDetailsLink: Locator;

  public constructor(page: Page) {
    this.page = page;
    this.addApplicationDetailsHeading = page.getByRole("heading", { name: "Add application details", });
    this.ordersAndDirectionsSoughtLink = page.getByRole("heading", { name: "Orders and directions sought", });
    this.factorsAffectingParentingLink = page.getByRole("heading", { name: "Factors affecting parenting", });
    this.hearingUrgencyLink = page.getByRole("link", { name: "Hearing urgency", });
    this.hearingUrgencyHeader = page.getByRole("heading", { name: "Hearing urgency", });
    this.groundsForTheApplicationLink = page.getByRole("link", { name: "Grounds for the application", });
    this.groundsForTheApplicationHeading = page.getByRole("heading", { name: "Grounds for the application", });
    this.groundsForTheApplicationHasBeenUpdatedFinished = page.locator( 'xpath=//*[@id="taskListLabel"]/dt/ccd-markdown/div/markdown/div/p[4]/img', );
    this.riskAndHarmToChildrenLink = page.getByRole("link", { name: "Risk and harm to children", });
    this.allocationProposalFinished = page.locator('p:has(a[text()="Allocation proposal"]) > img[title="Finished"]');
    this.allocationProposalHeading = page.getByRole("group", { name: "Allocation proposal" }).getByRole("heading");
    this.allocationProposalLink = page.getByRole("link", { name: "Allocation proposal", });
    this.uploadDocumentsLink = page.getByRole("link", { name: "Upload documents", });
    this.addApplicationDocsHeading = page.getByRole("heading", { name: "Add application documents", });
    this.upLoadDocsInProgress = page.locator('p:has(a[text()="Upload documents"]) > img[title="In progress"]');
    this.respondentsDetailsLink = page.getByRole('link', { name: 'Respondents\' details' });
  }

  async addApplicationDetails() {
    await expect(this.addApplicationDetailsHeading).toBeVisible();
  }

  async ordersAndDirectionsSought() {
    await this.ordersAndDirectionsSoughtLink.isVisible();
    await this.ordersAndDirectionsSoughtLink.click();
  }

  async hearingUrgency() {
    await this.hearingUrgencyLink.isVisible();
    await this.hearingUrgencyLink.click();
    await expect(this.hearingUrgencyHeader).toBeVisible();
  }

  async groundsForTheApplication() {
    await this.groundsForTheApplicationLink.isVisible();
    await this.groundsForTheApplicationLink.click();
    await expect(this.groundsForTheApplicationHeading).toBeVisible();
  }

  async groundsForTheApplicationHasBeenUpdated() {
    await expect(this.groundsForTheApplicationHasBeenUpdatedFinished)
      .toBeVisible;
  }

  async riskAndHarmToChildren() {
    await this.riskAndHarmToChildrenLink.isVisible();
    await this.riskAndHarmToChildrenLink.click();
  }

  async addApplicationDocuments() {
    await this.uploadDocumentsLink.isVisible();
    await this.uploadDocumentsLink.click();
  }

  async addApplicationDocumentsInProgress() {
    await this.upLoadDocsInProgress.isVisible();
  }

  async respondentDetails() {
    await this.respondentsDetailsLink.isVisible();
    await this.respondentsDetailsLink.click();
  }

  async allocationProposal() {
    await this.allocationProposalLink.isVisible();
    await this.allocationProposalLink.click();
  }

  async allocationProposalHasBeenUpdated() {
    await expect(this.allocationProposalFinished).toBeVisible;
  }
}
