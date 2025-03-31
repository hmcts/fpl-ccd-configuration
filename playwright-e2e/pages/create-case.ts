import { type Page, type Locator, expect } from "@playwright/test";
import { CreateCaseName } from "../utils/create-case-name";
import {BasePage} from "./base-page";

export class CreateCase extends BasePage{
    get casenumber(): string {
        return this._casenumber;
    }
    get generatedCaseName(): string {
        return <string>this._generatedCaseName;
    }
    private _generatedCaseName: string | undefined;
    constructor(page: Page, urlarry: string[], casenumber: string) {
        super(page);
        this.urlarry = urlarry;
        this._casenumber = casenumber;
    }
    urlarry: string[];
    private _casenumber: string;
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
        return this.page.getByRole("button", { name: 'Start' });
    }

    get eventOption(): Locator {
        return this.page.getByLabel('Event');
    }

    get localAuthorityOption(): Locator {
        return this.page.getByLabel('Select the local authority you\'re representing');
    }

    get caseListLink(): Locator {
        return this.page.getByRole('link', { name: ' Case list ' });
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
        return this.page.getByLabel('Local Authority', { exact: true });
    }
  // private readonly _caseJurisdictionFilterDropdown: Locator;
  // private readonly _caseTypeFilterDropdown: Locator;
  // private readonly _createCaseLink: Locator;
  // private readonly _addApplicationTitle: Locator;
  // private readonly _viewHistory: Locator;
  // generatedCaseName: string;
  // private readonly _localAuthority: Locator;
  // private readonly _startButton: Locator;
  // private readonly _eventOption: Locator;
  // private readonly _localAuthorityOption: Locator;
  // urlarry: string[];
  // casenumber: string;
  // private readonly _caseListLink: Locator;
  // private readonly _caseNumberTextBox: Locator;
  // private readonly _applyFilter: Locator;
  // private _caseNameTextBox: Locator;
  // private _representingPartyRadio: Locator;

  // public constructor(page: Page) {
  //     super(page);
  //   // this._createCaseLink =
  //   // this._caseJurisdictionFilterDropdown =
  //   // this._caseTypeFilterDropdown =
  //   // this._addApplicationTitle =
  //   // this._viewHistory =
  //   // this.generatedCaseName = "";
  //   // this._localAuthority =
  //   // this._caseNameTextBox =
  //   // this._startButton =
  //   // this._eventOption =
  //   // this._localAuthorityOption =
  //   // this.casenumber = '';
  //   // this.urlarry= [];
  //   // this._caseListLink =
  //   // this._caseNumberTextBox =
  //   // this._applyFilter =
  //   // this._representingPartyRadio =
  // }

  async createCase() {
    // This click timeout is here allow for ExUI loading spinner to finish
    await this.createCaseLink.click();

    await this.caseJurisdictionFilterDropdown.selectOption("PUBLICLAW").catch(
      (error)=>{
           this.page.waitForTimeout(500);
           console.log(error);
           console.log(" the page reloaded to ");
           this.page.reload({timeout:3000,waitUntil:'load'});
         }
       )
    await this.caseJurisdictionFilterDropdown.selectOption("PUBLICLAW");
    await this.caseTypeFilterDropdown.selectOption("CARE_SUPERVISION_EPO");
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
        await this.localAuthorityOption.selectOption(localAuthority);
  }

    async shareWithOrganisationUser(share:string){
        await this.page.getByLabel(`${share}`).check();
  }

    async fillcaseName(caseName:string) {
        await this.caseNameTextBox.fill(caseName);
  }

    async submitOutSourceCase(){
        await this.clickSubmit();
  }

    async getCaseNumber(){
        await this.page.waitForURL('**/case-details/**');
        let url:string= await this.page.url();
        this.urlarry = url.split('/');
        this._casenumber =  this.urlarry[5].slice(0,16);
  }

    async findCase(casenumber:string){
        await this.caseListLink.click();
        await this.caseJurisdictionFilterDropdown.selectOption('Public Law');
        await this.caseTypeFilterDropdown.selectOption('Public Law Applications')
        await this.caseNumberTextBox.fill(casenumber);
        await this.applyFilter.click();
  }

    async selectRepresentLA(){
        await this.representingPartyRadio.check();
  }

    // async respondentSolicitorCreatCase(){
    //     await this.respondentSolicitorUser.check();
    //     await this.applicationFor.selectOption('Barnet Borough Hillingdon');
    //     await this.clickContinue();
    // }
}
