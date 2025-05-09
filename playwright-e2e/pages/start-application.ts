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
        return this.page.getByRole("link", {name: "Hearing urgency",exact:true});
    }

    get groundsForTheApplicationLink(): Locator {
        return this.page.getByRole("link", {name: "Grounds for the application",exact:true});
    }

    get riskAndHarmToChildrenLink(): Locator {
        return this.page.getByRole("link", {name: "Risk and harm to children",exact:true});
    }

    get hearingUrgencyHeader(): Locator {
        return this.page.getByRole("heading", {name: "Hearing urgency"});
    }

    get groundsForTheApplicationHeading(): Locator {
        return this.page.getByRole("heading", {name: "Grounds for the application",});
    }

    get groundsForTheApplicationHasBeenUpdatedFinished(): Locator {
        return this.page.locator('xpath=//*[@id="taskListLabel"]/dt/ccd-markdown/div/markdown/div/p[4]/img',);
    }

    get ordersAndDirectionUpdated(): Locator {
        return this.page.locator('p').filter({hasText: 'Orders and directions sought'}).getByRole('img', {name: 'Finished'});
    }
    get hearingUrgencyUpdated(): Locator {
        return this.page.locator('p').filter({hasText: 'Hearing urgency'}).getByRole('img', {name: 'Finished'});
    }

    get allocationProposalFinished(): Locator {
        return this.page.locator('p').filter({hasText: 'Allocation proposal'}).getByRole('img', {name: 'Finished'});
    }
    get caseNameFinished(): Locator {
        return this.page.locator('p').filter({hasText: 'Case Name'}).getByRole('img', {name: 'Finished'});
    }



    get allocationProposalLink(): Locator {
        return this.page.getByRole("link", {name: "Allocation proposal",exact:true});
    }


    get uploadDocumentsLink(): Locator {
        return this.page.getByRole("link", {name: "Upload documents",exact:true});
    }

    get upLoadDocsInProgress(): Locator {
        return this.page.locator('p').filter({hasText: 'Upload documents'}).getByRole('img', {name: 'Finished'});
    }

    get applicantDetailsLink(): Locator {
        return this.page.getByRole('link', {name: 'Applicant\'s details',exact:true});
    }

    get childDetailsLink(): Locator {
        return this.page.getByRole("link", {name: 'Child\'s details',exact:true});
    }



    get respondentsDetailsLink(): Locator {
        return this.page.getByRole('link', {name: 'Respondents\' details',exact:true});
    }

    get applicantDetailsUpdated(): Locator {
        return this.page.locator('p').filter({hasText: 'Applicant\'s details'}).getByRole('img', {name: 'Information added'});
    }

    get respondentDetailsUpdated(): Locator {
        return this.page.locator('p').filter({hasText: 'Respondents\' details'}).getByRole('img', {name: 'Information added'});
    }

    get childDetailsUpdated(): Locator {
        return this.page.locator('p').filter({hasText: 'Child\'s details'}).getByRole('img', {name: 'Information added'});
    }

    get welshLanguageRequirements(): Locator {
        return this.page.getByRole('link', {name: 'Welsh language requirements',exact:true});
    }

    get welshLanguageReqFinished(): Locator {
        return this.page.locator('p:has(a[text="Welsh language requirements"]) > img[title="Finished"]');
    }

    get otherProceedingsLink(): Locator {
        return this.page.getByRole('link', {name: "Other Proceedings",exact:true});
    }

    get internationalElementsHeading(): Locator {
        return this.page.getByRole('link', {name: 'International element',exact:true});
    }

    get courtServices(): Locator {
        return this.page.getByRole('link', {name: 'Court services needed',exact:true});
    }

    get submitApplicationLink(): Locator {
        return this.page.getByRole('link', {name: 'Submit application',exact:true})
    }

    get otherPeopleInCaseLink(): Locator {
        return this.page.getByRole('link', {name: 'Other people in the case',exact:true});
    }

    get returnApplicationLink(): Locator {
        return this.page.getByRole('link', {name: 'Return application',exact:true});
    }

    async groundsForTheApplication() {

        await expect(async () => {
            await this.page.reload();
            await  this.groundsForTheApplicationLink.first().click();
            await expect(this.groundsForTheApplicationLink).toBeHidden();
        }).toPass();
    }

    async groundsForTheApplicationHasBeenUpdated() {
        await expect(this.groundsForTheApplicationHasBeenUpdatedFinished).toBeVisible();
    }

    async caseNameUpdated() {
        await expect(this.caseNameFinished).toBeVisible();
    }

    async riskAndHarmToChildren() {
        await expect(this.riskAndHarmToChildrenLink).toBeVisible();
        await this.riskAndHarmToChildrenLink.click();
    }
    async orderAndDirectionUpdated(){
        await expect(this.ordersAndDirectionUpdated).toBeVisible();
    }

    async hearingUrgency() {

        await expect(async () => {
            await this.page.reload();
            await  this.hearingUrgencyLink.first().click();
            await expect(this.hearingUrgencyLink).toBeHidden();
        }).toPass();

    }


    async addApplicationDocuments() {
        await expect(async () => {
            await this.page.reload();
            await this.uploadDocumentsLink.click();
            await expect(this.uploadDocumentsLink).toBeHidden();
        }).toPass();

    }

    async addApplicationDocumentsInProgress() {
        await expect(this.upLoadDocsInProgress).toBeVisible();
    }

    async applicantDetails() {

        await expect(async () => {
            await this.page.reload();
            await this.applicantDetailsLink.first().click();
            await expect(this.applicantDetailsLink).toBeHidden();
        }).toPass();


    }

    async applicantDetailsHasBeenUpdated() {
        await expect(this.applicantDetailsUpdated).toBeVisible();
    }

    async childDetails() {

        await expect(async () => {
            await this.page.reload();
            await  this.childDetailsLink.first().click();
            await expect(this.childDetailsLink).toBeHidden();
        }).toPass();

    }

    async childDetailsHasBeenUpdated() {
        await expect(this.childDetailsUpdated).toBeVisible();
    }

    async respondentDetails() {

        await expect(async () => {
            await this.page.reload();
            await  this.respondentsDetailsLink.first().click();
            await expect(this.respondentsDetailsLink).toBeHidden();
        }).toPass();



    }

    async allocationProposal() {

        await expect(async () => {
            await this.page.reload();
            await  this.allocationProposalLink.first().click();
            await expect(this.allocationProposalLink).toBeHidden();
        }).toPass();

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
    async respondentDetailsHasBeenUpdated(){
        await expect(this.respondentDetailsUpdated).toBeVisible();
    }
    async hearingurgencyHasBeenUpdate(){
        await expect(this.hearingUrgencyUpdated).toBeVisible();
    }
    async verifyCanBesubmitted()
    {
        await expect(this.page.getByText('Why can\'t I submit my application?', { exact: true })).toBeVisible();
        await this.page.getByText('Why can\'t I submit my application?', { exact: true }).click();
    }
}
