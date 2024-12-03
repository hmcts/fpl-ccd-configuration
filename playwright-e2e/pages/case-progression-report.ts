import { Locator, Page, expect } from "@playwright/test";
import { BasePage } from "./base-page";

export class CaseProgressionReport extends BasePage {
    readonly caseProgressionReport: Locator;
    readonly region: Locator;
    readonly selectDfjArea: Locator;
    readonly selectDfjCourt: Locator;
    readonly type: Locator;

    public constructor(page: Page) {
        super(page);
        this.caseProgressionReport = page.getByRole('heading', { name: 'Case progression report', exact: true });
        this.region = page.getByText('Select region?');
        this.selectDfjArea = page.locator('#londonDFJ');
        this.selectDfjCourt = page.locator('#westLondonDFJCourts')
        this.type = page.getByLabel('Select report type');
    }

    async CaseProgressionReport() {
        await expect(this.caseProgressionReport).toBeVisible;
        await this.region.selectOption('London');
        await this.selectDfjArea.selectOption('West London');
        await this.selectDfjCourt.selectOption('West London Family');
        await this.type.selectOption('At risk')
        await this.continueButton.click();
        await this.clickSubmit();
    }
};
