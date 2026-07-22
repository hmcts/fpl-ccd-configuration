import { type Page, type Locator, expect } from "@playwright/test";
import { BasePage } from "./base-page";

export class OthersToBeGivenNotice extends BasePage {
    readonly inputFirstName: Locator;
    readonly inputLastName: Locator
    readonly inputDobDay: Locator;
    readonly inputDobMonth: Locator;
    readonly inputDobYear: Locator;
    readonly currentAddress: Locator;
    readonly currentAddressNo: Locator;
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
    readonly confidentialAddress: Locator;

    public constructor(page: Page) {
        super(page);
        this.inputFirstName = page.getByLabel('First name');
        this.inputLastName = page.getByLabel('Last name');
        this.inputDobDay = page.getByLabel('Day');
        this.inputDobMonth = page.getByLabel('Month');
        this.inputDobYear = page.getByLabel('Year');
        this.currentAddress = page.getByRole('group', { name: 'Current address known?' });
        this.currentAddressNo = this.currentAddress.getByLabel('No');
        this.addressUnknown = page.getByLabel('Whereabouts unknown');
        this.giveMoreDetails = page.getByLabel('Give more details');
        this.telephoneNumber = page.getByLabel('Telephone number (Optional)');
        this.numberConfidential = page.getByRole('group', { name: 'Do you need to keep the' });
        this.relationshipToChild = page.getByText('What is this person\'s');
        this.contactDetailsHidden = page.getByRole('group', { name: 'Do you need to keep the contact number confidential? (Optional)' });
        this.difficultyCapacity = page.getByRole('group', { name: 'Do you believe this person' });
        this.litigation = page.getByRole('group', { name: 'Do you believe this person' }).getByLabel('No', { exact: true });
        this.addNew = page.getByRole('button', { name: 'Add new' });
        this.giveDetails = page.getByRole('textbox', { name: 'Give details, including' });
        this.confidentialAddress = page.getByRole('group', { name: 'Do you need to keep the' });
    }

    async othersToBeGivenNotice() {
        await this.addNew.click();
        await this.inputFirstName.fill('James');
        await this.inputLastName.fill('Trace');
        await this.inputDobDay.fill('12')
        await this.inputDobMonth.fill('05');
        await this.inputDobYear.fill('1988');
        await this.currentAddress.getByLabel('Yes').click();
        await this.currentAddress.getByLabel('Yes').click();//playwright playing up. had to work around it
        await this.enterPostCode('SW1A 0AA');
        await this.confidentialAddress.getByLabel('Yes').check();
        await this.confidentialAddress.getByLabel('Yes').check();//playwright playing up. had to work around it
        await this.telephoneNumber.fill('03456789000');
        await this.numberConfidential.getByLabel('Yes').check();
        await this.contactDetailsHidden.getByLabel('No').check();
        await this.relationshipToChild.fill('uncle')
        await this.difficultyCapacity.getByLabel('No', { exact: true }).check();
        await this.addNew.nth(1).click();
        await this.page.locator('#othersV2_1_firstName').fill('Tim');
        await this.page.locator('#othersV2_1_lastName').fill('kim');
        await this.page.locator('#DOB-day').nth(1).fill('4');
        await this.page.locator('#DOB-month').nth(1).fill('4');
        await this.page.locator('#DOB-year').nth(1).fill('1980');
        await this.page.locator('#othersV2_1_addressKnowV2-No').click();
        await this.page.locator('#othersV2_1_addressKnowV2-No').click();
        await this.page.locator('[id="othersV2_1_addressNotKnowReason-No\\ fixed\\ abode"]').click();
        await this.page.locator('#othersV2_1_telephone').fill('00000000000');
        await this.page.locator('#othersV2_1_childInformation').fill('Uncle');
        await this.page.locator('#othersV2_1_litigationIssues-NO').dblclick();
        await this.clickContinue();
        await this.clickSaveAndContinue();
        await this.checkYourAnsAndSubmit();
    }
};
