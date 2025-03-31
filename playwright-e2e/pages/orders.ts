import {BasePage} from "./base-page";
import {expect, Locator, Page} from "@playwright/test";
import config from "../settings/test-docs/config";

export class Orders extends BasePage {
    private _orderPage: Page | undefined;
    // private readonly _orderToAmend: Locator;
    // private readonly _uploadAmendOrder: Locator;
    // private readonly _EPOEndDate: Locator;
    // private readonly _orderTypeRadio: Locator;
    // private readonly _orderApproved: Locator;
    // private readonly _orderApplication: Locator;
    // private readonly _approvedHearing: Locator;
    // private readonly _issuingJudge: Locator;
    // private readonly _childInvolved: Locator;
    // private readonly _EPOrderType: Locator;
    // private readonly _finalOrder: Locator;
    // private readonly _orderPreviewLink: Locator;
    // private readonly _isExclusion: Locator;
    // private readonly _excluded: Locator;
    // private readonly _powerOfExclusionStart: Locator;
    // private readonly _judgemagistrateTitle: Locator;
    // private readonly _judgeLastName: Locator;
    // private readonly _judgeEmail: Locator;
    // private readonly _legalAdvisorName: Locator;
    // private readonly _orderFurtherDirectionDetails: Locator;
    // private readonly _closeOrder: Locator;
    // private readonly _careOrderIssuedDate: Locator;
    // private readonly _careOrderIssuedCourt: Locator;
    // private readonly _juridiction: Locator;
    // private readonly _juridictionRegion: Locator;
    // private readonly _approvalDate: Locator;
    // private readonly _childAccomadation: Locator;
    // private readonly _orderConsent: Locator;
    // private readonly _orderReason: Locator;
    // private readonly _childLegalAid: Locator;
    // private readonly _juridictionRadio: Locator;
    // private readonly _orderEndsOn: Locator;
    // private readonly _orderLength: Locator;
    // private readonly _assessmentStartDate: Locator;
    // private readonly _assessmentDuration: Locator;
    // private readonly _assessmentPlace: Locator;
    // private readonly _psychiatricAssessment: Locator;
    // private readonly _assessingBody: Locator;
    // private readonly _awayFromHome: Locator;
    // private readonly _awayfromDate: Locator;
    // private readonly _awayToDate: Locator;
    // private readonly _childFirstContact: Locator;
    // private readonly _childSecondContact: Locator;

    get orderPage(): Page {
        return <Page>this._orderPage;
    }

    get orderToAmend(): Locator {
        return this.page.getByLabel('Select order to amend');
    }

    get uploadAmendOrder(): Locator {
        return this.page.getByRole('textbox', {name: 'Upload the amended order. It will then be dated and stamped as amended.'});
    }

    get EPOEndDate(): Locator {
        return this.page.getByRole('group', {name: 'When does it end?'});
    }

    get orderTypeRadio(): Locator {
        return this.page.getByRole('group', {name: 'Select order'});
    }

    get orderApproved(): Locator {
        return this. page.getByRole('group', {name: 'Was the order approved at a'});
    }

    get orderApplication(): Locator {
        return this.page.getByRole('group', {name: 'Is there an application for'});
    }

    get approvedHearing(): Locator {
        return this.page.getByLabel('Which hearing?');
    }

    get issuingJudge(): Locator {
        return this.page.getByRole('group', {name: 'Is this judge issuing the'});
    }

    get childInvolved(): Locator {
        return this. page.getByRole('group', {name: 'Is the order about all the children?'});
    }

    get EPOrderType(): Locator {
        return this. page.getByRole('group', {name: 'Type of emergency protection'});
    }

    get finalOrder(): Locator {
        return this.page.getByRole('group', {name: 'Is this a final order?'});
    }

    get orderPreviewLink(): Locator {
        return this.page.getByRole('link', {name: 'Preview order.pdf'});
    }

    get isExclusion(): Locator {
        return this.page.getByRole('group', {name: 'Is there an exclusion'});
    }

    get excluded(): Locator {
        return this.page.getByLabel('Who\'s excluded');
    }

    get powerOfExclusionStart(): Locator {
        return this.page.getByRole('group', {name: 'Date power of exclusion starts'});
    }

    get judgemagistrateTitle(): Locator {
        return this.page.getByRole('group', {name: 'Judge or magistrate\'s title'});
    }

    get judgeLastName(): Locator {
        return this.page.getByLabel('Last name');
    }

    get judgeEmail(): Locator {
        return this.page.getByLabel('Email Address');
    }

    get legalAdvisorName(): Locator {
        return this.page.getByLabel('Justices\' Legal Adviser\'s');
    }

    get orderFurtherDirectionDetails(): Locator {
        return this.page.getByLabel('Add further directions, if');
    }

    get closeOrder(): Locator {
        return this.page.getByRole('group', {name: 'Does this order close the case?'});
    }

    get careOrderIssuedDate(): Locator {
        return this.page.getByRole('group', {name: 'When was the care order issued?'});
    }

