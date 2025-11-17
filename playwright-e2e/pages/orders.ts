import { BasePage } from "./base-page";
import { expect, Locator, Page } from "@playwright/test";
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
    readonly isAllChildrenInvolved: Locator;
    readonly EPOrderType: Locator;
    readonly finalOrder: Locator;
    readonly orderPreviewLink: Locator;
    readonly isExclusion: Locator;
    readonly excluded: Locator;
    readonly powerOfExclusionStart: Locator;
    readonly judgeMagistrateTitle: Locator;
    readonly judgeLastName: Locator;
    readonly judgeEmail: Locator;
    readonly legalAdvisorName: Locator;
    readonly orderFurtherDirectionDetails: Locator;
    readonly closeOrder: Locator;
    readonly careOrderIssuedDate: Locator;
    readonly careOrderIssuedCourt: Locator;
    readonly jurisdiction: Locator;
    readonly jurisdictionRegion: Locator;
    readonly approvalDate: Locator;
    readonly childAccommodation: Locator;
    readonly orderConsent: Locator;
    readonly orderReason: Locator;
    readonly childLegalAid: Locator;
    readonly jurisdictionRadio: Locator;
    readonly orderEndsOn: Locator;
    readonly orderLength: Locator;
    readonly assessmentStartDate: Locator;
    readonly assessmentDuration: Locator;
    readonly assessmentPlace: Locator;
    readonly psychiatricAssessment: Locator;
    readonly assessingBody: Locator;
    readonly awayFromHome: Locator;
    readonly awayfromDate: Locator;
    readonly awayToDate: Locator;
    readonly permissionReport: Locator;
    readonly childFirstContact: Locator;
    readonly childSecondContact: Locator;
    readonly childThirdContact: Locator;
    readonly costOrderDetails: Locator;
    readonly costOrder: Locator;
    readonly orderTitle: Locator;
    readonly orderDirectionDetails: Locator
    readonly radioNoButton: Locator;
    readonly firstFamilyBefriended: Locator;
    readonly secondFamilyBefriended: Locator;
    readonly thirdFamilyBefriended: Locator;
    readonly dateChosen: Locator;
    readonly day: Locator;
    readonly month: Locator;
    readonly year: Locator;
    readonly futherDirections: Locator;
    readonly applicationOrder: Locator;
    readonly addExclusionDetails: Locator;
    readonly endOfProceedings: Locator;
    readonly endDate: Locator;
    readonly applications: Locator;
    readonly childInOrder: Locator;
    readonly parentalResponsibilty: Locator;
    readonly relationToChild: Locator;
    readonly specialGuardianOne: Locator;
    readonly partyGrantedLeave: Locator;
    readonly newSurname: Locator;

    constructor(page: Page) {
        super(page);
        this.orderTypeRadio = page.getByRole('group', { name: 'Select order' });
        this.orderApproved = page.getByRole('group', { name: 'Was the order approved at a' });
        this.orderApplication = page.getByRole('group', { name: 'Is there an application for' });
        this.approvedHearing = page.getByLabel('Which hearing?');
        this.issuingJudge = page.getByRole('group', { name: 'Is this judge issuing the' });
        this.judgeMagistrateTitle = page.getByRole('group', { name: 'Judge or magistrate\'s title' });
        this.isAllChildrenInvolved = page.getByRole('group', { name: 'Is the order about all the children?' })
        this.EPOrderType = page.getByRole('group', { name: 'Type of emergency protection' });
        this.EPOEndDate = page.getByRole('group', { name: 'When does it end?' });
        this.finalOrder = page.getByRole('group', { name: 'Is this a final order?' });
        this.orderPreviewLink = page.getByRole('link', { name: 'Preview order.pdf' });
        this.orderPage = page;
        this.isExclusion = page.getByRole('group', { name: 'Is there an exclusion' });
        this.excluded = page.getByLabel('Who\'s excluded');
        this.powerOfExclusionStart = page.getByRole('group', { name: 'Date power of exclusion starts' });
        this.orderToAmend = page.getByLabel('Select order to amend');
        this.uploadAmendOrder = page.getByRole('button', { name: 'Upload the amended order. It will then be dated and stamped as amended.' });
        this.judgeLastName = page.getByLabel('Last name');
        this.judgeEmail = page.getByLabel('Email Address');
        this.legalAdvisorName = page.getByLabel('Justices\' Legal Adviser\'s');
        this.orderFurtherDirectionDetails = page.getByLabel('Add further directions, if');
        this.closeOrder = page.getByRole('group', { name: 'Does this order close the case?' });
        this.careOrderIssuedDate = page.getByRole('group', { name: 'When was the care order issued?' });
        this.careOrderIssuedCourt = page.getByLabel('Which court issued the order?');
        this.jurisdiction = page.getByRole('group', { name: 'Select jurisdiction' });
        this.jurisdictionRegion = page.locator('#manageOrdersCafcassOfficesWales');
        this.approvalDate = page.getByRole('group', { name: 'Approval Date' });
        this.childAccommodation = page.getByLabel('Which child is the order for?');
        this.orderConsent = page.getByRole('group', { name: 'Is order by consent?' });
        this.orderReason = page.getByRole('group', { name: 'Order given because the child is likely to' });
        this.childLegalAid = page.getByRole('group', { name: 'Does the child have a Legal' });
        this.jurisdictionRadio = page.getByRole('group', { name: 'Jurisdiction' });
        this.orderEndsOn = page.getByRole('group', { name: 'When does the order end?' });
        this.orderLength = page.getByLabel('Order length, in months');
        this.assessmentStartDate = page.getByRole('group', { name: 'Assessment Start Date' });
        this.assessmentDuration = page.getByLabel('Duration of assessment (days)');
        this.assessmentPlace = page.getByLabel('Place of Assessment (Optional)');
        this.psychiatricAssessment = this.page.getByLabel('Psychiatric Assessment');
        this.assessingBody = page.getByLabel('Assessing Body');
        this.awayFromHome = page.getByRole('group', { name: 'Is child kept away from home?' });
        this.awayfromDate = this.page.getByRole('group', { name: 'From date' });
        this.awayToDate = page.getByRole('group', { name: 'To date' });
        this.childFirstContact = page.getByLabel('Child\'s first contact');
        this.childSecondContact = page.getByLabel('Child\'s second contact');
        this.childThirdContact = page.getByLabel('Child\'s third contact');
        this.costOrder = page.getByRole('group', { name: 'Is there a costs order?' });
        this.costOrderDetails = page.getByLabel('Cost order details');
        this.orderTitle = page.getByLabel('Add order title (Optional)');
        this.orderDirectionDetails = page.getByLabel('Add order directions');
        this.radioNoButton = page.getByRole('radio', { name: 'No' });
        this.dateChosen = page.getByRole('radio', { name: 'Date to be chosen' });
        this.permissionReport = page.getByRole('group', { name: 'Permission to report is not' });
        this.firstFamilyBefriended = page.getByLabel('First party to be befriended');
        this.secondFamilyBefriended = page.getByLabel('Second party to be befriended');
        this.thirdFamilyBefriended = page.getByLabel('Third party to be befriended');
        this.day = page.getByRole('textbox', { name: 'Day' });
        this.month = page.getByRole('textbox', { name: 'Month' });
        this.year = page.getByRole('textbox', { name: 'Year' });
        this.futherDirections = page.getByRole('textbox', { name: 'Add further directions, if' });
        this.applicationOrder = page.getByRole('group', { name: 'Is there an application for' });
        this.addExclusionDetails = page.getByRole('textbox', { name: 'Add exclusion details' });
        this.endOfProceedings = page.getByRole('radio', { name: 'The end of proceedings' });
        this.endDate = page.getByRole('group', { name: 'End Date' });
        this.applications = page.getByLabel('Applications');
        this.childInOrder = page.getByRole('group', { name: 'Who’s included in the order?' });
        this.parentalResponsibilty = page.getByRole('textbox', { name: 'Who\'s been given parental' });
        this.relationToChild = page.getByRole('radio', { name: 'Father' });
        this.specialGuardianOne = page.getByRole('group', { name: 'Person 1 (Optional)' });
        this.partyGrantedLeave = page.getByRole('textbox', { name: 'Party granted leave' });
        this.newSurname = page.getByRole('textbox', { name: 'Child/Children\'s new surname' });

    }

    async selectOrderOperation(toDo: string) {
        await this.page.getByRole('radio', { name: `${toDo}` }).click();
    }

    async selectOrder(orderType: string) {
        await this.orderTypeRadio.getByLabel(`${orderType}`, { exact: true }).check();

    }

    async addIssuingDetailsOfApprovedOrder(approvalDate: string) {
        await this.orderApproved.getByLabel('Yes').click();
        await this.approvedHearing.selectOption('Case management hearing, 3 November 2012');
        await this.orderApplication.getByLabel('No').click();
        await this.clickContinue();
        await this.addIssuingJudgeDetails('Yes');

        if (approvalDate == 'yes') {
            await this.approvalDate.getByRole('textbox', { name: 'Day' }).fill('04');
            await this.approvalDate.getByRole('textbox', { name: 'Month' }).fill('11');
            await this.approvalDate.getByRole('textbox', { name: 'Year' }).fill('2023');
            await this.approvalDate.getByLabel('Hour').fill('12');
            await this.approvalDate.getByLabel('Minute').fill('20');
            await this.approvalDate.getByLabel('Second').fill('20');
        }
    }

    async addIssuingJudgeDetails(hearingJudge: string) {
        await this.issuingJudge.getByRole('radio', { name: `${hearingJudge}` }).check();
        await this.legalAdvisorName.fill('LA Marien Wester');
        if (hearingJudge == 'No') {
            await this.judgeMagistrateTitle.getByLabel('His Honour Judge').check();
            await this.judgeLastName.fill('John');
            await this.judgeEmail.fill('email@email.comLegal');
            await this.legalAdvisorName.fill('LA Jonathan');
        }
    }

    async addIssuningDeatilsOfUnApprovedOrder() {
        await this.orderApproved.getByLabel('No').click();
        await this.clickContinue();
        await expect.soft(this.page.getByText('Case assigned to: Her Honour')).toBeVisible();
        await this.addIssuingJudgeDetails('No');
    }

    async selectChildInvolved() {
        await this.childAccommodation.selectOption('Timothy Jones');
    }

    async addChildDetails(isAllChildrenInvolved: string) {
        await this.isAllChildrenInvolved.getByRole('radio', { name: `${isAllChildrenInvolved}` }).click();
        if (isAllChildrenInvolved == 'No') {
            await this.page.getByRole('group', { name: 'Child 1 (Optional)' }).getByLabel('Yes').check();
            await this.page.getByRole('group', { name: 'Child 2 (Optional)' }).getByLabel('Yes').check();
            await this.page.getByRole('group', { name: 'Child 4 (Optional)' }).getByLabel('Yes').check();
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

        await this.page.getByRole('group', { name: 'Include: "Any person who can produce the children to the applicant must do so"' }).getByLabel('Yes').click();
        await this.page.getByLabel('Add description of children (').fill('Children description');
        await this.page.getByLabel('Add further directions, if').fill('Furhter direction\nto the applicant \nto take care of children');
        await this.EPOEndDate.getByRole('textbox', { name: 'Day' }).fill('2');
        await this.EPOEndDate.getByRole('textbox', { name: 'Month' }).fill('10');
        await this.EPOEndDate.getByRole('textbox', { name: 'Year' }).fill('2013');
        await this.EPOEndDate.getByRole('textbox', { name: 'Hour' }).fill('10');
        await this.finalOrder.getByLabel('Yes').click();
    }
    async addC32CareOrder() {
        await this.orderFurtherDirectionDetails.fill('Direction on accomadation of the children\nNeed assistance for child1 sam');
    }

    async addC32BDischargeOfCareOrder() {
        await this.careOrderIssuedDate.getByRole('textbox', { name: 'Day' }).fill('3');
        await this.careOrderIssuedDate.getByRole('textbox', { name: 'Month' }).fill('4');
        await this.careOrderIssuedDate.getByRole('textbox', { name: 'Year' }).fill('2022');
        await this.careOrderIssuedCourt.selectOption('Swansea C&F Justice Centre');
        await this.orderFurtherDirectionDetails.fill('Remove the child from social care . The respondent is new gaurdian');
        await this.finalOrder.getByText('No').click();

    }

    async addC47AppointOfGuardianOrder() {
        await this.jurisdiction.getByRole('radio', { name: 'Wales' }).check();
        await this.jurisdictionRegion.selectOption('Swansea');
        await this.orderFurtherDirectionDetails.fill('Remove the child from the social care and appointing Aunty as guardian');
    }

    async addC26SecureAccomadation() {
        await this.orderConsent.getByLabel('No').check();
        await this.orderReason.getByLabel('abscond and suffer harm').check();
        await this.childLegalAid.getByLabel('Yes').check();
        await this.jurisdictionRadio.getByRole('radio', { name: 'Wales' }).check();
        await this.orderFurtherDirectionDetails.fill('Further Direction for give secure accommodation');
        await this.orderEndsOn.getByLabel('In a set number of months').check();
        await this.orderLength.fill('12');
        await this.finalOrder.getByLabel('No').check();
    }

    async addC39childAssessment() {
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
    async addC21BlankOrderDetails() {
        await this.orderTitle.fill('Prohibited Steps Order');
        await this.orderDirectionDetails.fill('Both father and mother have to get court permission before taking all the children out of country');
    }

    async closeTheOrder(close: string) {
        await this.closeOrder.getByLabel(`${close}`).check();
    }

    async openOrderDoc(docLink: string) {
        const newPagePromise = this.page.context().waitForEvent('page');
        await this.page.getByRole('button', { name: `${docLink}` }).click();
        this.orderPage = await newPagePromise;
        await this.orderPage.waitForLoadState();
    }

    async uploadAmendedOrder() {
        await this.uploadAmendOrder.setInputFiles(config.testPdfFile);
        await this.waitForAllUploadsToBeCompleted();
    }

    async ctscUploadsTransparencyOrder() {
        await this.issuingJudge.getByLabel('Yes').check();
        await this.clickContinue();
        await this.orderConsent.getByLabel('Yes').check();
        await this.finalOrder.getByLabel('No').check();
        await this.dateChosen.check();
        await this.endDate.getByLabel('Day').fill('11');
        await this.endDate.getByLabel('Month').fill('11');
        await this.endDate.getByLabel('Year').fill('2030');
        await this.permissionReport.getByLabel('Day').fill('12');
        await this.permissionReport.getByLabel('Month').fill('12');
        await this.permissionReport.getByLabel('Year').fill('2031');
    }

    async judgeUploadsTransparencyOrder() {
        await this.issuingJudge.getByLabel('Yes').check();
        await this.clickContinue();
        await this.orderConsent.getByLabel('Yes').check();
        await this.finalOrder.getByLabel('No').check();
        await this.dateChosen.check();
        await this.endDate.getByLabel('Day').fill('11');
        await this.endDate.getByLabel('Month').fill('07');
        await this.endDate.getByLabel('Year').fill('2030');
        await this.permissionReport.getByLabel('Day').fill('10');
        await this.permissionReport.getByLabel('Month').fill('08');
        await this.permissionReport.getByLabel('Year').fill('2020');

    }

    async ctscFamilyAssistanceOrder() {
        await expect(this.page.getByText(' Add issuing details', { exact: true })).toBeVisible();
        await this.issuingJudge.getByLabel('Yes').check();
        await this.clickContinue();
        await this.isAllChildrenInvolved.getByLabel('Yes').check();
        await this.clickContinue();
        await this.firstFamilyBefriended.selectOption('John Black');
        await this.secondFamilyBefriended.selectOption('Joe Bloggs');
        await this.thirdFamilyBefriended.selectOption('Sarah Black');
        await this.day.fill('07');
        await this.month.fill('08');
        await this.year.fill('2025');
        await this.orderConsent.getByLabel('Yes').click();
        await this.orderConsent.getByLabel('Yes').click(); // checkbox not clicking had to work around it
        await this.futherDirections.fill('test');
        await this.finalOrder.getByLabel('No').check();
    }

    async judgeUploadsFamilyAssistanceOrder() {
        await expect(this.page.getByText(' Add issuing details', { exact: true })).toBeVisible();
        await this.issuingJudge.getByLabel('Yes').check();
        await this.clickContinue();
        await this.isAllChildrenInvolved.getByLabel('Yes').check();
        await this.clickContinue();
        await this.firstFamilyBefriended.selectOption('John Black');
        await this.secondFamilyBefriended.selectOption('Sarah Black');
        await this.thirdFamilyBefriended.selectOption('Joe Bloggs');
        await this.day.fill('07');
        await this.month.fill('08');
        await this.year.fill('2025');
        await this.orderConsent.getByLabel('Yes').click();
        await this.orderConsent.getByLabel('Yes').click();
        await this.futherDirections.fill('test');
        await this.finalOrder.getByLabel('No').check();
    }

    async ctscUploadsInterimCareOrder() {
        await this.orderApproved.getByLabel('No').check();
        await this.applicationOrder.getByLabel('No').check();
        await this.clickContinue();
        await this.issuingJudge.getByLabel('Yes').check();
        await this.clickContinue();
        await this.isAllChildrenInvolved.getByLabel('Yes').check();
        await this.clickContinue();
        await this.radioNoButton.click();
        await this.endOfProceedings.check();
    }

    async judgeUploadsInterimCareOrder() {
        await this.orderApproved.getByLabel('No').check();
        await this.applicationOrder.getByLabel('No').check();
        await this.clickContinue();
        await this.issuingJudge.getByLabel('Yes').check();
        await this.clickContinue();
        await this.isAllChildrenInvolved.getByLabel('Yes').check();
        await this.clickContinue();
        await this.radioNoButton.check();
        await this.endOfProceedings.check();
    }

    async uploadsInterimSupervisionOrder() {
        await this.clickContinue();
        await this.orderApproved.getByLabel('Yes').check();
        await this.approvedHearing.selectOption('Case management hearing, 3 November 2012');
        await this.orderApplication.getByLabel('Yes').check();
        await this.applications.selectOption('C2, 25 March 2021, 3:16pm');
        await this.clickContinue();
        await this.clickContinue();
        await this.isAllChildrenInvolved.getByLabel('Yes').check();
        await this.clickContinue();
        await this.orderFurtherDirectionDetails.fill('Test');
        await this.endOfProceedings.check();
    }

    async assertuploadOrderType() {
        await expect.soft(this.page.getByText('Appointment of a guardian (C46A)', { exact: true })).toBeVisible();
        await expect.soft(this.page.getByText('Appointment of a solicitor (C48A)', { exact: true })).toBeVisible();
        await expect.soft(this.page.getByText('Authority to search for a child (C31)', { exact: true })).toBeVisible();
        await expect.soft(this.page.getByText('Authority to search for another child (C27)', { exact: true })).toBeVisible();
        await expect.soft(this.page.getByText('Discharge education supervision order (C38A)', { exact: true })).toBeVisible();
        await expect.soft(this.page.getByText('Discharge of parental responsibility (C45B)', { exact: true })).toBeVisible();
        await expect.soft(this.page.getByText('Extension of an education supervision order (C38B)', { exact: true })).toBeVisible();
        await expect.soft(this.page.getByText('Leave to remove a child from the UK (C44B)', { exact: true })).toBeVisible();
        await expect.soft(this.page.getByText('Power of arrest (FL406)', { exact: true })).toBeVisible();
        await expect.soft(this.page.getByText('Refusal of appointment of a children\'s guardian (C47B)', { exact: true })).toBeVisible();
        await expect.soft(this.page.getByText('Refusal of appointment of a solicitor (C48B)', { exact: true })).toBeVisible();
        await expect.soft(this.page.getByText('Refusal of contact with a child in care (C34B)', { exact: true })).toBeVisible();
        await expect.soft(this.page.getByText('Refusal to transfer proceedings (C50)', { exact: true })).toBeVisible();
        await expect.soft(this.page.getByText('Termination of appointment of a children\'s guardian (C47C)', { exact: true })).toBeVisible();
        await expect.soft(this.page.getByText('Termination of appointment of a solicitor (C48C)', { exact: true })).toBeVisible();
        await expect.soft(this.page.getByText('Termination of guardian\'s appointment (C46B)', { exact: true })).toBeVisible();
        await expect.soft(this.page.getByText('To disclose information about the whereabouts of a missing child (C30)', { exact: true })).toBeVisible();
        await expect.soft(this.page.getByText('Transfer out Children Act (C49)', { exact: true })).toBeVisible();
        await expect.soft(this.page.getByText('Variation of Emergency protection order (C24)', { exact: true })).toBeVisible();
        await expect.soft(this.page.getByText('Warrant to assist (C28)', { exact: true })).toBeVisible();
        await expect.soft(this.page.getByText('Warrant to assist EPO (C25)', { exact: true })).toBeVisible();
        await expect.soft(this.page.getByText('Other', { exact: true })).toBeVisible();
    }

    async addIssuingDetailsOfUploadedOrder(approvalDate: Date) {
        await this.fillDateInputs(this.page, new Date(new Date().setMonth(new Date().getMonth() + 3)));
        await this.clickContinue();
        await expect(this.page.getByText('Errors', { exact: true })).toBeVisible();
        await expect(this.page.getByText('Approval date cannot not be in the future')).toBeVisible();
        await this.fillDateInputs(this.page, approvalDate);
    }

    async enterOrderName() {
        await this.page.locator('#manageOrdersUploadTypeOtherTitle').fill('Uploaded Other Order');
    }

    async uploadOrder(isSealed: string) {

        await this.page.setInputFiles('#manageOrdersUploadOrderFile', config.testPdfFile);
        await this.waitForAllUploadsToBeCompleted();
        await this.page.getByRole('radio', { name: `${isSealed}` }).check();
    }

    async assertOrderSealScreenshot() {
        await this.orderPage.waitForLoadState('domcontentloaded');
        await this.orderPage.waitForTimeout(1000);
        await expect(this.orderPage).toHaveScreenshot({
            fullPage: true,
            threshold: 0.2, // Allow small differences
            maxDiffPixels: 1500, // Allow up to 1500 different pixels
            clip: { x: 0, y: 0, width: 1280, height: 720 } // Fixed dimensions});
        });

    }

    async uploadsParentalResponsibiltyOrder() {
        await this.clickContinue();
        await this.orderApproved.getByLabel('No').check();
        await this.clickContinue();
        await this.issuingJudge.getByLabel('Yes').check();
        await this.clickContinue();
        await this.isAllChildrenInvolved.getByLabel('Yes').check();
        await this.clickContinue();
        await this.orderConsent.getByLabel('Yes').check();
        await this.parentalResponsibilty.fill('Ross Tray');
        await this.relationToChild.check();
        await this.orderFurtherDirectionDetails.fill('Test');
        await this.finalOrder.getByLabel('No').check();
    }

    async uploadsSpecialGuardianshipOrder() {
        await this.clickContinue();
        await this.orderApproved.getByLabel('No').check();
        await this.clickContinue();
        await this.issuingJudge.getByLabel('Yes').check();
        await this.clickContinue();
        await this.isAllChildrenInvolved.getByLabel('Yes').check();
        await this.clickContinue();
        await this.orderConsent.getByLabel('Yes').check();
        await this.specialGuardianOne.getByRole('checkbox', { name: 'Yes' }).click()
        await this.finalOrder.getByLabel('No').check();
        await this.clickContinue();

    }

    async uploadsLeaveToChangeSurname() {
        await this.clickContinue();
        await this.issuingJudge.getByLabel('Yes').check();
        await this.clickContinue();
        await this.isAllChildrenInvolved.getByLabel('Yes').check();
        await this.clickContinue();
        await this.orderConsent.getByLabel('Yes').check();
        await this.finalOrder.getByLabel('No').check();
        await this.partyGrantedLeave.fill('Jason');
        await this.newSurname.fill('Fredrick');
        await this.clickContinue();
    }
}

