import {expect, type Locator, type Page} from "@playwright/test";
import {BasePage} from "./base-page";

export class OtherPeopleInCase extends BasePage {
    readonly otherPeopleHeading: Locator;
    readonly dobDay: Locator;
    readonly dobMonth: Locator;
    readonly dobYear: Locator;
    readonly currentAddress: Locator;
    readonly telephoneNumber: Locator;
    readonly numberConfidential: Locator;
    readonly relationshipToChild: Locator;
    readonly contactDetailsHidden: Locator;
    readonly giveDetails: Locator;
    readonly litigation: Locator;
    readonly gender: Locator;
    readonly placeofBirth: Locator;
    private fullName: Locator;

    public constructor(page: Page) {
        super(page);
        this.fullName = page.getByRole('textbox', {name: 'Full name (Optional)'});
        this.otherPeopleHeading = page.getByRole("heading", {name: 'Other people in the case', exact: true});
        this.dobDay = page.getByLabel('Day');
        this.dobMonth = page.getByLabel('Month');
        this.dobYear = page.getByLabel('Year');
        this.gender = page.getByLabel('Gender (Optional)');
        this.placeofBirth = page.getByRole('textbox', {name: 'Place of birth (Optional)'});
        this.currentAddress = page.getByRole('group', {name: 'Current address known?'});
        this.telephoneNumber = page.getByLabel('Telephone number (Optional)');
        this.numberConfidential = page.getByRole('group', {name: 'Do you need to keep the'});
        this.relationshipToChild = page.getByText('What is this person\'s');
        this.contactDetailsHidden = page.getByRole('group', {name: 'Do you need contact details'});
        this.litigation = page.getByRole('group', {name: 'Do you believe this person'}).getByLabel('Yes', {exact: true});
        this.giveDetails = page.getByRole('textbox', {name: 'Give details, including'});
    }


    async addOtherPerson() {
        await expect.soft(this.otherPeopleHeading).toBeVisible();
        await this.fullName.first().fill('Sam Daniel');
        await this.dobDay.fill('11');
        await this.dobMonth.fill('10');
        await this.dobYear.fill('1990');
        await this.gender.selectOption('Female');
        await this.placeofBirth.fill('UK');
        await this.currentAddress.getByLabel('Yes').click();
        await this.enterPostCode('WD3');
        await this.telephoneNumber.fill('232543654645');
        await this.contactDetailsHidden.getByLabel('No').check();
        await this.relationshipToChild.fill('Aunty');
        await this.litigation.check();
        await this.giveDetails.fill('No able to travel');
        await this.clickContinue();
        await this.checkYourAnsAndSubmit();

    }

}
