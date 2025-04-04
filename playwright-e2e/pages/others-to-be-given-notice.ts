import { type Page, type Locator, expect } from "@playwright/test";
import { BasePage } from "./base-page";

export class OthersToBeGivenNotice extends BasePage {
    get othersToBeGivenNoticeHeading(): Locator {
        return this.page.getByRole("heading", {name: "Other people in the case", exact: true});
    }

    get fullName(): Locator {
        return this.page.getByLabel('Full name (Optional)');
    }

    get dobDay(): Locator {
        return this.page.getByLabel('Day');
    }

    get dobMonth(): Locator {
        return this.page.getByLabel('Month');
    }

    get dobYear(): Locator {
        return this.page.getByLabel('Year');
    }

    get gender(): Locator {
        return this.page.getByLabel('Gender (Optional)');
    }

    get placeOfBirth(): Locator {
        return this.page.getByLabel('Place of birth (Optional)');
    }

    get currentAddress(): Locator {
        return this.page.getByRole('group', {name: '*Current address known? ('});
    }

    get reasonUnknownAddress(): Locator {
        return this.page.getByLabel('*Reason the address is not');
    }

    get telephoneNumber(): Locator {
        return this.page.getByLabel('Telephone number (Optional)');
    }

    get relationshipToChild(): Locator {
        return this.page.getByText('What is this person\'s relationship to the child or children in this case? (Optional)');
    }

    get contactDetailsHidden(): Locator {
        return this.page.getByRole('group', {name: 'Do you need contact details'});
    }

    get addNew(): Locator {
        return this.page.getByRole('button', {name: 'Add new'});
    }

    get hiddenDetails(): Locator {
        return this.page.locator('#others_additionalOthers_0_detailsHidden_No');
    }





        async othersToBeGivenNotice()
        {
            await expect(this.othersToBeGivenNoticeHeading).toBeVisible;
            await this.fullName.fill("John Doe");
            await this.dobDay.fill("1");
            await this.dobMonth.fill("10");
            await this.dobYear.fill("1990")
            await this.gender.selectOption('1: Male');
            await this.placeOfBirth.fill("London");
            await this.currentAddress.getByLabel('No').check();
            await this.reasonUnknownAddress.selectOption('1: No fixed abode');
            await this.telephoneNumber.fill("0123456789")
            await this.relationshipToChild.fill("uncle")
            await this.contactDetailsHidden.getByLabel('No').check();
            await this.page.getByLabel('Don\'t know').check();
            await this.addNew.click();
            await this.page.locator('#others_additionalOthers_0_name').fill('John Grey');
            await this.page.locator('#others_additionalOthers #DOB-day').fill("12");
            await this.page.locator('#others_additionalOthers #DOB-month').fill("11");
            await this.page.locator('#others_additionalOthers #DOB-year').fill("1999");
            await this.page.locator('#others_additionalOthers_0_gender').selectOption('2: Female');
            await this.page.locator('#others_additionalOthers_0_birthPlace').fill("London");
            await this.page.locator('#others_additionalOthers_0_addressKnowV2-No').check();
            await this.page.locator('#others_additionalOthers_0_addressNotKnowReason').selectOption('1: No fixed abode');
            await this.page.locator('#others_additionalOthers_0_telephone').fill("0000000000");
            await this.page.locator('#others_additionalOthers_0_childInformation').fill('test');
            await this.page.locator('#others_additionalOthers_0_litigationIssues-NO').check();
            await this.clickSubmit();
            await this.clickSaveAndContinue();

        }
};
