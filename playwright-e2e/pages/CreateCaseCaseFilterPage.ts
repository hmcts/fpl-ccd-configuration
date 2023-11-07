import { type Page, type Locator, expect } from "@playwright/test";

export class SmokeCreateCase {
  readonly page: Page;
  readonly caseJurisdictionFilterDropdown: Locator;
  readonly caseTypeFilterDropdown: Locator;
  readonly createCaseLink: Locator;
  readonly addApplicationTitle: Locator;
  readonly viewHistory: Locator;
  generatedCaseName: any;

  public constructor(page: Page) {
    this.page = page;
    this.createCaseLink = page.getByRole("link", { name: "Create case" });
    this.caseJurisdictionFilterDropdown = this.page.getByLabel("Jurisdiction");
    this.caseTypeFilterDropdown = this.page.getByLabel("Case type");
    this.addApplicationTitle = this.page.getByRole("heading", {
      name: "Add application details",
    });
    this.viewHistory = page.getByText("History");
  }

  async CreateCase() {
    // This click timeout is here allow for ExUI loading spinner to finish
    await this.createCaseLink.click();
    await this.caseJurisdictionFilterDropdown.selectOption("PUBLICLAW");
    await this.caseTypeFilterDropdown.selectOption("CARE_SUPERVISION_EPO");
    await this.page.getByLabel("Event").selectOption("openCase");
    await this.page.getByRole("button", { name: "Start" }).click();
  }

  async CaseName() {
    const currentDate = new Date();

    // Format the date and time components
    const year = currentDate.getFullYear();
    const month = currentDate.toLocaleString("en-UK", { month: "long" });
    const day = currentDate.getDate();
    const hours = currentDate.getHours().toString().padStart(2, "0");
    const minutes = currentDate.getMinutes().toString().padStart(2, "0");
    const seconds = currentDate.getSeconds().toString().padStart(2, "0");
    const milliseconds = currentDate
      .getMilliseconds()
      .toString()
      .padStart(3, "0");

    // Create the timestamp string
    const timestamp = `${day} ${month} ${year}, ${hours}:${minutes}:${seconds}.${milliseconds}`;

    const caseName = `Playwright only e2e smoke test ${timestamp}`;
    this.generatedCaseName = caseName;

    console.log("Case name:", caseName);
  }

  async SubmitCase(caseName) {
    await this.page.getByLabel("Case name").click();
    await this.page.getByLabel("Case name").fill(caseName);
    await this.page
      .getByRole("button", { name: "Submit" })
      // This click timeout is here allow for ExUI loading spinner to finish
      .click();
    await this.addApplicationTitle.isVisible;

    // This click timeout is here allow for ExUI loading spinner to finish
    await this.viewHistory.click();
  }

  async CheckCaseIsCreated(caseName) {
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
    await expect(this.page.getByText(caseName)).toBeVisible({ timeout: 75000 });
    await this.page.getByText(caseName).click();
  }
}
