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
  readonly courtServicesNeeded: Locator;
  readonly submitApplicationLink: Locator;
    readonly orderAndDirectionSoughtFinish: Locator;
    readonly hearingUrgencyFinish: Locator;
    readonly riskAndHarmToChildrenFinished: Locator;
    readonly factorsAffectingParentingFinish: Locator;
    readonly respondentsDetailsUpdated: Locator;
    readonly courtServiceUpdated: Locator;
    readonly internationalElement: Locator;
    private internationalElementsHeading: Locator;

  public constructor(page: Page) {
    this.page = page;
    this.addApplicationDetailsHeading = page.getByRole("heading", { name: "Add application details", });
    this.ordersAndDirectionsSoughtLink = page.getByRole("link", { name: "Orders and directions sought", });
    this.orderAndDirectionSoughtFinish = page.locator('p').filter({ hasText: 'Orders and directions sought' }).getByRole('img',{name:'Finished'});
    this.factorsAffectingParentingLink = page.getByRole("heading", { name: "Factors affecting parenting", });
      this.factorsAffectingParentingFinish =page.locator('p').filter({ hasText: 'Factors affecting parenting' }).getByRole('img',{name:'Finished'});
      this.hearingUrgencyLink = page.getByRole("link", { name: "Hearing urgency", });
    this.hearingUrgencyFinish = page.locator('p:has(a[text="Hearing urgency"]) > img[title="Finished"]');
    this.hearingUrgencyHeader = page.getByRole("heading", { name: "Hearing urgency", });
    this.groundsForTheApplicationLink = page.getByRole("link", { name: "Grounds for the application", });
    this.groundsForTheApplicationHeading = page.getByRole("heading", { name: "Grounds for the application", });
    this.groundsForTheApplicationHasBeenUpdatedFinished = page.locator('xpath=//*[@id="taskListLabel"]/dt/ccd-markdown/div/markdown/div/p[4]/img',);
    this.riskAndHarmToChildrenLink = page.getByRole("link", { name: "Risk and harm to children", });
    this.riskAndHarmToChildrenFinished= page.locator('p').filter({ hasText: 'Risk and harm to children' }).getByRole('img',{name:'Finished'});
     // page.locator('p:has(a[text="Risk and harm to children"]) > img[title="Finished"]');
    this.allocationProposalFinished = page.locator('p:has(a[text="Allocation proposal"]) > img[title="Finished"]');
    this.allocationProposalHeading = page.getByRole("group", { name: "Allocation proposal" }).getByRole("heading");
    this.allocationProposalLink = page.getByRole("link", { name: "Allocation proposal", });
    this.uploadDocumentsLink = page.getByRole("link", { name: "Upload documents", });
    this.addApplicationDocsHeading = page.getByRole("heading", { name: "Add application documents", });
      this.upLoadDocsInProgress = page.locator('p').filter({ hasText: 'Upload documents' }).getByRole('img',{name:'Finished'})
    //this.upLoadDocsInProgress = page.locator('p:has(a[text="Upload documents"]) > img[title="Finished"]');
    this.applicantDetailsLink = page.getByRole('link', { name: 'Applicant\'s details' });
    this.applicantDetailsUpdated = page.locator('p').filter({ hasText: 'Applicant\'s details' }).getByRole('img', { name: 'Information added' });
    this.respondentsDetailsLink = page.getByRole('link', { name: 'Respondents\' details' });
    this.respondentsDetailsUpdated = page.locator('p').filter({ hasText: 'Respondents\' details' }).getByRole('img', { name: 'Information added' });
    this.childDetailsLink = page.getByRole("link", { name: 'Child\'s Details', });
    this.childDetailsUpdated = page.locator('p').filter({ hasText: 'Child\'s Details' }).getByRole('img', { name: 'Information added' });
    this.welshLanguageRequirements = page.getByRole('link', { name: 'Welsh language requirements' });
    this.welshLanguageReqFinished = page.locator('p:has(a[text="Welsh language requirements"]) > img[title="Finished"]');
    this.internationalElementsHeading = page.getByRole('link', { name: 'International element' });
    this.submitApplicationLink = page.getByRole('link', { name: 'Submit application' });
    this.courtServicesNeeded = page.getByRole('link', { name: 'Court services needed' });
    this.courtServiceUpdated = page.locator('p:has(a[text="Court services needed"]) > img[title="Finished"]');


  }

  async groundsForTheApplication() {
    expect(await this.groundsForTheApplicationLink).toBeVisible();
    await this.groundsForTheApplicationLink.click();
   // await expect(this.groundsForTheApplicationHeading).toBeVisible();
  }

    async Ordersanddirectionssought() {
        await expect(this.ordersAndDirectionsSoughtLink).toBeVisible();
        await this.ordersAndDirectionsSoughtLink.click();
        // await expect(this.groundsForTheApplicationHeading).toBeVisible();
    }

    async hearingUrgency() {
        await expect(this.hearingUrgencyLink).toBeVisible();
        await this.hearingUrgencyLink.click();
        // await expect(this.groundsForTheApplicationHeading).toBeVisible();
    }

    async assertHearingUrgencyFinish()
    {
        await expect(this.hearingUrgencyFinish).toBeVisible;
    }

  async groundsForTheApplicationHasBeenUpdated() {
    await expect(this.groundsForTheApplicationHasBeenUpdatedFinished).toBeVisible();
  }

  async riskAndHarmToChildren() {
    await expect(this.riskAndHarmToChildrenLink).toBeVisible();
    await this.riskAndHarmToChildrenLink.click();

  }

  async assertRiskAndHarmTochildFinished()
  {
      await expect(this.riskAndHarmToChildrenFinished).toBeVisible();
  }

  async addApplicationDocuments() {
    await expect(this.uploadDocumentsLink).toBeVisible();
    await this.uploadDocumentsLink.click();
  }

  async addApplicationDocumentsInProgress() {
    await expect(this.upLoadDocsInProgress).toBeVisible();
  }

  async applicantDetails() {
    await expect(this.applicantDetailsLink).toBeVisible();
    await this.applicantDetailsLink.click();
  }

  async applicantDetailsHasBeenUpdated() {
    await expect(this.applicantDetailsUpdated).toBeVisible();
  }

  async childDetails() {
    await expect(this.childDetailsLink).toBeVisible();
    await this.childDetailsLink.click();
  }

  async childDetailsHasBeenUpdated() {
    await expect(this.childDetailsUpdated).toBeVisible();
  }

  async respondentDetails() {
    await expect(this.respondentsDetailsLink).toBeVisible();
    await this.respondentsDetailsLink.click();
  }

  async allocationProposal() {
    await expect(this.allocationProposalLink).toBeVisible();
    await this.allocationProposalLink.click();
  }

  async allocationProposalHasBeenUpdated() {
    await expect(this.allocationProposalFinished).toBeVisible;
  }

  async welshLanguageReq() {
    await this.welshLanguageRequirements.click();
  }

  async welshLanguageReqUpdated() {
    await expect(this.welshLanguageReqFinished).toBeVisible;
  }

  async internationalElementReqUpdated() {
    await expect(this.internationalElementsHeading).toBeVisible();
    await this.internationalElementsHeading.click();
  }

  async courtServicesNeededReqUpdated() {
    await expect(this.courtServicesNeeded).toBeVisible();
    await this.courtServicesNeeded.click();
  }

  async submitCase() {
    await this.submitApplicationLink.click();
  }

    async assertOrderAndDirectionSoughtsFinished() {
        await expect(this.orderAndDirectionSoughtFinish).toBeVisible();
    }

    async assertFactorsaffectingparenting() {
        await expect(this.factorsAffectingParentingFinish).toBeVisible();
    }

    async assertRespondentDetail() {
        await expect(this.respondentsDetailsUpdated).toBeVisible();
    }

    async assertCourtService(){
      await expect(this.courtServiceUpdated).toBeVisible();
    }


}
