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
        this.currentAddress = page.getByRole('group', { name: '*Current address known?' });
        this.reasonUnknownAddress = page.getByLabel('*Reason the address is not');
        this.telephoneNumber =  page.getByRole('group', { name: 'Telephone (Optional)' }).locator('#transformedRespondent_party_telephoneNumber_telephoneNumber');
        this.relationshipToChild = page.getByLabel('What is the respondent\'s relationship to the child or children in this case? (Optional)');
        this.abilityToTakeProceeding = page.getByRole('group', { name: 'Do you believe this person will have problems with litigation capacity' });
        this.anyLegalRepresentation = page.getByRole('group', { name: 'Do they have legal representation? (Optional)' });
    }

    async ChangeOtherToRespondent() {
        await expect(this.changeOtherToRespondent).toBeVisible;
        await this.giveNotice.selectOption('Doel Sany');
        await this.continueButton.click();
        await this.firstName.fill('Thierry');
        await this.lastName.fill('John');
        await this.dobDay.fill('11');
        await this.dobMonth.fill('04');
        await this.dobYear.fill('2000');
        await this.gender.selectOption('1: Male');
        await this.currentAddress.getByLabel('No').check();
        await this.reasonUnknownAddress.selectOption('1: No fixed abode');
        await this.telephoneNumber.fill("00000000000");
        await this.relationshipToChild.fill("Uncle");
        await this.abilityToTakeProceeding.getByLabel('No',{exact: true}).check();
        await this.anyLegalRepresentation.getByLabel('No',{exact: true}).check();
        await this.continueButton.click();
        await this.checkYourAnsAndSubmit();
    }
}
