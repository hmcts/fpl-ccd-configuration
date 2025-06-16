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
    readonly orderFurtherDirectionDetails: Locator;
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
    readonly assessmentStartDate: Locator;
    readonly assessmentDuration: Locator;
    readonly assessmentPlace: Locator;
    readonly psychiatricAssessment: Locator;
    readonly assessingBody: Locator;
    readonly awayFromHome: Locator;
    readonly awayfromDate: Locator;
    readonly awayToDate: Locator;
    readonly childFirstContact: Locator;
    readonly childSecondContact: Locator;
    readonly childThirdContact: Locator;
    readonly costOrderDetails: Locator;
    readonly costOrder: Locator;
    readonly orderTitle: Locator;
    readonly orderDirectionDetails: Locator

    async ctscUploadsTransparencyOrder() {
        await this.issuingJudge.getByLabel('Yes').check();
        await this.clickContinue();
        await this.orderConsent.getByLabel('Yes').check();
        await this.finalOrder.getByLabel('No').check();
        await this.dateChosen.check();
        await this.endDate.getByLabel('Day').fill('12');
        await this.endDate.getByLabel('Month').fill('12');
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
        await this.endDate.getByLabel('Day').fill('12');
        await this.endDate.getByLabel('Month').fill('12');
        await this.endDate.getByLabel('Year').fill('2030');
        await this.permissionReport.getByLabel('Day').fill('09');
        await this.permissionReport.getByLabel('Month').fill('10');
        await this.permissionReport.getByLabel('Year').fill('2031');

    async ctscFamilyAssistanceOrder() {
        await expect(this.page.getByText(' Add issuing details', { exact: true })).toBeVisible();
        await this.issuingJudge.getByLabel('Yes').check();
        await this.page.pause();
        await this.clickContinue();
        await expect(this.page.getByText(' Family assistance order (C42)', { exact: true })).toBeVisible();
        await this.page.pause();
        await this.childInvolved.getByLabel('Yes').check();
        await this.page.pause();
        await this.clickContinue();
        await expect(this.page.getByText(' Family assistance order (C42)', { exact: true })).toBeVisible();
        await this.firstPartyBefriended.selectOption('John Black');
        await this.secondPartyBefriended.selectOption('Joe Bloggs');
        await this.thirdPartyBefriended.selectOption('Sarah Black');
        await this.endDateDay.fill('07');
        await this.endDateMonth.fill('08');
        await this.endDateYear.fill('2025');
        await this.orderConsent.getByLabel('Yes').click();
        await this.orderConsent.getByLabel('Yes').click(); // checkbox not clicking had to work around it
        await this.addFutherDirection.fill('test');
        await this.finalOrder.getByLabel('No').check();
    }

    async judgeFamilyAssistanceOrder() {
        await expect(this.page.getByText(' Add issuing details', { exact: true })).toBeVisible();
        await this.issuingJudge.getByLabel('Yes').check();
        await this.page.pause();
        await this.clickContinue();
        await expect(this.page.getByText(' Family assistance order (C42)', { exact: true })).toBeVisible();
        await this.childInvolved.getByLabel('Yes').check();
        await this.clickContinue();
        await expect(this.page.getByText(' Family assistance order (C42)', { exact: true })).toBeVisible();
        await this.firstPartyBefriended.selectOption('John Black');
        await this.secondPartyBefriended.selectOption('Sarah Black');
        await this.thirdPartyBefriended.selectOption('Joe Bloggs');
        await this.endDateDay.fill('07');
        await this.endDateMonth.fill('08');
        await this.endDateYear.fill('2025');
        await this.orderConsent.getByLabel('Yes').click();
        await this.addFutherDirection.fill('test');
        await this.finalOrder.getByLabel('No').check();
    }
}
