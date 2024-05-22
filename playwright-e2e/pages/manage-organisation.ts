import {expect, type Locator, type Page} from "@playwright/test";
import {BasePage} from "./base-page";

export class Organisation extends BasePage {
    readonly unassignedCaseLink: Locator;
    readonly caseNumberSearchText: Locator;
    readonly showcaseFilterButton: Locator;
    readonly applyFilterButton: Locator;

    public constructor(page: Page) {
        super(page);
        this.unassignedCaseLink = this.page.getByRole('link', {name: 'Unassigned cases'});
        this.caseNumberSearchText = this.page.getByLabel('Enter the 16-digit case');
        this.showcaseFilterButton = this.page.getByRole('button', {name: 'Show unassigned cases filter'});
        this.applyFilterButton = this.page.getByRole('button', {name: 'Apply filter'});
    }

    async searchUnassignedCase(caseNumber: string, caseName: string) {
        await this.unassignedCaseLink.click();
        await expect(this.page.getByRole('heading', {name: 'Unassigned Cases'})).toBeVisible();
        await expect(this.page.getByLabel('CARE_SUPERVISION_EPO').getByText('CARE_SUPERVISION_EPO', { exact: true })).toBeVisible();
        await this.showcaseFilterButton.click();
        await this.caseNumberSearchText.fill(caseNumber);
        await this.applyFilterButton.click();
        await expect(this.page.getByText('Showing 1 to 1 of 1')).toBeVisible();
        await expect(this.page.getByRole('cell', {name: `${caseName}`})).toBeVisible();
    }
}