    get careOrderIssuedCourt(): Locator {
        return this.page.getByLabel('Which court issued the order?');
    }

    get juridiction(): Locator {
        return this.page.getByRole('group', {name: 'Select jurisdiction'});
    }

    get juridictionRegion(): Locator {
        return this.page.locator('#manageOrdersCafcassOfficesWales');
    }

    get approvalDate(): Locator {
        return this.page.getByRole('group', { name: 'Approval Date' });
    }

    get childAccomadation(): Locator {
        return this.page.getByLabel('Which child is the order for?');
    }

    get orderConsent(): Locator {
        return this. page.getByRole('group', { name: 'Is order by consent?' });
    }

    get orderReason(): Locator {
        return this.page.getByRole('group', { name: 'Order given because the child is likely to' });
    }

    get childLegalAid(): Locator {
        return this.page.getByRole('group', { name: 'Does the child have a Legal' });
    }

    get juridictionRadio(): Locator {
        return this.page.getByRole('group', {name: 'Jurisdiction'});
    }

    get orderEndsOn(): Locator {
        return this.page.getByRole('group', {name: 'When does the order end?'});
    }

    get orderLength(): Locator {
        return this.page.getByLabel('Order length, in months');;
    }

    get assessmentStartDate(): Locator {
        return this.page.getByRole('group', { name: 'Assessment Start Date' });
    }

    get assessmentDuration(): Locator {
        return this. page.getByLabel('Duration of assessment (days)');
    }

    get assessmentPlace(): Locator {
        return this.page.getByLabel('Place of Assessment (Optional)');
    }

    get psychiatricAssessment(): Locator {
        return this.page.getByLabel('Psychiatric Assessment');
    }

    get assessingBody(): Locator {
        return this.page.getByLabel('Assessing Body');
    }

    get awayFromHome(): Locator {
        return this.page.getByRole('group', { name: 'Is child kept away from home?' });
    }

    get awayfromDate(): Locator {
        return this.page.getByRole('group', { name: 'From date' });
    }

    get awayToDate(): Locator {
        return this.page.getByRole('group', { name: 'To date' });
    }

    get childFirstContact(): Locator {
        return this.page.getByLabel('Child\'s first contact');
    }

    get childSecondContact(): Locator {
        return this.page.getByLabel('Child\'s second contact');
    }

    get childThirdContact(): Locator {
        return this.page.getByLabel('Child\'s third contact');
    }

    get costOrderDetails(): Locator {
        return this.page.getByLabel('Cost order details');;
    }

    get costOrder(): Locator {
        return this. page.getByRole('group', { name: 'Is there a costs order?' });
    }

    get orderTitle(): Locator {
        return this. page.getByLabel('Add order title (Optional)');
    }

    get orderDirectionDetails(): Locator {
        return this. page.getByLabel('Add order directions');
    }


    // private readonly _childThirdContact: Locator;
    // private readonly _costOrderDetails: Locator;
    // private readonly _costOrder: Locator;
    // private readonly _orderTitle: Locator;
    // private readonly _orderDirectionDetails: Locator

    // constructor(page: Page) {
    //     super(page);
    //     this._orderTypeRadio =
    //     this._orderApproved =
    //     this._orderApplication =
    //     this._approvedHearing =
    //     this._issuingJudge =
    //     this._judgemagistrateTitle =
    //     this._childInvolved =
    //     this._EPOrderType =
    //     this._EPOEndDate =
    //     this._finalOrder =
    //     this._orderPreviewLink =
    //     this._orderPage = page;
    //     this._isExclusion =
    //     this._excluded =
    //     this._powerOfExclusionStart =
    //     this._orderToAmend = ;
    //     this._uploadAmendOrder =
    //     this._judgeLastName = ;
    //     this._judgeEmail=
    //     this._legalAdvisorName =;
    //     this._orderFurtherDirectionDetails =
    //     this._closeOrder =
    //     this._careOrderIssuedDate =
    //     this._careOrderIssuedCourt =
    //     this._juridiction =
    //     this._juridictionRegion =
    //     this._approvalDate =
    //     this._childAccomadation =
    //     this._orderConsent =
    //     this._orderReason =
    //     this._childLegalAid =
    //     this._juridictionRadio =
    //     this._orderEndsOn =
    //     this._orderLength =
    //     this._assessmentStartDate =
    //     this._assessmentDuration =
    //     this._assessmentPlace =
    //     this._psychiatricAssessment =
    //     this._assessingBody =
    //     this._awayFromHome =
    //     this._awayfromDate =
    //     this._awayToDate =
    //     this._childFirstContact =
    //     this._childSecondContact =
    //     this._childThirdContact =
    //     this._costOrder =
    //     this._costOrderDetails =
    //     this._orderTitle =
    //     this._orderDirectionDetails =
    //
    //
    // }

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
        await this.addIssuingJudgeDetails('Yes');

