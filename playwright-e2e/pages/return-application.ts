import {expect, type Locator} from "@playwright/test";
import {BasePage} from "./base-page";

export class ReturnApplication extends BasePage {

    get reasonForRejection(): Locator {
        return this.page.getByLabel('Application Incomplete');
    }

    get needToChange(): Locator {
        return this.page.getByLabel('Let the local authority know');
    }

    get IAgreeWithThisStatement(): Locator {
        return this.page.getByLabel('I agree with this statement');
    }

    get DoTheyHaveLegal(): Locator {
        return this.page.getByRole('group', {name: '*Do they have legal'}).getByLabel('No');
    }

    get DoYouNeedContactDetailsHidden(): Locator {
        return this.page.getByRole('group', {name: 'Do you need contact details'}).getByLabel('No');
    }

    get submit(): Locator {
        return this.page.getByRole('button', {name: 'Submit'});
    }

    get respondentDetailsHeading(): Locator {
        return this.page.getByRole("heading", {name: 'Respondents\' details'});
    }

    async ReturnApplication() {
        await this.reasonForRejection.check();
        await this.needToChange.fill('incomplete application');
        await this.clickSubmit()
        await this.clickContinue();
    }

    async UpdateRespondent() {
        await expect(this.respondentDetailsHeading).toBeVisible();
        await this.DoTheyHaveLegal.check();
        await this.DoYouNeedContactDetailsHidden.check();
        await this.clickContinue();
        await this.checkYourAnsAndSubmit();

    }
}
