import {expect, type Locator, type Page} from "@playwright/test";
import {CreateCaseName} from "../utils/create-case-name";
import {BasePage} from "./base-page";

export class CreateCase extends BasePage{
  readonly page: Page;
  readonly caseJurisdictionFilterDropdown: Locator;
  readonly caseTypeFilterDropdown: Locator;
  readonly eventTypeFilterDropdown: Locator;
  readonly createCaseLink: Locator;
  readonly addApplicationTitle: Locator;
  readonly viewHistory: Locator;
  readonly caseNameLabel: Locator;
  readonly submitButton: Locator
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
  readonly respondentSolicitorUser: Locator;
  readonly applicationFor: any;

  public constructor(page: Page) {
      super(page);
    this.page = page;
    this.createCaseLink = page.getByRole("link", { name: "Create case" });
    this.caseJurisdictionFilterDropdown = this.page.getByLabel("Jurisdiction");
    this.caseTypeFilterDropdown = this.page.getByLabel("Case type");
    this.eventTypeFilterDropdown = this.page.getByLabel("Event");
    this.addApplicationTitle = this.page.getByRole("heading", {
      name: "Add application details",
    });
    this.viewHistory = page.getByText("History");
    this.caseNameLabel = page.getByLabel("Case name");
    this.submitButton = page.getByRole('button', { name: 'Submit' });
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
    this.respondentSolicitorUser = page.getByLabel('Respondent Solicitor');
    this.applicationFor = page.getByLabel('Select the local authority which relates to the case');
  }

  private async gotoCreateCase(): Promise<void> {
      await Promise.all([
          this.page.waitForResponse((response) =>
              response.url().includes('/aggregated/caseworkers/') &&
              response.url().includes('/jurisdictions?access=create') &&
              response.status() === 200
          ),
          this.createCaseLink.click()
      ]);
  }

  private async selectJurisdiction(option: string): Promise<void> {
      await this.caseJurisdictionFilterDropdown.selectOption(option);
  }

  private async selectCaseType(option: string): Promise<void> {
      await this.caseTypeFilterDropdown.selectOption(option);
  }

  private async selectEventType(option: string): Promise<void> {
      await this.eventTypeFilterDropdown.selectOption(option);
  }

  private async fillCaseNameLabel(value: string): Promise<void> {
      await this.caseNameLabel.fill(value);
  }

  async clickSubmit(): Promise<void> {
      await this.submitButton.click();
  }

  private generateCaseName(value: string = 'Smoke Test'): string {
      const formattedDate = CreateCaseName.getFormattedDate();
      return `${value} ${formattedDate}`;
  }

  async createCase(jurisdictionOption: string = 'PUBLICLAW',
                      caseTypeOption: string = 'CARE_SUPERVISION_EPO',
                      eventTypeOption: string = 'openCase') {
      await this.gotoCreateCase();
      await this.selectJurisdiction(jurisdictionOption);
      await this.selectCaseType(caseTypeOption);
      await this.selectEventType(eventTypeOption);
      await Promise.all([
          this.page.waitForResponse((response) =>
              response.url().includes('/event-triggers/openCase?ignore-warning=false') &&
              response.status() === 200
          ),
          this.clickStartButton()
      ]);
  }

  caseName(testType: string = 'Smoke Test'): void {
    const formattedDate = CreateCaseName.getFormattedDate();
    this.generatedCaseName = `${testType} ${formattedDate}`;
  }

  async submitCase(caseName: string = this.generateCaseName('Smoke Test')) {
      await this.fillCaseNameLabel(caseName)

      const [caseResponse] = await Promise.all([
          this.page.waitForResponse(response =>
              response.url().includes('/cases') && response.status() === 200
          ),
          this.page.waitForResponse(response =>
              response.url().includes('/api/wa-supported-jurisdiction/get') && response.status() === 200
          ),
          this.clickSubmit(),
      ]);

      const caseData = await caseResponse.json();
      const caseId = caseData.case_id;
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
        this.casenumber =  this.urlarry[7].slice(0,16);
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

    async respondentSolicitorCreatCase(){
        await this.respondentSolicitorUser.check();
        await this.applicationFor.selectOption('Swansea City Council');
        await this.clickContinue();
    }
}
