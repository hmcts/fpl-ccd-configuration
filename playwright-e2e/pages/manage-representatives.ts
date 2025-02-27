import { Locator, Page, expect } from "@playwright/test";
import { BasePage } from "./base-page";

export class ManageRepresentatives extends BasePage {
    readonly manageRepresentatives: Locator;
    readonly fullName: Locator;
    readonly positionInACase: Locator;
    readonly emailAddress: Locator;
    readonly byEmail: Locator;
    readonly whoAreThey: Locator;


    public constructor(page: Page) {
        super(page);
        this.manageRepresentatives = page.getByRole('heading', { name: 'Manage representatives', exact: true });
        this.fullName = page.getByLabel('Full name (Optional)');
        this.positionInACase = page.getByLabel('Position in a case (Optional)');
        this.emailAddress = page.getByLabel('Email address (Optional)');
        this.byEmail = page.getByLabel('By email');
        this.whoAreThey = page.getByLabel('Who are they? (Optional)');

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
