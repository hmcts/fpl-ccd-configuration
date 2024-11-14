import {BasePage} from "./base-page";
import {expect, Locator, Page} from "@playwright/test";
import config from "../settings/test-docs/config";

export class Orders extends BasePage {
    orderPage: Page;
    readonly orderToAmend: Locator;
    readonly uploadAmendOrder: Locator;
    readonly EPOEndDate: Locator;
    readonly orderTypeRadio: Locator;
    readonly orderApproved: Locator;
    readonly orderApplication: Locator;
    readonly approvedHearing: Locator;
    readonly issuingJudge: Locator;
    readonly childInvolved: Locator;
    readonly EPOrderType: Locator;
    readonly finalOrder: Locator;
    readonly orderPreviewLink: Locator;
    readonly isExclusion: Locator;
    readonly excluded: Locator;
    readonly powerOfExclusionStart: Locator;
    readonly judgemagistrateTitle: Locator;
    readonly judgeLastName: Locator;
    readonly judgeEmail: Locator;
    readonly legalAdvisorName: Locator;
    readonly orderDirectionDetails: Locator;
    readonly closeOrder: Locator;
    readonly careOrderIssuedDate: Locator;
    readonly careOrderIssuedCourt: Locator;
    readonly juridiction: Locator;
    readonly juridictionRegion: Locator;
    readonly approvalDate: Locator;
    readonly childAccomadation: Locator;
    readonly orderConsent: Locator;
    readonly orderReason: Locator;
    readonly childLegalAid: Locator;
    readonly juridictionRadio: Locator;
    readonly orderEndsOn: Locator;
    readonly orderLength: Locator;

    constructor(page: Page) {
        super(page);
        this.orderTypeRadio = page.getByRole('group', {name: 'Select order'});
        this.orderApproved = page.getByRole('group', {name: 'Was the order approved at a'});
        this.orderApplication = page.getByRole('group', {name: 'Is there an application for'});
        this.approvedHearing = page.getByLabel('Which hearing?');
        this.issuingJudge = page.getByRole('group', {name: 'Is this judge issuing the'});
        this.judgemagistrateTitle = page.getByRole('group', {name: 'Judge or magistrate\'s title'});
        this.childInvolved = page.getByRole('group', {name: 'Is the order about all the children?'})
        this.EPOrderType = page.getByRole('group', {name: 'Type of emergency protection'});
        this.EPOEndDate = page.getByRole('group', {name: 'When does it end?'});
        this.finalOrder = page.getByRole('group', {name: 'Is this a final order?'});
        this.orderPreviewLink = page.getByRole('link', {name: 'Preview order.pdf'});
        this.orderPage = page;
        this.isExclusion = page.getByRole('group', {name: 'Is there an exclusion'});
        this.excluded = page.getByLabel('Who\'s excluded');
        this.powerOfExclusionStart = page.getByRole('group', {name: 'Date power of exclusion starts'});
        this.orderToAmend = page.getByLabel('Select order to amend');
        this.uploadAmendOrder = page.getByRole('textbox', {name: 'Upload the amended order. It will then be dated and stamped as amended.'});
        this.judgeLastName = page.getByLabel('Last name');
        this.judgeEmail= page.getByLabel('Email Address');
        this.legalAdvisorName =page.getByLabel('Justices\' Legal Adviser\'s');
        this.orderDirectionDetails =page.getByLabel('Add further directions, if');
        this.closeOrder = page.getByRole('group', {name: 'Does this order close the case?'});
        this.careOrderIssuedDate = page.getByRole('group', {name: 'When was the care order issued?'});
        this.careOrderIssuedCourt =page.getByLabel('Which court issued the order?');
        this.juridiction = page.getByRole('group', {name: 'Select jurisdiction'});
        this.juridictionRegion = page.locator('#manageOrdersCafcassOfficesWales');
        this.approvalDate = page.getByRole('group', { name: 'Approval Date' });
        this.childAccomadation =page.getByLabel('Which child is the order for?');
        this.orderConsent = page.getByRole('group', { name: 'Is order by consent?' });
        this.orderReason = page.getByRole('group', { name: 'Order given because the child is likely to' });
        this.childLegalAid = page.getByRole('group', { name: 'Does the child have a Legal' });
        this.juridictionRadio = page.getByRole('group', {name: 'Jurisdiction'});
        this.orderEndsOn = page.getByRole('group', {name: 'When does the order end?'});
        this.orderLength =page.getByLabel('Order length, in months');

    }

    async selectOrderOperation(toDo: string) {
        await this.page.getByRole('radio', {name: `${toDo}`}).click();
    }

    async selectOrder(orderType: string) {
        await this.orderTypeRadio.getByLabel(`${orderType}`).check();
    }

