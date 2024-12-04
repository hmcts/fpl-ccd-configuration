import { Locator, Page, expect } from "@playwright/test";
import { BasePage } from "./base-page";

export class ManageRepresentatives extends BasePage {
    readonly manageRepresentatives: Locator;
    readonly fullName: Locator;
    readonly positionInACase: Locator;
    readonly emailAddress: Locator;
    readonly phoneNumber: Locator;
    readonly Postcode: Locator;
    readonly buildingAndStreet: Locator;
    readonly findAddress: Locator;
    readonly townOrCity: Locator;
    readonly byEmail: Locator;
    readonly whoAreThey: Locator;
    readonly selectAnAddress: Locator;

    public constructor(page: Page) {
        super(page);
        this.manageRepresentatives = page.getByRole('heading', { name: 'Manage representatives', exact: true });
        this.fullName = page.getByLabel('Full name (Optional)');
        this.positionInACase = page.getByLabel('Position in a case (Optional)');
        this.emailAddress = page.getByLabel('Email address (Optional)');
        this.phoneNumber = page.getByLabel('Phone number (Optional)');
        this.buildingAndStreet = page.getByLabel('Select an address');
        this.townOrCity = page.getByLabel('Town or City');
        this.Postcode = page.getByLabel('Enter a UK postcode');
        this.findAddress = page.getByRole('button', { name: 'Find address' });
        this.selectAnAddress = page.getByText('Select an address');
        this.byEmail = page.getByLabel('By email');
        this.whoAreThey = page.getByLabel('Who are they? (Optional)');

    }
    async updateRepresentatives() {
        await expect(this.manageRepresentatives).toBeVisible;
        await this.fullName.fill('Charlie Chaplin');
        await this.positionInACase.fill('FPL');
        await this.emailAddress.fill('solicitors1@solicitors.uk');
        await this.phoneNumber.fill('07818213677');
        await this.Postcode.fill('sk3 8pp');
        await this.findAddress.click();
        await this.selectAnAddress.selectOption('1: Object');
        await this.townOrCity.fill('Stockport')
        await this.enterPostcode.fill('sk3 8pp');
        await this.byEmail.click();
        await this.whoAreThey.selectOption('1: LA_LEGAL_REPRESENTATIVE');
        await this.clickSubmit();
    }
};
