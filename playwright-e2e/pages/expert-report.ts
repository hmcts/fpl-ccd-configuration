import {type Page, type Locator, expect} from "@playwright/test";
import {BasePage} from "./base-page";

export class ExpertReport extends BasePage {


    get addNewButton(): Locator {
        return this.page.getByRole('button', {name: 'Add new'})
    }

    get dateRequested(): Locator {
        return this.page.getByRole('group', {name: 'Date requested'});
    }

    get dateApproved(): Locator {
        return this.page.getByRole('group', {name: 'Date approved'});
    }

    get reportApprovedYes(): Locator {
        return this.page.getByRole('group', {name: 'Has it been approved? (Optional)'}).getByLabel('Yes')
    }

    constructor(page: Page) {
        super(page);
    }

    async selectExpertReportType(type: string, reportNumber: number = 0) {
        await this.page.getByText('What type of report have you requested?').nth(reportNumber).selectOption({label: type});
    }


    async addNewReport(reportNumber: number) {
        await this.addNewButton.nth(reportNumber).focus();
        await this.addNewButton.nth(reportNumber).click();
        await expect(this.addNewButton.nth(1)).toBeVisible();

    }

    async checkDateValidationPass() {
        await this.page.press('body', 'Tab');
        await expect(this.page.getByText(' The data entered is not valid for Date requested ')).toBeHidden()
    }

    async enterRequestedDate(requestDate: Date, reportNumber: number = 0) {
        await this.dateRequested.getByLabel('Day').nth(reportNumber).fill(new Intl.DateTimeFormat('en', {day: 'numeric'}).format(requestDate));
        await this.dateRequested.getByLabel('Month').nth(reportNumber).fill(new Intl.DateTimeFormat('en', {month: 'numeric'}).format(requestDate));
        await this.dateRequested.getByLabel('Year').nth(reportNumber).fill(new Intl.DateTimeFormat('en', {year: 'numeric'}).format(requestDate));
    }

    async enterApprovedDate(approvedDate: Date, reportNumber: number = 0) {
        await this.dateApproved.nth(reportNumber).getByLabel('Day').fill(new Intl.DateTimeFormat('en', {day: 'numeric'}).format(approvedDate));
        await this.dateApproved.nth(reportNumber).getByLabel('Month').fill(new Intl.DateTimeFormat('en', {month: 'numeric'}).format(approvedDate));
        await this.dateApproved.nth(reportNumber).getByLabel('Year').fill(new Intl.DateTimeFormat('en', {year: 'numeric'}).format(approvedDate));

    }

    async orderApprovedYes(reportNumber: number = 0) {
        await this.reportApprovedYes.nth(reportNumber).click();
        await this.reportApprovedYes.nth(reportNumber).click();

    }
}
