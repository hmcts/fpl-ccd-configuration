import { Locator, Page, expect } from "@playwright/test";
import { BasePage } from "./base-page";

export class ChangeOthersToRespondent extends BasePage {
    readonly changeRespondent: Locator;
    readonly giveNotice: Locator;
    readonly firstName: Locator;
    readonly lastName: Locator;
    readonly dobDay: Locator;
    readonly dobMonth: Locator;
    readonly dobYear: Locator;
    readonly gender: Locator;
    readonly placeOfBirth: Locator;
    readonly currentAddress: Locator;
    readonly reasonUnknownAddress: Locator;
    readonly telephoneNumber: Locator;
    readonly relationshipToChild: Locator;
    readonly litigationIssues: Locator;

    public constructor(page: Page) {
        super(page);
        this.changeRespondent = page.getByRole('heading', { name: 'Change other to respondent' });
        this.giveNotice = page.getByLabel('Select one of the others to');
        this.firstName = page.getByLabel('Representative\'s first name (');
        this.lastName = page.getByLabel('Representative\'s last name (')
        this.dobDay = page.getByLabel('Day');
        this.dobMonth = page.getByLabel('Month');
        this.dobYear = page.getByLabel('Year');
        this.gender = page.getByLabel('Gender (Optional)');
        this.placeOfBirth = page.getByLabel('Place of birth (Optional)');
        this.currentAddress = page.getByRole('group', { name: '*Current address known? (' }).getByLabel('No')
        this.reasonUnknownAddress = page.getByLabel('*Reason the address is not');
        this.telephoneNumber = page.getByLabel('Telephone number (Optional)');
        this.relationshipToChild = page.getByLabel('What is this person\'s');
        this.litigationIssues = page.getByRole('group', { name: 'Do you believe this person' });
    }

    async changeOthersToRespondent() {
        await expect(this.changeRespondent).toBeVisible;
        await this.giveNotice.selectOption('1: cec97500-c91e-40be-91cb-982297ff0a91');
        await this.continueButton.click();
        await this.firstName.fill('Thierry');
        await this.lastName.fill('John');
        await this.dobDay.fill('1');
        await this.dobMonth.fill('10');
        await this.dobYear.fill('1999');
        await this.gender.selectOption('1: Male');
        await this.placeOfBirth.fill('London');
        await this.currentAddress.click();
        await this.reasonUnknownAddress.selectOption('1: No fixed abode');
        await this.telephoneNumber.fill('000000000');
        await this.relationshipToChild.fill('uncle');
        await this.litigationIssues.getByLabel('No', { exact: true }).check();
        await this.continueButton.click();
        await this.checkYourAnsAndSubmit();
    }
}
