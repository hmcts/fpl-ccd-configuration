import {expect, type Locator} from "@playwright/test";
import {BasePage} from "./base-page";

export class ChangeOtherToRespondent extends BasePage {
    get changeOtherToRespondent(): Locator {
        return this.page.getByRole('heading', {name: 'Change other to respondent', exact: true});
    }

    get giveNotice(): Locator {
        return this.page.getByLabel('Select one of the others to');
    }

    get firstName(): Locator {
        return this.page.getByLabel('First name (Optional)', {exact: true});
    }

    get lastName(): Locator {
        return this.page.getByLabel('Last name (Optional)', {exact: true});
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
        return this.page.getByRole('group', {name: '*Current address known?'});
    }

    get reasonUnknownAddress(): Locator {
        return this.page.getByLabel('*Reason the address is not');
    }

    get telephoneNumber(): Locator {
        return this.page.getByRole('group', {name: 'Telephone (Optional)'}).locator('#transformedRespondent_party_telephoneNumber_telephoneNumber');
    }

    get relationshipToChild(): Locator {
        return this.page.getByLabel('What is the respondent\'s relationship to the child or children in this case? (Optional)');
    }

    get abilityToTakeProceeding(): Locator {
        return this.page.getByRole('group', {name: 'Do you believe this person will have problems with litigation capacity'});
    }

    get anyLegalRepresentation(): Locator {
        return this.page.getByRole('group', {name: 'Do they have legal representation? (Optional)'});
    }

    async ChangeOtherToRespondent() {
        await expect(this.changeOtherToRespondent).toBeVisible;
        await this.giveNotice.selectOption('Doel Sany');
        await this.clickContinue();
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
        await this.abilityToTakeProceeding.getByLabel('No', {exact: true}).check();
        await this.anyLegalRepresentation.getByLabel('No', {exact: true}).check();
        await this.clickContinue();
        await this.checkYourAnsAndSubmit();
    }
}
