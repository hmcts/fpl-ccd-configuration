import {expect, type Locator, type Page} from "@playwright/test";
import {CreateCaseName} from "../utils/create-case-name";
import {BasePage} from "./base-page";

export class CreateCase extends BasePage {

    constructor(page: Page) {
        super(page);

    }

    private _casenumber: string | undefined;

    get casenumber(): string {
        return <string>this._casenumber;
    }

    private _generatedCaseName: string | undefined;

    get generatedCaseName(): string {
        return <string>this._generatedCaseName;
    }

    set generatedCaseName(value: string) {
        this._generatedCaseName = value;
    }

    get caseJurisdictionFilterDropdown(): Locator {
        return this.page.getByLabel("Jurisdiction");
    }

    get caseTypeFilterDropdown(): Locator {
        return this.page.getByLabel("Case type");
    }

    get createCaseLink(): Locator {
        return this.page.getByRole("link", {name: "Create case"});
    }

    get localAuthorityOption(): Locator {
        return this.page.getByLabel('Select the local authority you\'re representing');
    }

    get caseListLink(): Locator {
        return this.page.getByRole('link', {name: ' Case list '});
    }

    get caseNumberTextBox(): Locator {
        return this.page.getByLabel('CCD Case Number');
    }

    get applyFilter(): Locator {
        return this.page.getByLabel('Apply filter');
    }

    get caseNameTextBox(): Locator {
        return this.page.getByLabel('Case name');
    }

    get representingPartyRadio(): Locator {
        return this.page.getByLabel('Local Authority', {exact: true});
    }

    get applicationFor(): Locator {
        return this.page.getByLabel('Select the local authority which relates to the case');
    }

    get respondentSolicitorUser() {
        return this.page.getByLabel('Respondent Solicitor');
    }


    async createCase() {
        // This click timeout is here allow for ExUI loading spinner to finish
        await this.createCaseLink.click();

        await this.caseJurisdictionFilterDropdown.selectOption("PUBLICLAW").catch((error) => {
            this.page.waitForTimeout(500);
            console.log(error);
            console.log(" the page reloaded to ");
            this.page.reload({timeout: 3000, waitUntil: 'load'});
        })
        await this.caseJurisdictionFilterDropdown.selectOption("PUBLICLAW");
        await this.caseTypeFilterDropdown.selectOption("CARE_SUPERVISION_EPO");
        await this.page.getByLabel("Event").selectOption("openCase");
        await this.page.getByRole("button", {name: "Start"}).click();
    }

    caseName(testType: string = 'Smoke Test'): void {
        const formattedDate = CreateCaseName.getFormattedDate();
        this.generatedCaseName = `${testType} ${formattedDate}`;
    }

    async submitCase(caseName: string) {
        await this.page.getByLabel("Case name").click();
        await this.page.getByLabel("Case name").fill(caseName);
        await this.page
            .getByRole("button", {name: "Submit"})
            // This click timeout is here allow for ExUI loading spinner to finish
            .click();
        await expect(this.page.getByText('has been created.')).toBeVisible();

    }

    async checkCaseIsCreated(caseName: string) {
        await this.page.getByRole("link", {name: "Case list"}).click();
        await this.page.getByLabel("Jurisdiction").selectOption("Public Law");
        await this.page
            .getByLabel("Case type")
            .selectOption("Public Law Applications");
        await this.page.getByLabel("State").selectOption("Any");
        await this.page.getByLabel("Apply filter").click();
        await this.page.getByLabel("Case name").fill(caseName);
        await this.page.getByLabel("Apply filter").click();
        expect(this.page.getByText(caseName)).toBeVisible();
        await this.page.getByText(caseName).click();
    }

    async selectLA(localAuthority: string) {
        await this.localAuthorityOption.selectOption(localAuthority);
    }

    async shareWithOrganisationUser(share: string) {
        await this.page.getByLabel(`${share}`).check();
    }

    async fillcaseName(caseName: string) {
        await this.caseNameTextBox.fill(caseName);
    }

    async submitOutSourceCase() {
        await this.clickSubmit();
    }

    async getCaseNumber() {
        await this.page.waitForURL('**/case-details/**');
        let urlarry: string[];
        let url: string = await this.page.url();
        urlarry = url.split('/');
        this._casenumber = urlarry[5].slice(0, 16);
    }

    async findCase(casenumber: string) {
        await this.caseListLink.click();
        await this.caseJurisdictionFilterDropdown.selectOption('Public Law');
        await this.caseTypeFilterDropdown.selectOption('Public Law Applications')
        await this.caseNumberTextBox.fill(casenumber);
        await this.applyFilter.click();
    }

    async selectRepresentLA() {
        await this.representingPartyRadio.check();
    }

    async respondentSolicitorCreatCase() {
        await this.respondentSolicitorUser.check();
        await this.applicationFor.selectOption('Barnet Borough Hillingdon');
        await this.clickContinue();
    }
}
