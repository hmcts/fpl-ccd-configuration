import {type Page, type Locator, expect} from "@playwright/test";
import {BasePage} from "./base-page";

export class ReviewAdditionalApplication extends BasePage {
    // readonly page: Page;
    private c2AdditionalApplicationGroup: Locator;
    private c1AdditionalApplicationGroup: Locator;
    private approveApplicationAndOrder: Locator;
    private approveApplicationAndChangeOrder: Locator;
    private approveApplicationRefuseOrder: Locator;
    private refuseApplication: Locator;
    private listAtNextHearing: Locator;
    private amendedOrder: Locator;
    private applicantNeedsToMakeChangesDetail: Locator;
    private refusalReason: Locator;
    private whatTheDecision: Locator;
    private isOrderUrgent: Locator;
    private closeConfirmationInformation: Locator;
    draftOrderPage: any;

    public constructor(page: Page) {
        super(page);

        this.c2AdditionalApplicationGroup = page.getByRole('group', {name: 'C2 additional application'});
        this.c1AdditionalApplicationGroup = page.getByRole('group', {name: 'C1 additional application'});
        this.approveApplicationAndOrder = page.getByRole('radio', {name: 'Approve the application and the order'});
        this.approveApplicationAndChangeOrder = page.getByRole('radio', {name: 'Approve the application but make changes to the order'});
        this.approveApplicationRefuseOrder = page.getByRole('radio', {name: 'Approve the application but the applicant need to make changes to the order'});
        this.refuseApplication = page.getByRole('radio', {name: 'Refuse the application'});
        this.listAtNextHearing = page.getByRole('radio', {name: 'List at the next hearing'});
        this.amendedOrder = page.getByLabel('Upload amended order');
        this.applicantNeedsToMakeChangesDetail = page.getByLabel('What does the applicant need to change?');
        this.refusalReason = page.getByLabel('Reason for refusal - this will populate the order');
        this.whatTheDecision = page.getByRole('group', {name: 'What do you want to do?'});
        this.isOrderUrgent = page.getByRole('checkbox', {name: 'One or more of the orders contains a priority administrative action. The actions will be prioritised by the centralised family public law team.'});
        this.closeConfirmationInformation = page.getByRole('button', {name: 'Close and Return to case'});

    }

    async getValueofC2AdditionalApplicationGroup(label: string): Promise<string> {
        const value = await this.c2AdditionalApplicationGroup.locator('dl').filter({hasText: label}).innerText();
        return value;
    }

    async getValueofC1AdditionalApplicationGroup(label: string): Promise<string> {
        const value = await this.c1AdditionalApplicationGroup.locator('dl').filter({hasText: label}).innerText();
        return value;
    }

    async makeDecision(decision: string): Promise<void> {
        await this.whatTheDecision.getByRole('radio', {name: decision}).check();
    }

    async setOrderUrgent(isUrgent: boolean): Promise<void> {
        if (isUrgent) {
            await this.isOrderUrgent.check();
        }
    }

    async closeConfirmation(): Promise<void> {
        await this.closeConfirmationInformation.click();
    }

    async uploadAmendedOrder(filePath: string): Promise<void> {
        await this.amendedOrder.setInputFiles(filePath);
    }

    async enterChangesRequested(detail: string): Promise<void> {
        await this.applicantNeedsToMakeChangesDetail.fill(detail);
    }

    async enterRefusalReason(reason: string): Promise<void> {
        await this.refusalReason.fill(reason);
    }

    async openDraftOrder(docName: string) {

        const newPagePromise = this.page.context().waitForEvent('page');
        await this.page.getByRole('button', {name: `${docName}`}).click();
        this.draftOrderPage = await newPagePromise;
        await this.draftOrderPage.waitForLoadState();
    }

}
