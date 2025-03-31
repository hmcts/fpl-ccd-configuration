import { expect, type Locator, type Page } from "@playwright/test";
import { BasePage } from "./base-page";
import config from "../settings/test-docs/config";

export class LogExpertReport extends BasePage {
    get logExpertRerort(): Locator {
        return this.page.getByRole('heading', { name: 'Log expert report', exact: true });
    }

    get Day(): Locator {
        return this.page.getByRole('textbox', { name: 'Day' });
    }

    get Month(): Locator {
        return this.page.getByRole('textbox', { name: 'Month' });
    }

    get Year(): Locator {
        return this.page.getByRole('textbox', { name: 'Year' });
    }

    get radioButton(): Locator {
        return this.page.getByRole('radio', { name: 'No' });
    }

    get addNew(): Locator {
        return this.page.getByRole('button', { name: 'Add new' });
    }

    get typeOfReport(): Locator {
        return this.page.getByLabel('What type of report have you');
    }
    // private readonly _logExpertRerort: Locator;
    // private readonly _Day: Locator;
    // private readonly _Month: Locator;
    // private readonly _Year: Locator;
    // private readonly _radioButton: Locator;
    // private readonly _addNew: Locator;
    // private readonly _typeOfReport: Locator;

    // public constructor(page: Page) {
    //     super(page);
    //     this._logExpertRerort =
    //     this._addNew =
    //     this._typeOfReport =
    //     this._Day =
    //     this._Month =
    //     this._Year =
    //     this._radioButton =
    // }
    public async logExpertReport() {
        await this.addNew.click();
        await this.typeOfReport.selectOption('Psychiatric - On child only');
        await this.Day.fill('14');
        await this.Month.fill('3');
        await this.Year.fill('2024');
        await this.radioButton.click();
        await this.radioButton.click();
        await this.clickSubmit();
        await this.checkYourAnsAndSubmit();
       // await this.saveAndContinue.click();
    }
};
