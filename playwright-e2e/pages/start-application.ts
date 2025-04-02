import {expect, type Locator, type Page} from "@playwright/test";
import {BasePage} from "./base-page.ts";

export class StartApplication extends BasePage {
    // readonly logExpertReportLink: Locator;
    public constructor(page: Page) {
        super(page)

    }

    get addApplicationDetailsHeading(): Locator {
        return this.page.getByRole("heading", {name: "Add application details",});
    }


    get hearingUrgencyLink(): Locator {
        return this.page.getByRole("link", {name: "Hearing urgency",});
    }

    get groundsForTheApplicationLink(): Locator {
        return this.page.getByRole("link", {name: "Grounds for the application",});
    }

    get riskAndHarmToChildrenLink(): Locator {
        return this.page.getByRole("link", {name: "Risk and harm to children",});
    }

    get hearingUrgencyHeader(): Locator {
        return this.page.getByRole("heading", {name: "Hearing urgency",});
    }

    get groundsForTheApplicationHeading(): Locator {
        return this.page.getByRole("heading", {name: "Grounds for the application",});
    }

    get groundsForTheApplicationHasBeenUpdatedFinished(): Locator {
        return this.page.locator('xpath=//*[@id="taskListLabel"]/dt/ccd-markdown/div/markdown/div/p[4]/img',);
    }

    get allocationProposalFinished(): Locator {
        return this.page.locator('p').filter({hasText: 'Allocation proposal'}).getByRole('img', {name: 'Finished'});
    }

    get allocationProposalLink(): Locator {
        return this.page.getByRole("link", {name: "Allocation proposal",});
    }


    get uploadDocumentsLink(): Locator {
        return this.page.getByRole("link", {name: "Upload documents",});
    }

    get upLoadDocsInProgress(): Locator {
        return this.page.locator('p').filter({hasText: 'Upload documents'}).getByRole('img', {name: 'Finished'});
    }

    get applicantDetailsLink(): Locator {
        return this.page.getByRole('link', {name: 'Applicant\'s details'});
    }

    get childDetailsLink(): Locator {
        return this.page.getByRole("link", {name: 'Child\'s Details',});
    }

    get childDetailsUpdated(): Locator {
        return this.page.locator('p').filter({hasText: 'Child\'s Details'}).getByRole('img', {name: 'Information added'});
    }

    get respondentsDetailsLink(): Locator {
        return this.page.getByRole('link', {name: 'Respondents\' details'});
    }

    get applicantDetailsUpdated(): Locator {
        return this.page.locator('p').filter({hasText: 'Applicant\'s details'}).getByRole('img', {name: 'Information added'});
    }

    get welshLanguageRequirements(): Locator {
        return this.page.getByRole('link', {name: 'Welsh language requirements'});
    }

    get welshLanguageReqFinished(): Locator {
        return this.page.locator('p:has(a[text="Welsh language requirements"]) > img[title="Finished"]');
    }

    get otherProceedingsLink(): Locator {
        return this.page.getByRole('link', {name: "Other Proceedings",});
    }

    get internationalElementsHeading(): Locator {
        return this.page.getByRole('link', {name: 'International element'});
    }

    get courtServices(): Locator {
        return this.page.getByRole('link', {name: 'Court services needed'});
    }

    get submitApplicationLink(): Locator {
        return this.page.getByRole('link', {name: 'Submit application'})
    }

    get otherPeopleInCaseLink(): Locator {
        return this.page.getByRole('link', {name: 'Other people in the case'});
    }

    get returnApplicationLink(): Locator {
        return this.page.getByRole('link', {name: 'Return application'});
    }

    get ordersAndDirectionsSoughtFinishedStatus():Locator{
        return this.page.locator('p').filter({ hasText: 'Orders and directions sought' }).getByRole('img');
    }



    async groundsForTheApplication() {
        expect(await this.groundsForTheApplicationLink).toBeVisible();
        await this.groundsForTheApplicationLink.click();
        await expect(this.groundsForTheApplicationHeading).toBeVisible();
    }

    async groundsForTheApplicationHasBeenUpdated() {
        await expect(this.groundsForTheApplicationHasBeenUpdatedFinished).toBeVisible();
    }

    async riskAndHarmToChildren() {
        await expect(this.riskAndHarmToChildrenLink).toBeVisible();
        await this.riskAndHarmToChildrenLink.click();
    }
  async hearingUrgency() {
    expect(await this.hearingUrgencyLink).toBeVisible();
    await this.hearingUrgencyLink.click();
    await expect(this.hearingUrgencyHeader).toBeVisible();
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
        await expect(this.allocationProposalFinished).toBeVisible();
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

  async courtServicesReqUpdated() {
    await expect(this.courtServices).toBeVisible();
    await this.courtServices.click();
  }


    async addOtherPeopleInCase() {
        await expect(this.otherPeopleInCaseLink).toBeVisible();
        await this.otherPeopleInCaseLink.click();
    }


    async submitCase() {
        await this.submitApplicationLink.click();
    }
}