        if (approvalDate == 'yes'){
            await this.approvalDate.getByRole('textbox', { name: 'Day' }).fill('04');
            await this.approvalDate.getByRole('textbox', { name: 'Month' }).fill('11');
            await this.approvalDate.getByRole('textbox', { name: 'Year' }).fill('2023');
            await this.approvalDate.getByLabel('Hour').fill('12');
            await this.approvalDate.getByLabel('Minute').fill('20');
            await this.approvalDate.getByLabel('Second').fill('20');
        }
    }

    async addIssuingJudgeDetails(hearingJudge: string){
        await this.issuingJudge.getByRole('radio', {name: `${hearingJudge}`}).check();
        await this.legalAdvisorName.fill('LA Marien Wester');
        if (hearingJudge =='No'){
            await this.judgemagistrateTitle.getByLabel('His Honour Judge').check();
            await this.judgeLastName.fill('John');
            await this.judgeEmail.fill('email@email.comLegal');
            await this.legalAdvisorName.fill('LA Jonathan');
        }
    }

    async addIssuningDeatilsOfUnApprovedOrder(){
        await this.orderApproved.getByLabel('No').click();
        await this.clickContinue();
        await expect.soft(this.page.getByText('Case assigned to: Her Honour')).toBeVisible();
        await this.addIssuingJudgeDetails('No');
    }

    async selectChildInvolved(){
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
        await this.orderFurtherDirectionDetails.fill('Direction on accomadation of the children\nNeed assistance for child1 sam');
    }

    async addC32BDischargeOfCareOrder(){
        await this.careOrderIssuedDate.getByRole('textbox', { name: 'Day' }).fill('3');
        await this.careOrderIssuedDate.getByRole('textbox', { name: 'Month' }).fill('4');
        await this.careOrderIssuedDate.getByRole('textbox', { name: 'Year' }).fill('2022');
        await this.careOrderIssuedCourt.selectOption('Swansea C&F Justice Centre');
        await this.orderFurtherDirectionDetails.fill('Remove the child from social care . The respondent is new gaurdian');
        await this.finalOrder.getByText('No').click();

    }

    async addC47AppointOfGuardianOrder(){
        await this.juridiction.getByRole('radio', { name: 'Wales' }).check();
        await this.juridictionRegion.selectOption('Swansea');
        await this.orderFurtherDirectionDetails.fill('Remove the child from the social care and appointing Aunty as guardian');
    }

    async addC26SecureAccomadation(){
        await this.orderConsent.getByLabel('No').check();
        await this.orderReason.getByLabel('abscond and suffer harm').check();
        await this.childLegalAid.getByLabel('Yes').check();
        await this.juridictionRadio.getByRole('radio', { name: 'Wales' }).check();
        await this.orderFurtherDirectionDetails.fill('Further Direction for give secure accommodation');
        await this.orderEndsOn.getByLabel('In a set number of months').check();
        await this.orderLength.fill('12');
        await this.finalOrder.getByLabel('No').check();
    }

    async addC39childAssessment(){
        await expect(this.page.getByText('Child assessment order (C39)')).toBeVisible();
        await this.finalOrder.getByLabel('Yes').check();
        await this.orderConsent.getByLabel('Yes').check();
        await this.assessmentStartDate.getByRole('textbox', { name: 'Day' }).fill('4');
        await this.assessmentStartDate.getByRole('textbox', { name: 'Month' }).fill('4');
        await this.assessmentStartDate.getByRole('textbox', { name: 'Year' }).fill('2024');
        await this.assessmentDuration.fill('1');
        await this.assessmentPlace.fill('London');
        await this.psychiatricAssessment.check();
        await this.assessingBody.fill('NHS pediatric specialist');
        await this.awayFromHome.getByLabel('Yes').check();
        await this.enterPostCode('EN5');
        await this.awayfromDate.getByLabel('Day').fill('2');
        await this.awayfromDate.getByLabel('Month').fill('2');
        await this.awayfromDate.getByLabel('Year').fill('2024');
        await this.awayToDate.getByLabel('Day').fill('2');
        await this.awayToDate.getByLabel('Month').fill('4');
        await this.awayToDate.getByLabel('Year').fill('2024');
        await this.childFirstContact.fill('Mother');
        await this.childSecondContact.fill('Father');
        await this.childThirdContact.fill('Aunty Joey');
        await this.costOrder.getByLabel('Yes').check();
        await this.costOrderDetails.fill('Cost to cover the  assessment and transportation');
    }
    async addC21BlankOrderDetails(){
        await this.orderTitle.fill('Prohibited Steps Order');
        await this.orderDirectionDetails.fill('Both father and mother have to get court permission before taking all the children out of country');
    }

    async closeTheOrder(close:string){
        await this.closeOrder.getByLabel(`${close}`).check();
    }

    async openOrderDoc(docLink: string) {
        const newPagePromise = this.page.context().waitForEvent('page');
        await this.page.getByRole('link', {name: `${docLink}`}).click();
        this._orderPage = await newPagePromise;
        await this._orderPage.waitForLoadState();
    }

    async uploadAmendedOrder() {
        await this.uploadAmendOrder.setInputFiles(config.testPdfFile);
        await this.waitForAllUploadsToBeCompleted();
    }
}
