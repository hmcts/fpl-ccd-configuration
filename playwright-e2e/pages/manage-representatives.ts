import { Locator, Page, expect } from "@playwright/test";
import { BasePage } from "./base-page";

export class ManageRepresentatives extends BasePage {
    get manageRepresentatives(): Locator {
        return this.page.getByRole('heading', { name: 'Manage representatives', exact: true });;
    }

    get fullName(): Locator {
        return this.page.getByLabel('Full name (Optional)');
    }

    get positionInACase(): Locator {
        return this.page.getByLabel('Position in a case (Optional)');
    }

    get emailAddress(): Locator {
        return this.page.getByLabel('Email address (Optional)');
    }

    get byEmail(): Locator {
        return this.page.getByLabel('By email');
    }

    get whoAreThey(): Locator {
        return this.page.getByLabel('Who are they? (Optional)');
    }



    async updateRepresentatives() {
        await expect(this.manageRepresentatives).toBeVisible;
        await this.fullName.fill('Charlie Chaplin');
        await this.positionInACase.fill('FPL');
        await this.emailAddress.fill('solicitors1@solicitors.uk');
        await this.enterPostCode('sk3 8pp');
        await this.byEmail.click();
        await this.whoAreThey.selectOption('5: REPRESENTING_RESPONDENT_1');
        await this.clickSubmit();
    }
};
