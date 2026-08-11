import {BasePage} from "../base-page";
import {Page, Locator} from "@playwright/test";

export class FamilymanCaseNumberPage extends BasePage {
    readonly caseNumberTextbox: Locator;

    public constructor(page: Page) {
        super(page);
        this.caseNumberTextbox = page.getByRole('textbox', { name: 'FamilyMan case number' });
    }

    public async fillfamilyManCaseNumber(familymanCaseNumber: string): Promise<void> {
        await this.caseNumberTextbox.fill(familymanCaseNumber);
    }
}
