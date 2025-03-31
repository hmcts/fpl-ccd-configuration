import { Locator, Page, expect } from "@playwright/test";
import { BasePage } from "./base-page";

export class CaseProgressionReport extends BasePage {
    get caseProgressionReport(): Locator {
        return this.page.getByRole('heading', { name: 'Case progression report', exact: true });
    }

    get region(): Locator {
        return this.page.getByText('Select region?');
    }

    get selectDfjArea(): Locator {
        return this.page.locator('#londonDFJ');
    }

    get selectDfjCourt(): Locator {
        return this.page.locator('#westLondonDFJCourts');
    }

    get type(): Locator {
        return this.page.getByLabel('Select report type');
    }
    // private readonly _caseProgressionReport: Locator;
    // private readonly _region: Locator;
    // private readonly _selectDfjArea: Locator;
    // private readonly _selectDfjCourt: Locator;
    // private readonly _type: Locator;

    // public constructor(page: Page) {
    //     super(page);
    //     this._caseProgressionReport =
    //     this._region =
    //     this._selectDfjArea =
    //     this._selectDfjCourt =
    //     this._type =
    // }

    async CaseProgressionReport() {
        await expect(this.caseProgressionReport).toBeVisible;
        await this.region.selectOption('London');
        await this.selectDfjArea.selectOption('West London');
        await this.selectDfjCourt.selectOption('West London Family');
        await this.type.selectOption('At risk')
        await this.clickContinue();
        await this.clickSubmit();
    }
};
