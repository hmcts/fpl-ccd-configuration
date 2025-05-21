import { type Page, type Locator, expect } from "@playwright/test";
import { BasePage } from "./base-page";

export class ChangeOtherToRespondent extends BasePage {
    readonly changeOtherToRespondent: Locator;
    readonly giveNotice: Locator;
    readonly firstName: Locator;
    readonly lastName: Locator;
    readonly dobDay: Locator;
    readonly dobMonth: Locator;
    readonly dobYear: Locator;
    readonly gender: Locator;
    readonly placeOfBirth: Locator;
    readonly legalRepresentation: Locator;

    constructor(page: Page) {
        super(page);
        this.changeOtherToRespondent = page.getByRole('heading', { name: 'Change other to respondent', exact: true });
        this.giveNotice = page.getByLabel('Select one of the others to');
        this.firstName = page.getByLabel('First name (Optional)', { exact: true });
        this.lastName = page.getByLabel('Last name (Optional)', { exact: true });
        this.dobDay = page.getByLabel('Day');
        this.dobMonth = page.getByLabel('Month');
        this.dobYear = page.getByLabel('Year');
        this.gender = page.getByLabel('What is the respondent\'s gender? (Optional)');
        this.placeOfBirth = page.getByLabel('Place of birth (Optional)');
        this.legalRepresentation = page.getByRole('group', { name: 'Do they have legal representation?' });
    }

    async ChangeOtherToRespondent() {
        await expect(this.changeOtherToRespondent).toBeVisible;
        await this.giveNotice.selectOption('Doel Sany');
        await this.continueButton.click();
        await this.firstName.fill('Thierry');
        await this.lastName.fill('John');
        await this.dobDay.fill('11');
        await this.dobMonth.fill('04');
        await this.dobYear.fill('1980');
        await this.gender.selectOption('1: Male');
        await this.legalRepresentation.getByLabel('No').check();
        await this.continueButton.click();
        await this.checkYourAnsAndSubmit();
    }
}
