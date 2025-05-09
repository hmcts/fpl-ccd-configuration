import {expect, type Locator} from "@playwright/test";
import {BasePage} from "./base-page.ts";

export class RespondentDetails extends BasePage {

    get respondentDetailsHeading(): Locator {
        return this.page.getByRole("heading", {name: 'Respondents\' details'});
    }

    get firstName(): Locator {
        return this.page.getByLabel('*First name (Optional)');

    }

    get lastName(): Locator {
        return this.page.getByLabel('*Last name (Optional)');
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

    get currentAddress(): Locator {
        return this.page.getByRole('group', {name: '*Current address known?'});
    }

    get telephone(): Locator {
        return this.page.getByRole('group', {name: 'Telephone (Optional)'}).locator('#respondents1_0_party_telephoneNumber_telephoneNumber');

    }

    get relationToChild(): Locator {
        return this.page.getByLabel('*What is the respondent\'s relationship to the child or children in this case? (Optional)');
    }

    get relationToChildContact(): Locator {
        return this.page.getByRole('group', {name: 'Do you need contact details hidden from other parties? (Optional)'});
    }

    get relationToChildContactReason(): Locator {
        return this.page.getByLabel('Give reason (Optional)');
    }

    get litigationCapacity(): Locator {
        return this.page.getByRole('group', {name: 'Do you believe this person will have problems with litigation capacity (understanding what\'s happening in the case)? (Optional)'});
    }

    get litigationCapacityReason(): Locator {
        return this.page.getByLabel('Give details, including assessment outcomes and referrals to health services (Optional)');
    }

    get legalRepresentation(): Locator {
        return this.page.getByRole('group', {name: '*Do they have legal representation? (Optional)'});
    }

    get addressNotKnownReason(): Locator {
        return this.page.getByLabel('*Reason the address is not known');
    }


    async respondentDetailsNeeded() {
        await expect(this.respondentDetailsHeading).toBeVisible();
        await this.firstName.click();
        await this.firstName.fill('John');
        await this.lastName.click();
        await this.lastName.fill('Smith');
        await this.dobDay.click();
        await this.dobDay.fill('10');
        await this.dobMonth.click();
        await this.dobMonth.fill('11');
        await this.dobYear.click();
        await this.dobYear.fill('2001');
        await this.gender.click(); //not sure if click needed
        await this.gender.selectOption('1: Male');
        await this.currentAddress.getByLabel('No').check();
        await this.addressNotKnownReason.selectOption('2: Person deceased');
        await this.telephone.fill('01234567890');
        await this.relationToChild.click();
        await this.relationToChild.fill('aunt');
        await this.relationToChildContact.getByLabel('Yes').check();
        await this.relationToChildContactReason.click();
        await this.relationToChildContactReason.fill('this is the reason');
        await this.litigationCapacity.getByLabel('Yes').check();
        await this.litigationCapacityReason.click();
        await this.litigationCapacityReason.fill('these are the details');
        await this.legalRepresentation.getByLabel('No').check();
        await this.clickContinue();
        await this.checkYourAnsAndSubmit();
    }
}
