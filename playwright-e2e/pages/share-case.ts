import {type Locator, type Page} from "@playwright/test";
import {BasePage} from "./base-page";

export class ShareCase extends BasePage {
    shareCaseButton: Locator;
    private selectCaseChckBox: Locator;
    private orgUserEmailText: Locator;
    private addButton: Locator;
    private confirmButton: Locator;
    private caselist: Locator;

    public constructor(page: Page) {
        super(page);
        this.selectCaseChckBox = page.getByRole('row', { name: 'Case name ▼ FamilyMan case' });
        this.shareCaseButton = page.getByRole('button', {name: 'Share Case'});
        this.orgUserEmailText = page.getByLabel('Search by name or email');
        this.addButton = page.getByRole('button', {name: 'Add user'});
        this.confirmButton = page.getByRole('button', {name: 'Confirm'});
        this.caselist = page.getByRole('link', {name: 'Go back to the case list.'});
    }

    async shareCaseWithinOrg(userEmail: string) {
        await this.selectCaseChckBox.getByLabel('').check();
        await this.shareCaseButton.click();
        await this.orgUserEmailText.click();
        await this.orgUserEmailText.pressSequentially(`${userEmail}`,{delay: 800})
        await this.page.getByText(`${userEmail}`).click();
        await this.addButton.click();
        await this.continueButton.click();
        await this.confirmButton.click();
        await this.caselist.click();
    }
}
