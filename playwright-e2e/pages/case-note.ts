import { type Page, type Locator, expect } from "@playwright/test";
import { BasePage } from "./base-page";

export class CaseNote extends BasePage {



    get caseNote(): Locator {
        return  this.page.getByRole('textbox', { name: 'Note' })
    }


    constructor(page: Page) {
        super(page);

    }

    async enterCaseNote(note: string) {

        await this.caseNote.fill(note);
    };
};
