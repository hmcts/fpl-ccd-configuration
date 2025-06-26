import {expect, type Locator, type Page} from "@playwright/test";
import {BasePage} from "./base-page";

export class NoticeOfChange extends BasePage {


    public constructor(page: Page) {
        super(page);

    }

    async noticeOfChange(caseNumber: string, firstName: string, lastName: string) {

        await this.page.getByRole('link', {name: 'Notice of change'}).click();
        await expect(this.page.getByRole('heading', {name: 'Notice of change'})).toBeVisible();
        await expect.soft(this.page.getByText('This is a 16-digit number from MyHMCTS, for example 1111-2222-3333-4444')).toBeVisible();
        await this.page.getByRole('textbox', {name: 'Online case reference number'}).fill(caseNumber);
        await this.clickContinue();
        await expect(this.page.getByText(' You must enter the client details exactly as they\'re written on the case, including any mistakes. If the client\'s name is Smyth but it has been labelled "Smith", you should enter Smith. Please ensure that you are only performing a notice of change on behalf of the client that you are representing. ')).toBeVisible();
        await this.page.getByRole('textbox', {name: 'Your client\'s first name'}).fill(firstName);
        await this.page.getByRole('textbox', {name: 'Your client\'s last name'}).fill(lastName);
        await this.clickContinue();
        await expect(this.page.getByText('You are representing')).toBeVisible();
        await expect(this.page.getByText('You\'re satisfied that all')).toBeVisible();

        await this.page.getByRole('checkbox', {name: 'I confirm all these details'}).check();
        await this.page.getByRole('checkbox', {name: 'I have served notice of this'}).check();
        await expect(this.page.locator('#notification-section')).toContainText('If the client previously had legal representation, we\'ll let the legal firm or legal representative know that they no longer have access to the case.');
        await expect(this.page.locator('#notification-section')).toContainText('After you submit a notice of change, you might not see the confirmation page immediately');
        await this.clickContinue();

        await expect(this.page.getByRole('heading', {name: 'Notice of change successful'})).toBeVisible();
        await expect(this.page.locator('h1')).toContainText('Notice of change successful You\'re now representing a client on case 1750-8635-3053-6490');


    }

    async accessTheCase() {
        await this.page.getByRole('link', {name: 'Access the case'}).click();

    }
}
