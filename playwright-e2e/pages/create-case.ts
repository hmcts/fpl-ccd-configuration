import { type Page, type Locator, expect } from "@playwright/test";
import { CreateCaseName } from "../utils/create-case-name";
import {BasePage} from "./base-page";

export class CreateCase extends BasePage{
  readonly page: Page;
  readonly caseJurisdictionFilterDropdown: Locator;
  readonly caseTypeFilterDropdown: Locator;
  readonly createCaseLink: Locator;
  readonly addApplicationTitle: Locator;
  readonly viewHistory: Locator;
  generatedCaseName: string;
  readonly localAuthority: Locator;
  readonly startButton: Locator;
  readonly eventOption: Locator;
  readonly localAuthorityOption: Locator;
  urlarry: string[];
  casenumber: string;
  readonly caseListLink: Locator;
  readonly caseNumberTextBox: Locator;
  readonly applyFilter: Locator;
  private caseNameTextBox: Locator;
  private representingPartyRadio: Locator;

  public constructor(page: Page) {
      super(page);
    this.page = page;
    this.createCaseLink = page.getByRole("link", { name: "Create case" });
    this.caseJurisdictionFilterDropdown = this.page.getByLabel("Jurisdiction");
    this.caseTypeFilterDropdown = this.page.getByLabel("Case type");
    this.addApplicationTitle = this.page.getByRole("heading", {
      name: "Add application details",
    });
    this.viewHistory = page.getByText("History");
    this.generatedCaseName = "";
    this.localAuthority = page.getByLabel('Select the local authority you\'re representing');
    this.caseNameTextBox = page.getByLabel('Case name');
    this.startButton =page.getByRole("button", { name: 'Start' });
    this.eventOption = page.getByLabel('Event');
    this.localAuthorityOption = page.getByLabel('Select the local authority you\'re representing');
    this.casenumber = '';
    this.urlarry= [];
    this.caseListLink = page.getByRole('link', { name: ' Case list ' });
    this.caseNumberTextBox = page.getByLabel('CCD Case Number');
    this.applyFilter = page.getByLabel('Apply filter');
    this.representingPartyRadio = page.getByLabel('Local Authority', { exact: true });
  }

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
        await this.submit.click();
  }

    async getCaseNumber(){
        await this.page.waitForURL('**/case-details/**');
        let url:string= await this.page.url();
        this.urlarry = url.split('/');
        this.casenumber =  this.urlarry[5].slice(0,16);
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
}
