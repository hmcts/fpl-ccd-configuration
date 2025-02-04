import { type Page, type Locator, expect } from "@playwright/test";
import { CreateCaseName } from "../utils/create-case-name";
import {BasePage} from "./base-page";

export class CreateCase extends BasePage{
    get caseJurisdictionFilterDropdown(): Locator {
        return this.page.getByLabel("Jurisdiction");
    }

    get caseTypeFilterDropdown(): Locator {
        return this.page.getByLabel("Case type");
    }

    get createCaseLink(): Locator {
        return this.page.getByRole("link", { name: "Create case" });
    }

    get addApplicationTitle(): Locator {
        return this.page.getByRole("heading", {
            name: "Add application details",
        });
    }

    get viewHistory(): Locator {
        return this.page.getByText("History");
    }

    get localAuthority(): Locator {
        return this.page.getByLabel('Select the local authority you\'re representing');
    }

    get startButton(): Locator {
        return this._startButton;
    }

    get eventOption(): Locator {
        return this._eventOption;
    }

    get localAuthorityOption(): Locator {
        return this._localAuthorityOption;
    }

    get caseListLink(): Locator {
        return this._caseListLink;
    }

    get caseNumberTextBox(): Locator {
        return this._caseNumberTextBox;
    }

    get applyFilter(): Locator {
        return this._applyFilter;
    }

    get caseNameTextBox(): Locator {
        return this._caseNameTextBox;
    }

    get representingPartyRadio(): Locator {
        return this._representingPartyRadio;
    }
  private readonly _caseJurisdictionFilterDropdown: Locator;
  private readonly _caseTypeFilterDropdown: Locator;
  private readonly _createCaseLink: Locator;
  private readonly _addApplicationTitle: Locator;
  private readonly _viewHistory: Locator;
  generatedCaseName: string;
  private readonly _localAuthority: Locator;
  private readonly _startButton: Locator;
  private readonly _eventOption: Locator;
  private readonly _localAuthorityOption: Locator;
  urlarry: string[];
  casenumber: string;
  private readonly _caseListLink: Locator;
  private readonly _caseNumberTextBox: Locator;
  private readonly _applyFilter: Locator;
  private _caseNameTextBox: Locator;
  private _representingPartyRadio: Locator;

  public constructor(page: Page) {
      super(page);
    this._createCaseLink =
    this._caseJurisdictionFilterDropdown =
    this._caseTypeFilterDropdown =
    this._addApplicationTitle =
    this._viewHistory =
    this.generatedCaseName = "";
    this._localAuthority =
    this._caseNameTextBox = page.getByLabel('Case name');
    this._startButton =page.getByRole("button", { name: 'Start' });
    this._eventOption = page.getByLabel('Event');
    this._localAuthorityOption = page.getByLabel('Select the local authority you\'re representing');
    this.casenumber = '';
    this.urlarry= [];
    this._caseListLink = page.getByRole('link', { name: ' Case list ' });
    this._caseNumberTextBox = page.getByLabel('CCD Case Number');
    this._applyFilter = page.getByLabel('Apply filter');
    this._representingPartyRadio = page.getByLabel('Local Authority', { exact: true });
  }

  async createCase() {
    // This click timeout is here allow for ExUI loading spinner to finish
    await this._createCaseLink.click();

    await this._caseJurisdictionFilterDropdown.selectOption("PUBLICLAW").catch(
      (error)=>{
           this.page.waitForTimeout(500);
           console.log(error);
           console.log(" the page reloaded to ");
           this.page.reload({timeout:3000,waitUntil:'load'});
         }
       )
    await this._caseJurisdictionFilterDropdown.selectOption("PUBLICLAW");
    await this._caseTypeFilterDropdown.selectOption("CARE_SUPERVISION_EPO");
    await this.page.getByLabel("Event").selectOption("openCase");
    await this.page.getByRole("button", { name: "Start" }).click();
  }

  caseName(testType: string = 'Smoke Test'): void {
    const formattedDate = CreateCaseName.getFormattedDate();
    this.generatedCaseName = `${testType} ${formattedDate}`;
  }

  async submitCase(caseName: string) {
    await this.page.getByLabel("Case name").click();
    await this.page.getByLabel("Case name").fill(caseName);
    await this.page
      .getByRole("button", { name: "Submit" })
      // This click timeout is here allow for ExUI loading spinner to finish
      .click();
  }

  async checkCaseIsCreated(caseName: string) {
    await this.page.getByRole("link", { name: "Case list" }).click();
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

    async selectLA(localAuthority: string){
        await this._localAuthorityOption.selectOption(localAuthority);
  }

    async shareWithOrganisationUser(share:string){
        await this.page.getByLabel(`${share}`).check();
  }

    async fillcaseName(caseName:string) {
        await this._caseNameTextBox.fill(caseName);
  }

    async submitOutSourceCase(){
        await this.clickSubmit();
  }

    async getCaseNumber(){
        await this.page.waitForURL('**/case-details/**');
        let url:string= await this.page.url();
        this.urlarry = url.split('/');
        this.casenumber =  this.urlarry[5].slice(0,16);
  }

    async findCase(casenumber:string){
        await this._caseListLink.click();
        await this._caseJurisdictionFilterDropdown.selectOption('Public Law');
        await this._caseTypeFilterDropdown.selectOption('Public Law Applications')
        await this._caseNumberTextBox.fill(casenumber);
        await this._applyFilter.click();
  }

    async selectRepresentLA(){
        await this._representingPartyRadio.check();
  }
}
