import {BasePage} from "./base-page";
import {Locator, Page} from "@playwright/test";

export class Orders extends BasePage {
    orderPage: Page;
    private EPOEndDate: Locator;
    private createOrder: Locator;
    private orderTypeRadio: Locator;
    private orderApproved: Locator;
    private orderApplication: Locator;
    private approvedHearing: Locator;
    private issuingJudge: Locator;
    private childInvolved: Locator;
    private EPOrderType: Locator;
    private finalOrder: Locator;
    private orderPreviewLink: Locator;

    public constructor(page: Page) {
        super(page);
        this.createOrder = page.getByRole('radio', {name: 'Create an order'});
        this.orderTypeRadio = page.getByRole('group', {name: 'Select order'});
        this.orderApproved = page.getByRole('group', {name: 'Was the order approved at a'});
        this.orderApplication = page.getByRole('group', {name: 'Is there an application for'});
        this.approvedHearing = page.getByLabel('Which hearing?');
        this.issuingJudge = page.getByRole('group', {name: 'Is this judge issuing the'});
        this.childInvolved = page.getByRole('group', {name: 'Is the order about all the children?'})
        this.EPOrderType = page.getByRole('group', {name: 'Type of emergency protection'});
        this.EPOEndDate = page.getByRole('group', {name: 'When does it end?'});
        this.finalOrder = page.getByRole('group', {name: 'Is this a final order?'});
        this.orderPreviewLink = page.getByRole('link', {name: 'Preview order.pdf'});
        this.orderPage = page;
    }

    async createorder() {
        await this.createOrder.check();
    }

    async selectOrder(orderType: string) {
        await this.orderTypeRadio.getByLabel(`${orderType}`).check();
    }

    async addIssuingDetails() {
        await this.orderApproved.getByLabel('Yes').click();
        await this.approvedHearing.selectOption('Case management hearing, 3 November 2012');
        await this.orderApplication.getByLabel('No').click();
        await this.clickContinue();
        await this.issuingJudge.getByRole('radio', {name: 'Yes'}).check();
    }

    async addChildDetails() {
        await this.childInvolved.getByRole('radio', {name: 'No'}).click();
        await this.page.getByRole('group', {name: 'Child 1 (Optional)'}).getByLabel('Yes').check();
        await this.page.getByRole('group', {name: 'Child 2 (Optional)'}).getByLabel('Yes').check();
        await this.page.getByRole('group', {name: 'Child 4 (Optional)'}).getByLabel('Yes').check();
    }

    async addOrderDetails() {

        await this.EPOrderType.getByLabel('Remove to accommodation').click();
        await this.page.getByRole('group', {name: 'Include: "Any person who can produce the children to the applicant must do so"'}).getByLabel('Yes').click();
        await this.page.getByLabel('Add description of children (').fill('Children description');
        await this.page.getByLabel('Add further directions, if').fill('Furhter direction\nto the applicant \nto take care of children');
        await this.EPOEndDate.getByRole('textbox', {name: 'Day'}).fill('2');
        await this.EPOEndDate.getByRole('textbox', {name: 'Month'}).fill('10');
        await this.EPOEndDate.getByRole('textbox', {name: 'Year'}).fill('2013');
        await this.EPOEndDate.getByRole('spinbutton', {name: 'Hour'}).fill('10');
        await this.finalOrder.getByLabel('Yes').click();
    }

    async previewOrderBeforeSubmit() {
        const newPagePromise = this.page.context().waitForEvent('page');
        this.orderPreviewLink.click();
        this.orderPage = await newPagePromise;
        await this.orderPage.waitForLoadState();
    }
}
