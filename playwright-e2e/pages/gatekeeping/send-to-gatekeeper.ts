import {BasePage} from '../base-page';
import {Page, Locator} from '@playwright/test';

export class SendToGatekeeperPage extends BasePage {
    readonly emailAddressTextbox: Locator;

    public constructor(page: Page) {
        super(page)
        this.emailAddressTextbox = page.getByRole('textbox', { name: 'Email address' });
    }

    public async fillAddressTextbox(address: string): Promise<void> {
        await this.emailAddressTextbox.fill(address);
    }
}
