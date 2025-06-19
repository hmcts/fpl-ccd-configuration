import { type Page, type Locator, expect } from "@playwright/test";
import { BasePage } from "./base-page";

export class ChangeCaseName extends BasePage {
    readonly changeCaseName: Locator;
    readonly caseName: Locator;

    constructor(page: Page) {
        super(page);
        this.changeCaseName = page.getByRole('heading', { name: 'Update case name', exact: true });
        this.caseName = page.getByLabel('Case name');
    }

    async updateCaseName() {
        await expect(this.changeCaseName).toBeVisible;
        await expect(this.page.getByText('Case name will be updated to:')). toBeVisible();
        await expect(this.page.locator('#updatedCaseNameLabel').getByText('Swansea City Council & Bloggs')).toBeVisible();
        await expect(this.page.getByText('based on the current applicant and respondent\'s details.')).toBeVisible();
        await this.clickSubmit();
    };
};
