import {expect, type Locator} from "@playwright/test";
import {BasePage} from "./base-page";

export class InternationalElement extends BasePage {

    get internationalElementHeading(): Locator {
        return this.page.getByRole('heading', {name: 'International element', exact: true, level: 1});
    }

    get countryInvolved(): Locator {
        return this.page.getByLabel('Which other countries are involved?');
    }

    get importantDetails(): Locator {
        return this.page.getByLabel('Provide all important details');
    }

    get outSideHague(): Locator {
        return this.page.getByRole('group', {name: 'Are any of these countries outside of the Hague Convention?'});
    }


    async internationalElementSmokeTest() {
        await expect(this.internationalElementHeading).toBeVisible();
        await expect(this.page.getByText('Including any carers, events, proceedings or authorities outside the UK, or issues with jurisdiction.', {exact: true})).toBeVisible();
        await this.countryInvolved.fill('Spain\nItlay\nFrance');
        await this.outSideHague.getByRole('radio', {name: 'Yes'}).click();
        await this.importantDetails.fill('Convention\nCare order by the father');
        await this.clickContinue();
        await this.checkYourAnsAndSubmit();
    }
}
