import {type Page, type Locator, expect} from "@playwright/test";
import {BasePage} from "./base-page";

export class RecordFinalDecision extends BasePage {


    constructor(page: Page) {
        super(page);

    }


    get isFinalDecionForAllChild(): Locator {
        return this.page.getByRole('group', {name: 'Do all children in the case have final decisions?'})
    }

    get decicionDate(): Locator {
        return this.page.getByRole('group', {name: 'Date final decision was made'})
    }

    get childOneFinalDecisionReason() {
        return this.page.locator('#childFinalDecisionDetails00_finalDecisionReason-FINAL_ORDER')
    }

    get childTwoFinalDecisionReason() {
        return this.page.locator('#childFinalDecisionDetails01_finalDecisionReason-NO_ORDER')
    }

    get childThreeFinalDecisionReason() {
        return this.page.locator('#childFinalDecisionDetails02_finalDecisionReason-HOUSEKEEPING')
    }

    get childFourFinalDecisionReason() {
        return this.page.locator('#childFinalDecisionDetails03_finalDecisionReason-WITHDRAWN')
    }


    async selectFinalDecisionForAllChildren(YesNo = 'Yes') {

        await this.page.getByRole('radio', {name: 'Yes'}).check();
    }

    async dateValidationPass() {
        await expect(this.page.getByText('The data entered is not valid for Date final decision was made')).toBeHidden();
    }

    async enterDecisionDate(decisionDate: Date) {
        await this.decicionDate.getByRole('textbox', {name: 'Day'}).fill(new Intl.DateTimeFormat('en', {day: 'numeric'}).format(decisionDate));
        await this.decicionDate.getByRole('textbox', {name: 'Month'}).fill(new Intl.DateTimeFormat('en', {month: 'numeric'}).format(decisionDate));
        await this.decicionDate.getByRole('textbox', {name: 'Year'}).fill(new Intl.DateTimeFormat('en', {year: 'numeric'}).format(decisionDate));
        await this.decicionDate.click();
    }

    async enterFinalOutCome() {
        await this.childOneFinalDecisionReason.click();
        await this.childTwoFinalDecisionReason.click();
        await this.childFourFinalDecisionReason.click();
        await this.childThreeFinalDecisionReason.click();
        await this.childFourFinalDecisionReason.click();

    }

}


