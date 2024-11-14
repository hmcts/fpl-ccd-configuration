import { expect, type Locator, type Page } from "@playwright/test";
import { BasePage } from "./base-page";
import config from "../settings/test-docs/config";

export class LogExpertReport extends BasePage {
    readonly logExpertRerort: Locator;
    readonly button: Locator;
    readonly Day: Locator;
    readonly Month: Locator;
    readonly Year: Locator;
    readonly hasItBeenApproved: Locator;
    readonly dateApproved: Locator;
    readonly submitButton: Locator;
    readonly radio: Locator;
    readonly addNew: Locator;
    readonly typeOfReport: Locator;
    readonly saveAndContinue: Locator;

    public constructor(page: Page) {
        super(page);
        this.logExpertRerort = page.getByRole('heading', { name: 'Log expert report', exact: true });
        this.button = page.getByRole('button', { name: 'Go' });
        this.addNew = page.getByRole('button', { name: 'Add new' });
        this.typeOfReport = page.getByLabel('What type of report have you');
        this.hasItBeenApproved = page.getByRole('heading', { name: 'Has it been approved?' })
        this.Day = page.getByRole('textbox', { name: 'Day' });
        this.Month = page.getByRole('textbox', { name: 'Month' });
        this.Year = page.getByRole('textbox', { name: 'Year' });
        this.dateApproved = page.getByRole('heading', { name: 'Date approved' })
        this.radio = page.getByRole('radio', { name: 'No' });
        this.submitButton = page.getByRole('button', { name: 'Submit' });
        this.saveAndContinue = page.getByRole('button', { name: 'Save and continue' });

    }

    public async logExpertReport() {
        await this.addNew.click();
        await this.typeOfReport.selectOption('Psychiatric - On child only');
        await this.Day.fill('02');
        await this.Month.fill('03');
        await this.Year.fill('2025');
        await this.radio.getByRole('radio', { name: 'No' });
        await this.submitButton.click();
        await this.saveAndContinue.click();
    }
};
