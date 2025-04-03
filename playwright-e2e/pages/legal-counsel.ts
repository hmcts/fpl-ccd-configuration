import {type Page, type Locator} from "@playwright/test";
import {BasePage} from "./base-page";

export class LegalCounsel extends BasePage {
    get addCounsel(): Locator {
        return this.page.getByRole('button', {name: 'Add new'});
    }

    get firstName(): Locator {
        return this.page.getByLabel('First name');
    }

    get lastName(): Locator {
        return this. page.getByLabel('Last name');
    }

    get email(): Locator {
        return this. page.getByLabel('Email address');
    }

    get organisation(): Locator {
        return this.page.getByLabel('You can only search for');
    }

    get selectOrg(): Locator {
        return this.page.getByRole('link', {name: 'Select'});
    }

    get removeCounsel(): Locator {
        return this. page.getByLabel('Remove Counsel');
    }

    get removeCounselAlert(): Locator {
        return this.page.getByRole('button', {name: 'Remove'});
    }

    get isRepresented(): Locator {
        return this. page.getByRole('group', {name: 'Do they have legal'});
    }

    async toAddLegalCounsel() {
        await this.addCounsel.click();
    }

    async toRemoveLegalCounsel() {
        await this.removeCounsel.click();
        await this.removeCounselAlert.click();
    }

    async enterLegalCounselDetails() {
        await this.firstName.fill('FPLOrg');
        await this.lastName.fill('Solicitor');
        await this.email.fill('fpl_sol_org_01@mailinator.com');
        await this.organisation.fill('FPLSolicitorOrg');
        await this.selectOrg.click();
    }

    async removeRepresentative() {
        await this.isRepresented.getByLabel('No').check();
    }

}
