import {type Page, type Locator, expect} from "@playwright/test";
import { BasePage } from "./base-page";

export class InternationalElement extends BasePage {
    readonly internationalElementHeading: Locator;
    readonly countryInvolved: Locator;
    readonly importantDetails: Locator;
    readonly outSideHague: Locator;

    public constructor(page: Page) {
        super(page);
        this.internationalElementHeading = page.getByRole('heading', {name: 'International element', exact: true,level:1});
        this.countryInvolved = page.getByLabel('Which other countries are involved?');
        this.outSideHague = page.getByRole('group', {name: 'Are any of these countries outside of the Hague Convention?'});
        this.importantDetails = page.getByLabel('Provide all important details');
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
