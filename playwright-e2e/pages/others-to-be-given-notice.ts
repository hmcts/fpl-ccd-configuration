import { type Page, type Locator, expect } from "@playwright/test";
import { BasePage } from "./base-page";

export class OthersToBeGivenNotice extends BasePage {
    readonly othersToBeGivenNoticeHeading: Locator;
    readonly dobDay: Locator;
    readonly dobMonth: Locator;
    readonly dobYear: Locator;
    readonly placeOfBirth: Locator;
    readonly currentAddress: Locator;
    readonly telephoneNumber: Locator;
    readonly relationshipToChild: Locator;
    readonly contactDetailsHidden: Locator;
    readonly addNew: Locator;
    readonly hiddenDetails: Locator;
    readonly firstNameRequired: Locator;
    readonly lastNameRequired: Locator;
    readonly whyAddressUnknown: Locator;
    readonly litigation: Locator;
    readonly radioButton:Locator;



    public constructor(page: Page) {
        super(page);
        this.othersToBeGivenNoticeHeading = page.getByRole("heading", { name: "Other people in the case", exact: true });
        this.dobDay = page.getByRole('textbox', { name: 'Day' });
        this.dobMonth = page.getByRole('textbox', { name: 'Month' });
        this.dobYear = page.getByRole('textbox', { name: 'Year' });
        this.placeOfBirth = page.getByLabel('Place of birth (Optional)');
        this.currentAddress = page.getByRole('group', { name: 'Current address known?' })
        this.whyAddressUnknown = page.getByRole('radio', { name: 'No fixed abode' })
        this.telephoneNumber = page.getByLabel('Telephone number (Optional)');
        this.relationshipToChild = page.getByText('What is this person\'s');
        this.contactDetailsHidden = page.getByRole('group', { name: 'Do you need contact details' });
        this.litigation = page.getByRole('group', { name: 'Do you believe this person' });
        this.addNew = page.getByRole('button', { name: 'Add new' });
        this.hiddenDetails = page.locator('#others_additionalOthers_0_detailsHidden_No');
        this.firstNameRequired = page.getByRole('textbox', { name: 'First name' });
        this.lastNameRequired = page.getByRole('textbox', { name: 'Last name' });
        this.radioButton = page.getByRole('radio', { name: 'No' });
    }

    async othersToBeGivenNotice() {
        await expect(this.othersToBeGivenNoticeHeading).toBeVisible;
        await this.addNew.click()
        await this.firstNameRequired.fill('Char');
        await this.lastNameRequired.fill('Bra');
        await this.dobDay.fill('31');
        await this.dobMonth.fill('3');
        await this.dobYear.fill('1980');
        await this.currentAddress.getByLabel('No').click();
        await this.currentAddress.getByLabel('No').click();
        await this.whyAddressUnknown.click();
        await this.relationshipToChild.fill("uncle");
        await this.litigation.getByLabel('No', { exact: true }).check();
        await this.addNew.nth(1).click();
        await this.page.locator('#othersV2_1_firstName').fill('John');
        await this.page.locator('#othersV2_1_lastName').fill('Doe');
        await this.radioButton.nth(5).check();
        await this.page.locator('[id="othersV2_1_addressNotKnowReason-No fixed abode"]').check();
        await this.page.locator('#othersV2_1_childInformation').fill('uncle');
        await this.page.locator('#othersV2_1_childInformation').click();
        await this.page.locator('#othersV2_1_litigationIssues-NO').check();
        await this.clickContinue();
        await this.saveAndContinue.click();
    }
};
