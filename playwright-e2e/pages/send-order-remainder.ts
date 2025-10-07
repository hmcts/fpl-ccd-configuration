import {type Page, type Locator, expect} from "@playwright/test";
import {BasePage} from "./base-page";

export class SendOrderRemainder extends BasePage {


    constructor(page: Page) {
        super(page);
    }

    get remainderRadioButton(): Locator {
        return this.page.getByRole('group', {name: 'Would you like to remind the'});
    }

    get historyTab(): Locator {
        return this.page.getByRole('tab', { name: 'History',exact:true });
    }

    async sendOrderRemainder(yesNo = 'Yes') {
        await this.remainderRadioButton.getByRole('radio', {name: 'Yes'}).check();
    }

    //This need to be removed after EXUI fix the accessiblity issue
    async gotoHistoryTab(){
        await this.historyTab.click();
    }

}


