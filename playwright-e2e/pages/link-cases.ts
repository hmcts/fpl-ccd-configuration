import {type Locator, type Page} from "@playwright/test";
import {BasePage} from "./base-page";

export class CaseLink extends BasePage {
    set linkedCasePage(value: Page) {
        this._linkedCasePage = value;
    }
    get linkedCasePage(): Page {
        return this._linkedCasePage;
    }
    get caseNumber(): Locator {
        return this.page.locator('#width-20');
    }

    get proposeLink(): Locator {
        return this.page.getByRole('button', {name: 'Propose case link'});
    }

    get next(): Locator {
        return this.page.getByRole('button', {name: 'Next'});
    }

    get submit(): Locator {
        return this.page.getByRole('button', {name: 'Submit'});;
    }


    private _linkedCasePage: Page ;

    constructor(page: Page) {
        super(page);
        this._linkedCasePage = page;

    }

    async clickNext() {
        await this.next.click();
    }

    async proposeCaseLink(caseNumber: string, linkreason: string[]) {
        await this.caseNumber.fill(caseNumber);
        for (var linktype of linkreason) {
            await this.page.getByLabel(linktype).check();
        }
        await this.proposeLink.click();
    }
    hypenateCaseNumber(caseNumber: string) {
        let hypenatedCaseNumber: string;
        hypenatedCaseNumber = caseNumber.slice(0, 4) + "-" + caseNumber.slice(4, 8) + "-" + caseNumber.slice(8, 12) + "-" + caseNumber.slice(12, 16);
        return hypenatedCaseNumber
    }
    async submitCaseLink() {
        this.submit.click();
    }
    async openLinkedCase(caseNumber: string) {
        const newPagePromise = this.page.context().waitForEvent('page');
        await this.page.getByRole('link', {name: `${caseNumber}`}).click();
        this.linkedCasePage = await newPagePromise
        await this.linkedCasePage.waitForLoadState();
        await this.linkedCasePage.getByRole('tab', {name: 'Linked Cases', exact: true}).click();
        await this.linkedCasePage.getByRole('link', {name: 'Show'}).click();

    }
    async selectCaseToUnlink(caseNumber: string) {
        await this.page.locator(`#case-reference-${caseNumber}`).check();
    }
    async gotoCaseLinkNextStep(eventName: string) {
        await expect(async () => {
            await this.nextStep.selectOption(eventName);
            await this.goButton.click({clickCount:2,delay:300});
            await this.page.waitForTimeout(300);
            await expect(this.page.getByRole('button', { name: 'Submit' })).toBeAttached();
            await expect.soft(this.page.getByRole('heading', {name: 'Before you start'})).toBeVisible();
            await this.page.reload();
        }).toPass();
    }
}
