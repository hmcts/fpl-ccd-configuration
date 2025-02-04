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
    readonly currentAddress: Locator;
    readonly reasonUnknownAddress: Locator;
    readonly telephoneNumber: Locator;
    readonly relationshipToChild: Locator;
    readonly abilityToTakeProceeding: Locator;
    readonly anyLegalRepresentation: Locator;


    constructor(page: Page) {
        super(page);
        this.changeOtherToRespondent = page.getByRole('heading', { name: 'Change other to respondent', exact: true });
        this.giveNotice = page.getByLabel('Select one of the others to');
        this.firstName = page.getByLabel('First name (Optional)', { exact: true });
        this.lastName = page.getByLabel('Last name (Optional)', { exact: true });
        this.dobDay = page.getByLabel('Day');
        this.dobMonth = page.getByLabel('Month');
        this.dobYear = page.getByLabel('Year');
        this.gender = page.getByLabel('Gender (Optional)');
        this.placeOfBirth = page.getByLabel('Place of birth (Optional)');
        this.currentAddress = page.getByRole('group', { name: '*Current address known? (' });
        this.reasonUnknownAddress = page.getByLabel('*Reason the address is not');
        this.telephoneNumber = page.getByLabel('Telephone number (Optional)');
        this.relationshipToChild = page.getByText('What is this person\'s relationship to the child or children in this case? (Optional)');
        this.abilityToTakeProceeding = page.getByRole('group', { name: 'Do you believe this person' });
        this.anyLegalRepresentation = page.getByRole('group', { name: 'Do they have legal' });

    }

    async ChangeOtherToRespondent() {
        await expect(this.changeOtherToRespondent).toBeVisible;
        await this.giveNotice.selectOption('1: b4ef35b4-3287-4d51-8a5d-b8e1fc6da18f');
        await this.continueButton.click();
        await this.firstName.fill('Tom ');
        await this.lastName.fill('Cruise');
        await this.dobDay.fill("1");
        await this.dobMonth.fill("10");
        await this.dobYear.fill("1990")
        await this.gender.selectOption('1: Male');
        await this.placeOfBirth.fill("London");
        await this.currentAddress.getByLabel('No').check();
        await this.reasonUnknownAddress.selectOption('1: No fixed abode');
        await this.telephoneNumber.fill("00000000000")
        await this.relationshipToChild.fill("Uncle")
        await this.abilityToTakeProceeding.getByLabel('No', { exact: true }).check();
        await this.anyLegalRepresentation.getByLabel('No').check();
    }

    async continueAndCheck() {
        await this.clickContinue();
        await this.page.getByText("Tom Cruise", { exact: true });
        await this.page.getByText("1990", { exact: true });
        await this.page.getByText("London", { exact: true });
        await this.page.getByText("0000000000", { exact: true });
        await this.checkYourAnsAndSubmit();
    }
}
