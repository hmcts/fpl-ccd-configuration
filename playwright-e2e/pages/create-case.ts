import { type Page, type Locator, expect } from "@playwright/test";
import { CreateCaseName } from "../utils/create-case-name";

export class CreateCase{
  readonly page: Page;
  readonly caseJurisdictionFilterDropdown: Locator;
  readonly caseTypeFilterDropdown: Locator;
  readonly createCaseLink: Locator;
  readonly addApplicationTitle: Locator;
  readonly viewHistory: Locator;
  generatedCaseName: string;

  public constructor(page: Page) {
    this.page = page;
    this.createCaseLink = page.getByRole("link", { name: "Create case" });
    this.caseJurisdictionFilterDropdown = this.page.getByLabel("Jurisdiction");
    this.caseTypeFilterDropdown = this.page.getByLabel("Case type");
    this.addApplicationTitle = this.page.getByRole("heading", {
      name: "Add application details",
    });
    this.viewHistory = page.getByText("History");
    this.generatedCaseName = "";
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

   caseName()  {
    const formattedDate = CreateCaseName.getFormattedDate();
    this.generatedCaseName = `Smoke Test ${formattedDate}`;
  }

  async submitCase(caseName: string) {
    await this.page.getByLabel("Case name").click();
    await this.page.getByLabel("Case name").fill(caseName);
    await this.page
      .getByRole("button", { name: "Submit" })
      // This click timeout is here allow for ExUI loading spinner to finish
      .click();
    await this.addApplicationTitle.isVisible();

    // This click timeout is here allow for ExUI loading spinner to finish
    await this.viewHistory.click();
  }

  async checkCaseIsCreated(caseName: string) {
    await this.page.getByRole("link", { name: "Case list" }).click();
    await this.page.getByLabel("Jurisdiction").selectOption("Public Law");
    await this.page
      .getByLabel("Case type")
      .selectOption("Public Law Applications");
    await this.page.getByLabel("State").selectOption("Any");
    await this.page.getByLabel("Apply filter").click();
    await this.page.getByLabel("Day").click();
    await this.page.getByLabel("Case name").click();
    await this.page.getByLabel("Case name").fill(caseName);
    await this.page.getByLabel("Apply filter").click();
    await this.page.getByLabel("Day").click();
    expect(this.page.getByText(caseName)).toBeVisible();
    await this.page.getByText(caseName).click();
  }
}
