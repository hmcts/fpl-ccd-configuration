import { expect, type Locator, type Page } from "@playwright/test";
import { BasePage } from "./base-page";
import config from "../settings/test-docs/config";

export class LogExpertReport extends BasePage {
    readonly logExpertRerort: Locator;
    readonly Day: Locator;
    readonly Month: Locator;
    readonly Year: Locator;
    readonly radioButton: Locator;
    readonly addNew: Locator;
    readonly typeOfReport: Locator;

    public constructor(page: Page) {
        super(page);
        this.logExpertRerort = page.getByRole('heading', { name: 'Log expert report', exact: true });
        this.addNew = page.getByRole('button', { name: 'Add new' });
        this.typeOfReport = page.getByLabel('What type of report have you');
        this.Day = page.getByRole('textbox', { name: 'Day' });
        this.Month = page.getByRole('textbox', { name: 'Month' });
        this.Year = page.getByRole('textbox', { name: 'Year' });
        this.radioButton = page.getByRole('radio', { name: 'No' });
    }
    public async logExpertReport() {
        await this.addNew.click();
        await this.page.waitForTimeout(1000);
        await this.typeOfReport.selectOption('Psychiatric - On child only');
        await this.Day.fill('14');
        await this.Month.fill('3');
        await this.Year.fill('2024');
        await this.radioButton.click();
        await this.radioButton.click();
        await this.clickSubmit();
        await this.saveAndContinue.click();
    }
};
