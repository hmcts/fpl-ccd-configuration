import { type Page, type Locator, expect } from "@playwright/test";
import { BasePage } from "./base-page";

export class OthersToBeGivenNotice extends BasePage {
    readonly otherPeopleToBeGivenNotice: Locator;
    readonly firstName: Locator;
    readonly lastName: Locator
    readonly dobDay: Locator;
    readonly dobMonth: Locator;
    readonly dobYear: Locator;
    readonly currentAddress: Locator;
    readonly addressUnknown: Locator;
    readonly giveMoreDetails: Locator;
    readonly telephoneNumber: Locator;
    readonly numberConfidential: Locator;
    readonly relationshipToChild: Locator;
    readonly contactDetailsHidden: Locator;
    readonly addNew: Locator;
    readonly difficultyCapacity: Locator;
    readonly giveDetails: Locator;
    readonly litigation: Locator;

    public constructor(page: Page) {
        super(page);
        this.otherPeopleToBeGivenNotice = page.getByRole('link', { name: 'Other people to be given' });
        this.firstName = page.getByLabel('First name');
        this.lastName = page.getByLabel('Last name');
        this.dobDay = page.getByLabel('Day');
        this.dobMonth = page.getByLabel('Month');
        this.dobYear = page.getByLabel('Year');
        this.currentAddress = page.getByRole('group', { name: 'Current address known?' });
        this.addressUnknown = page.getByLabel('Whereabouts unknown');
        this.giveMoreDetails = page.getByLabel('Give more details');
        this.telephoneNumber = page.getByLabel('Telephone number (Optional)');
        this.numberConfidential = page.getByRole('group', { name: 'Do you need to keep the' });
        this.relationshipToChild = page.getByText('What is this person\'s');
        this.contactDetailsHidden = page.getByRole('group', { name: 'Do you need contact details' });
        this.difficultyCapacity = page.getByRole('group', { name: 'Do you believe this person' });
        this.litigation = page.getByRole('group', { name: 'Do you believe this person' }).getByLabel('No', { exact: true });
        this.addNew = page.getByRole('button', { name: 'Add new' });
        this.giveDetails = page.getByRole('textbox', { name: 'Give details, including' });
    }

    async othersToBeGivenNotice() {
        await this.otherPeopleToBeGivenNotice.click();
        await this.addNew.click();
        await this.firstName.fill('John');
        await this.lastName.fill('Tom');
        await this.dobDay.fill('11')
        await this.dobMonth.fill('04');
        await this.dobYear.fill('1980');
        await this.currentAddress.getByLabel('No').click();
        await this.currentAddress.getByLabel('No').click();
        await this.addressUnknown.click();
        await this.giveMoreDetails.fill('test');
        await this.telephoneNumber.fill('0123456789');
        await this.numberConfidential.getByLabel('No').check();
        await this.relationshipToChild.fill('uncle')
        await this.difficultyCapacity.getByLabel('No', { exact: true }).check();
        await this.addNew.nth(1).click();
        await this.page.locator('#othersV2_1_firstName').fill('Tim');
        await this.page.locator('#othersV2_1_lastName').fill('kim');
        await this.page.locator('#DOB-day').nth(1).fill('4');
        await this.page.locator('#DOB-month').nth(1).fill('4');
        await this.page.locator('#DOB-year').nth(1).fill('1980');
        await this.page.getByRole('radio', { name: 'No', exact: true }).nth(3).click();
        await this.page.getByRole('radio', { name: 'No', exact: true }).nth(3).click();
        await this.page.locator('[id="othersV2_1_addressNotKnowReason-No\\ fixed\\ abode"]').check();
        await this.page.locator('#othersV2_1_telephone').fill('00000000000');
        await this.page.locator('#othersV2_1_childInformation').fill('Uncle');
        await this.page.locator('#othersV2_1_litigationIssues-NO').dblclick();
        await this.submit.click();
        await this.saveAndContinue.click();
    }
};
