import { type Page, type Locator, expect } from "@playwright/test";
import {BasePage} from "./base-page";
import {urlConfig} from "../settings/urls";
import config from "../settings/test-docs/config";

export class StartApplication extends BasePage {
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
    readonly internationalElementsHeading: Locator;
    readonly courtServices: Locator;
    readonly submitApplicationLink: Locator;
    readonly otherPeopleInCaseLink: Locator;
    readonly returnApplicationLink: Locator;
    readonly  ordersAndDirectionsSoughtFinishedStatus: Locator;

    // readonly logExpertReportLink: Locator;
    public constructor(page: Page) {
        super(page);
        this.addApplicationDetailsHeading = page.getByRole("heading", { name: "Add application details", });
        this.ordersAndDirectionsSoughtLink = page.getByRole("heading", { name: "Orders and directions sought", });
        this.factorsAffectingParentingLink = page.getByRole("heading", { name: "Factors affecting parenting", });
        this.hearingUrgencyLink = page.getByRole("link", { name: "Hearing urgency", });
        this.hearingUrgencyHeader = page.getByRole("heading", { name: "Hearing urgency", });
        this.groundsForTheApplicationLink = page.getByRole("link", { name: "Grounds for the application", });
        this.groundsForTheApplicationHeading = page.getByRole("heading", { name: "Grounds for the application", });
        // this.groundsForTheApplicationHasBeenUpdatedFinished = page.locator('heading-h2',);
        this.groundsForTheApplicationHasBeenUpdatedFinished = page.locator('xpath=//*[@id="taskListLabel"]/dt/ccd-markdown/div/markdown/div/p[4]/img',);
        this.riskAndHarmToChildrenLink = page.getByRole("link", { name: "Risk and harm to children", });
        this.allocationProposalFinished = page.locator('p').filter({ hasText: 'Allocation proposal' }).getByRole('img', { name: 'Finished' });
        this.allocationProposalHeading = page.getByRole("group", { name: "Allocation proposal" }).getByRole("heading");
        this.allocationProposalLink = page.getByRole("link", { name: "Allocation proposal", });
        this.uploadDocumentsLink = page.getByRole("link", { name: "Upload documents", });
        this.addApplicationDocsHeading = page.getByRole("heading", { name: "Application documents", });
        this.upLoadDocsInProgress = page.locator('p').filter({ hasText: 'Upload documents' }).getByRole('img', { name: 'Finished' })
        this.applicantDetailsLink = page.getByRole('link', { name: 'Applicant\'s details' });
        this.respondentsDetailsLink = page.getByRole('link', { name: 'Respondents\' details' });
        this.applicantDetailsUpdated = page.locator('p').filter({ hasText: 'Applicant\'s details' }).getByRole('img', { name: 'Information added' });
        this.childDetailsLink = page.getByRole("link", { name: 'Child\'s Details', });
        this.respondentsDetailsLink = page.getByRole('link', { name: 'Respondents\' details' });
        this.childDetailsUpdated = page.locator('p').filter({ hasText: 'Child\'s Details' }).getByRole('img', { name: 'Information added' });
        this.welshLanguageRequirements = page.getByRole('link', { name: 'Welsh language requirements' });
        this.welshLanguageReqFinished = page.locator('p:has(a[text="Welsh language requirements"]) > img[title="Finished"]');
        this.internationalElementsHeading = page.getByRole('link', { name: 'International element' });
        this.submitApplicationLink = page.getByRole('link', { name: 'Submit application' })
        this.otherProceedingsLink = page.getByRole('link', { name: "Other Proceedings", });
        this.courtServices = page.getByRole('link', { name: 'Court services'});
        this.otherPeopleInCaseLink = page.getByRole('link', { name: 'Other people in the case' }) ;
        this.returnApplicationLink = page.getByRole('link', { name: 'Return application'});
        this.ordersAndDirectionsSoughtFinishedStatus = page.locator('p').filter({ hasText: 'Orders and directions sought' }).getByRole('img');
    }

    async groundsForTheApplication() {
        await Promise.all([
            this.page.waitForResponse(response =>
                response.url().includes(`event-triggers/enterGrounds?ignore-warning=false`) &&
                response.request().method() === 'GET'
            ),
            this.groundsForTheApplicationLink.click()
        ]);
    }

    async groundsForTheApplicationHasBeenUpdated() {
        await expect(this.groundsForTheApplicationHasBeenUpdatedFinished).toBeVisible();
    }

    async hearingUrgency() {
        await Promise.all([
            this.page.waitForResponse((response) =>
                response.url().includes('hearingNeeded') &&
                response.status() === 200
            ),
            this.hearingUrgencyLink.click()
        ])

        await expect(this.hearingUrgencyHeader).toBeVisible();
    }

    async riskAndHarmToChildren() {
        await expect(this.riskAndHarmToChildrenLink).toBeVisible();
        await this.riskAndHarmToChildrenLink.click();
    }

    async addApplicationDocuments(): Promise<void> {
        await Promise.all([
            this.page.waitForResponse(response =>
                response.url().includes('event-triggers/uploadDocuments?ignore-warning=false') &&
                response.request().method() === 'GET' &&
                response.status() === 200
            ),
            await this.uploadDocumentsLink.click()
        ]);
    }

    async addApplicationDocumentsInProgress() {
        await expect(this.upLoadDocsInProgress).toBeVisible();
    }

    async applicantDetails(): Promise<void> {
        await this.applicantDetailsLink.click()
    }

    async applicantDetailsHasBeenUpdated() {
        await expect(this.applicantDetailsUpdated).toBeVisible();
    }

    async childDetails() {
        await this.childDetailsLink.click();
    }

    async childDetailsHasBeenUpdated() {
        await expect(this.childDetailsUpdated).toBeVisible();
    }

    async respondentDetails() {
        await this.respondentsDetailsLink.click()
    }

    async allocationProposal() {
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

    async returnApplication() {
        await expect(this.returnApplicationLink).toBeVisible();
        await this.returnApplicationLink.click();
    }

    async submitCase() {
        await this.submitApplicationLink.click();
    }
}