    async addIssuingDetailsOfApprovedOrder(approvalDate: string) {
        await this.orderApproved.getByLabel('Yes').click();
        await this.approvedHearing.selectOption('Case management hearing, 3 November 2012');
        await this.orderApplication.getByLabel('No').click();
        await this.clickContinue();
        await this.issuingJudge.getByRole('radio', {name: 'Yes'}).check();
        await this.legalAdvisorName.fill('LA Marien Wester');
        if (approvalDate == 'yes'){
            await this.approvalDate.getByRole('textbox', { name: 'Day' }).fill('04');
            await this.approvalDate.getByRole('textbox', { name: 'Month' }).fill('11');
            await this.approvalDate.getByRole('textbox', { name: 'Year' }).fill('2023');
            await this.approvalDate.getByLabel('Hour').fill('12');
            await this.approvalDate.getByLabel('Minute').fill('20');
            await this.approvalDate.getByLabel('Second').fill('20');
        }
    }
    async addIssuningDeatilsOfUnApprovedOrder(){
        await this.orderApproved.getByLabel('No').click();
        await this.clickContinue();
        await expect.soft(this.page.getByText('Case assigned to: Her Honour')).toBeVisible();
        await this.issuingJudge.getByLabel('No').click();
        await this.judgemagistrateTitle.getByLabel('His Honour Judge').check();
        await this.judgeLastName.fill('John');
        await this.judgeEmail.fill('email@email.comLegal');
        await this.legalAdvisorName.fill('LA Jonathan');
    }

    async selectChildForAccomodation(){
        await this.childAccomadation.selectOption('Timothy Jones');
    }

    async addChildDetails(isAllChild: string) {
        await this.childInvolved.getByRole('radio', {name: `${isAllChild}`}).click();
        if(isAllChild == 'No'){
            await this.page.getByRole('group', {name: 'Child 1 (Optional)'}).getByLabel('Yes').check();
            await this.page.getByRole('group', {name: 'Child 2 (Optional)'}).getByLabel('Yes').check();
            await this.page.getByRole('group', {name: 'Child 4 (Optional)'}).getByLabel('Yes').check();
        }

    }

    async addEPOOrderDetails(EPOOrderType: string) {
        await this.EPOrderType.getByLabel(`${EPOOrderType}`).click();
        if (EPOOrderType == 'Prevent removal from an address') {
            await this.enterPostCode('EN4');
            await this.isExclusion.getByLabel('Yes').check();
            await this.excluded.fill('father');
            await this.powerOfExclusionStart.getByLabel('Day').fill('12');
            await this.powerOfExclusionStart.getByLabel('Month').fill('3');
            await this.powerOfExclusionStart.getByLabel('Year').fill('2024');
            await this.powerOfExclusionStart.getByLabel('Day').fill('12');
        }
        await this.page.getByRole('group', {name: 'Include: "Any person who can produce the children to the applicant must do so"'}).getByLabel('Yes').click();
        await this.page.getByLabel('Add description of children (').fill('Children description');
        await this.page.getByLabel('Add further directions, if').fill('Furhter direction\nto the applicant \nto take care of children');
        await this.EPOEndDate.getByRole('textbox', {name: 'Day'}).fill('2');
        await this.EPOEndDate.getByRole('textbox', {name: 'Month'}).fill('10');
        await this.EPOEndDate.getByRole('textbox', {name: 'Year'}).fill('2013');
        await this.EPOEndDate.getByRole('spinbutton', {name: 'Hour'}).fill('10');
        await this.finalOrder.getByLabel('Yes').click();
    }
    async addC32CareOrder(){
        await this.orderDirectionDetails.fill('Direction on accomadation of the children\nNeed assistance for child1 sam');
    }

    async addC32BDischargeOfCareOrder(){
        await this.careOrderIssuedDate.getByRole('textbox', { name: 'Day' }).fill('3');
        await this.careOrderIssuedDate.getByRole('textbox', { name: 'Month' }).fill('4');
        await this.careOrderIssuedDate.getByRole('textbox', { name: 'Year' }).fill('2022');
        await this.careOrderIssuedCourt.selectOption('Swansea C&F Justice Centre');
        await this.orderDirectionDetails.fill('Remove the child from social care . The respondent is new gaurdian');
        await this.finalOrder.getByText('No').click();

    }

    async addC47AppointOfGuardianOrder(){
        await this.juridiction.getByRole('radio', { name: 'Wales' }).check();
        await this.juridictionRegion.selectOption('Swansea');
        await this.orderDirectionDetails.fill('Remove the child from the social care and appointing Aunty as guardian');
    }

    async addC26SecureAccomadation(){
        await this.orderConsent.getByLabel('No').check();
        await this.orderReason.getByLabel('abscond and suffer harm').check();
        await this.childLegalAid.getByLabel('Yes').check();
        await this.juridictionRadio.getByRole('radio', { name: 'Wales' }).check();
        await this.orderDirectionDetails.fill('Further Direction for give secure accommodation');
        await this.orderEndsOn.getByLabel('In a set number of months').check();
        await this.orderLength.fill('12');
        await this.finalOrder.getByLabel('No').check();
    }

    async closeTheOrder(close:string){
        await this.closeOrder.getByLabel(`${close}`).check();
    }

    async openOrderDoc(docLink: string) {
        const newPagePromise = this.page.context().waitForEvent('page');
        await this.page.getByRole('link', {name: `${docLink}`}).click();
        this.orderPage = await newPagePromise;
        await this.orderPage.waitForLoadState();
    }

    async uploadAmendedOrder() {
        await this.uploadAmendOrder.setInputFiles(config.testPdfFile);
        await this.waitForAllUploadsToBeCompleted();
    }
}
