import {expect, type Locator, type Page} from "@playwright/test";
import {BasePage} from "./base-page";

export class InternationalElement extends BasePage {
    public constructor(page: Page) {
        super(page);


    }

    get internationalElementHeading(): Locator {
        return this.page.getByRole('heading', {name: 'International element', exact: true});
    }

    get areThereAnySuitableCarers(): Locator {
        return this.page.getByRole('group', {name: 'Are there any suitable carers'});
    }

    get anySignificantEventsOutsideUk(): Locator {
        return this.page.getByRole('group', {name: 'Are you aware of any significant events that have happened outside the UK? ('});
    }

    get anyIssueWithJurisdictionOfThisCase(): Locator {
        return this.page.getByRole('group', {name: 'Are you aware of any issues'});
    }

    get awareOfAnyProceedingsOutsideTheUk(): Locator {
        return this.page.getByRole('group', {name: 'Are you aware of any proceedings outside the UK? (Optional)'});
    }

    get aGovtOrCentralAuthorityOutsideUkInvolvedInCase(): Locator {
        return this.page.getByRole('group', {name: 'Has, or should, a government'});
    }

    async internationalElementSmokeTest() {
        await expect(this.internationalElementHeading).toBeVisible();
        await this.areThereAnySuitableCarers.getByLabel('No').check();
        await this.anySignificantEventsOutsideUk.getByLabel('No').check();
        await this.anyIssueWithJurisdictionOfThisCase.getByLabel('No').check();
        await this.awareOfAnyProceedingsOutsideTheUk.getByLabel('No').check();
        await this.aGovtOrCentralAuthorityOutsideUkInvolvedInCase.getByLabel('No').check();
        await this.clickContinue();
        await this.checkYourAnsAndSubmit();
    }
}
