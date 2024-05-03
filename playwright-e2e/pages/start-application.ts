import { type Page, type Locator, expect } from "@playwright/test";

export class StartApplication {
  readonly page: Page;
  readonly addApplicationDetailsHeading: Locator;
  readonly ordersAndDirectionsSoughtLink: Locator;
  readonly factorsAffectingParentingLink: Locator;
  readonly hearingUrgencyLink: Locator;
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
  readonly applicantDetailsLink: Locator;
  readonly childDetailsLink: Locator;
  readonly childDetailsUpdated: Locator;
  readonly respondentsDetailsLink: Locator;
  readonly applicantDetailsUpdated: Locator;
  readonly welshLanguageRequirements: Locator;
  readonly welshLanguageReqFinished: Locator;
  readonly otherProceedingsLink: Locator;
  readonly c1WithSupplement: Locator;
  readonly c1WithSupplementFinished: Locator;
  readonly internationalElementsHeading: Locator;
  readonly courtServicesNeeded: Locator;
  readonly submitApplicationLink: Locator;
  
  public constructor(page: Page) {
    this.page = page;
    this.addApplicationDetailsHeading = page.getByRole("heading", { name: "Add application details", });
    this.ordersAndDirectionsSoughtLink = page.getByRole("heading", { name: "Orders and directions sought", });
    this.factorsAffectingParentingLink = page.getByRole("heading", { name: "Factors affecting parenting", });
    this.hearingUrgencyLink = page.getByRole("link", { name: "Hearing urgency", });
    this.hearingUrgencyHeader = page.getByRole("heading", { name: "Hearing urgency", });
    this.groundsForTheApplicationLink = page.getByRole("link", { name: "Grounds for the application", });
    this.groundsForTheApplicationHeading = page.getByRole("heading", { name: "Grounds for the application", });
    this.groundsForTheApplicationHasBeenUpdatedFinished = page.locator('xpath=//*[@id="taskListLabel"]/dt/ccd-markdown/div/markdown/div/p[4]/img',);
    this.riskAndHarmToChildrenLink = page.getByRole("link", { name: "Risk and harm to children", });
    this.allocationProposalFinished = page.locator('p:has(a[text="Allocation proposal"]) > img[title="Finished"]');
    this.allocationProposalHeading = page.getByRole("group", { name: "Allocation proposal" }).getByRole("heading");
    this.allocationProposalLink = page.getByRole("link", { name: "Allocation proposal", });
    this.uploadDocumentsLink = page.getByRole("link", { name: "Upload documents", });
    this.addApplicationDocsHeading = page.getByRole("heading", { name: "Add application documents", });
    this.upLoadDocsInProgress = page.locator('p:has(a[text="Upload documents"]) > img[title="In progress"]');
    this.applicantDetailsLink = page.getByRole('link', { name: 'Applicant\'s details' });
    this.respondentsDetailsLink = page.getByRole('link', { name: 'Respondents\' details' });
    this.applicantDetailsUpdated = page.locator('p').filter({ hasText: 'Applicant\'s details' }).getByRole('img', { name: 'Information added' });
    this.childDetailsLink = page.getByRole("link", { name: 'Child\'s Details', });
    this.respondentsDetailsLink = page.getByRole('link', { name: 'Respondents\' details' });
    this.childDetailsUpdated = page.locator('p').filter({ hasText: 'Child\'s Details' }).getByRole('img', { name: 'Information added' });
    this.welshLanguageRequirements = page.getByRole('link', { name: 'Welsh language requirements' });
    this.welshLanguageReqFinished = page.locator('p:has(a[text="Welsh language requirements"]) > img[title="Finished"]');
    this.internationalElementsHeading = page.getByRole('link', { name: 'International element' });
    this.c1WithSupplement = page.getByRole('link', { name: 'C1 with supplement' });
    this.c1WithSupplementFinished = page.locator('p:has(a[text="C1 with supplement"]) > img[title="Finished"]');
    this.submitApplicationLink = page.getByRole('link', { name: 'Submit application' });
    this.welshLanguageReqFinished = page.locator('p:has(a[text()="Welsh language requirements"]) > img[title="Finished"]');
    this.otherProceedingsLink = page.getByRole("link", { name: "Other Proceedings", });
    this.internationalElementsHeading = page.getByRole('link', { name: 'International element' });
    this.courtServicesNeeded = page.getByRole('link', { name: 'Court services needed' });

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

  async applicantDetails() {
    await this.applicantDetailsLink.isVisible();
    await this.applicantDetailsLink.click();
  }

  async applicantDetailsHasBeenUpdated() {
    await expect(this.applicantDetailsUpdated).toBeVisible();
  }

  async childDetails() {
    await this.childDetailsLink.isVisible();
    await this.childDetailsLink.click();
  }

  async childDetailsHasBeenUpdated() {
    await expect(this.childDetailsUpdated).toBeVisible();
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
    await expect(this.allocationProposalFinished).toBeVisible();
  }

  async otherProceedingsNeeded() {
    await this.otherProceedingsLink.click();
  }

  async welshLanguageReq() {
    await this.welshLanguageRequirements.click();
  }

  async welshLanguageReqUpdated() {
    await expect(this.welshLanguageReqFinished).toBeVisible;
  }
  async c1WithSupp() {
     await this.c1WithSupplement.click();
  }

  async c1WithSuppFinished() {
     await expect(this.c1WithSupplementFinished).toBeVisible;
  }
  async internationalElementReqUpdated() {
    await this.internationalElementsHeading.isVisible();
    await this.internationalElementsHeading.click();
  }

  async courtServicesNeededReqUpdated() {
    await this.courtServicesNeeded.isVisible();
    await this.courtServicesNeeded.click();
  }
  
  async submitCase() {
    await this.submitApplicationLink.click();
  }
}
