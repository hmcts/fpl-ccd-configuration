import {BasePage} from "../base-page";
import {Page, Locator} from "@playwright/test";

export class AddFamilymanCaseNumberPage extends BasePage {
    readonly caseNumberTextbox: Locator;

    public constructor(page: Page) {
        super(page);
        this.caseNumberTextbox = page.getByRole('textbox', { name: 'FamilyMan case number' });
    }

    public async fillCaseNumberTextbox(familymanCaseNumber: string): Promise<void> {
        await this.caseNumberTextbox.fill(familymanCaseNumber);
    }
}
