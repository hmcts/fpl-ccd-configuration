import {BasePage} from "./base-page";
import {Locator, Page} from "@playwright/test";
import config from "../settings/test-docs/config";

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
    private isExclusion: Locator;
    private excluded: Locator;
    private powerOfExclusionStart: Locator;

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
        this.isExclusion = page.getByRole('group', { name: 'Is there an exclusion' });
        this.excluded =page.getByLabel('Who\'s excluded');
        this.powerOfExclusionStart =page.getByRole('group', { name: 'Date power of exclusion starts' });

    }

    async createNewOrder() {
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

    async addEPOOrderDetails(EPOOrderType:string) {

        //await this.EPOrderType.getByLabel('Remove to accommodation').click();
        await this.EPOrderType.getByLabel(`${EPOOrderType}`).click();
        if(EPOOrderType == 'Prevent removal from an address'){

            // await page.getByRole('textbox', { name: 'Enter a UK postcode' }).click();
            // await page.getByRole('textbox', { name: 'Enter a UK postcode' }).fill('EN4');
            // await page.getByRole('button', { name: 'Find address' }).click();
            // await page.getByLabel('Select an address').selectOption('1: Object');
            await this.enterPostCode('EN4');
            await this.isExclusion.getByLabel('Yes').check();
           // await page.getByRole('group', { name: 'Is there an exclusion' }).getByLabel('Yes').check();

            await this.excluded.fill('father');
            await this.powerOfExclusionStart.getByLabel('Day').fill('12');
            await this.powerOfExclusionStart.getByLabel('Month').fill('3');
            await this.powerOfExclusionStart.getByLabel('Year').fill('2024');
            await this.powerOfExclusionStart.getByLabel('Day').fill('12');

            // await page.getByRole('group', { name: 'Date power of exclusion starts' }).getByLabel('Month').fill('3');
            // await page.getByRole('group', { name: 'Date power of exclusion starts' }).getByLabel('Year').fill('02');
            // await page.getByRole('group', { name: 'Date power of exclusion starts' }).getByLabel('Year').click();
            // await page.getByRole('group', { name: 'Date power of exclusion starts' }).getByLabel('Year').fill('2024');
           // await this.page.getByRole('textbox', { name: 'Upload power of arrest, if' }).click();
            await this.page.getByRole('textbox', { name: 'Upload power of arrest, if' }).setInputFiles(config.testPdfFile);
await this.waitForAllUploadsToBeCompleted();
        }

        await this.page.getByRole('group', {name: 'Include: "Any person who can produce the children to the applicant must do so"'}).getByLabel('Yes').click();
        await this.page.getByLabel('Add description of children (').fill('Children description');
        await this.page.getByLabel('Add further directions, if').fill('Furhter direction\nto the applicant \nto take care of children');
        await this.EPOEndDate.getByRole('textbox', {name: 'Day'}).fill('2');
        await this.EPOEndDate.getByRole('textbox', {name: 'Month'}).fill('10');
        await this.EPOEndDate.getByRole('textbox', {name: 'Year'}).fill('2013');
        await this.EPOEndDate.getByRole('spinbutton', {name: 'Hour'}).fill('10');
        await this.finalOrder.getByLabel('Yes').click();


        // await page.getByRole('group', { name: 'Include: "Any person who can' }).getByLabel('Yes').check();
        // await page.getByLabel('Add description of children (').dblclick();
        // await page.getByLabel('Add description of children (').fill('tesy');
        // await page.getByLabel('Add further directions, if').click();
        // await page.getByLabel('Add further directions, if').click();
        // await page.getByLabel('Add further directions, if').fill('test');
        // await page.getByRole('group', { name: 'When does it end?' }).getByLabel('Day').click({
        //     clickCount: 3
        // });
        // await page.getByRole('group', { name: 'When does it end?' }).getByLabel('Day').fill('12');
        // await page.getByRole('group', { name: 'When does it end?' }).getByLabel('Month').click();
        // await page.getByRole('group', { name: 'When does it end?' }).getByLabel('Month').fill('3');
        // await page.getByRole('group', { name: 'When does it end?' }).getByLabel('Year').fill('20');
        // await page.getByRole('group', { name: 'When does it end?' }).getByLabel('Year').click();
        // await page.getByRole('group', { name: 'When does it end?' }).getByLabel('Year').fill('2024');
        // await page.getByRole('group', { name: 'When does it end?' }).getByLabel('Year').press('Tab');
        // await page.getByRole('spinbutton', { name: 'Minute' }).press('Shift+Tab');
        // await page.getByRole('spinbutton', { name: 'Hour' }).fill('10');
        // await page.getByRole('group', { name: 'Is this a final order?' }).getByLabel('Yes').check();
        // await page.getByRole('button', { name: 'Continue' }).click();
        // await page.getByRole('group', { name: 'Date power of exclusion starts' }).getByLabel('Month').click();
        // await page.getByRole('group', { name: 'Date power of exclusion starts' }).getByLabel('Month').fill('3');
        // await page.getByText('Manage ordersEPO order by').click();
        // await page.getByRole('button', { name: 'Continue' }).click();
        // await page.getByRole('group', { name: 'When does it end?' }).getByLabel('Year').click({
        //     clickCount: 3
        // });
        // await page.getByRole('group', { name: 'When does it end?' }).getByLabel('Year').fill('');
        // await page.getByRole('group', { name: 'When does it end?' }).getByLabel('Month').fill('');
        // await page.getByRole('group', { name: 'When does it end?' }).getByLabel('Month').click();
        // await page.getByRole('group', { name: 'When does it end?' }).getByLabel('Month').fill('10');
        // await page.getByRole('group', { name: 'When does it end?' }).getByLabel('Year').click();
        // await page.getByRole('group', { name: 'When does it end?' }).getByLabel('Year').fill('2013');
        // await page.getByRole('button', { name: 'Continue' }).click();
        // await page.goto('https://manage-case.aat.platform.hmcts.net/cases/case-details/1719848245113172/trigger/manageOrders/manageOrdersreview');
// await expect(page1.locator('#viewerContainer')).toContainText('An Emergency Protection Order is granted to the applicant, Swansea City Council.');
        // await expect(page1.locator('#viewerContainer')).toContainText('The Court authorises the applicant to prevent the children being removed from 5 Pilgrims');
        // await expect(page1.locator('#viewerContainer')).toContainText('The Court directs that father be excluded from 5 Pilgrims Rise, Barnet, EN4 9QP, United');


    }

    async openOrderDoc() {
        const newPagePromise = this.page.context().waitForEvent('page');
        this.orderPreviewLink.click();
        this.orderPage = await newPagePromise;
        await this.orderPage.waitForLoadState();
    }
}
