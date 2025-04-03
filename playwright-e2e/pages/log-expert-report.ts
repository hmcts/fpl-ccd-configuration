import {type Locator} from "@playwright/test";
import {BasePage} from "./base-page";

export class LogExpertReport extends BasePage {

    get Day(): Locator {
        return this.page.getByRole('textbox', {name: 'Day'});
    }

    get Month(): Locator {
        return this.page.getByRole('textbox', {name: 'Month'});
    }

    get Year(): Locator {
        return this.page.getByRole('textbox', {name: 'Year'});
    }

    get radioButton(): Locator {
        return this.page.getByRole('radio', {name: 'No'});
    }

    get addNew(): Locator {
        return this.page.getByRole('button', {name: 'Add new'});
    }

    get typeOfReport(): Locator {
        return this.page.getByLabel('What type of report have you');
    }

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
    }
}
