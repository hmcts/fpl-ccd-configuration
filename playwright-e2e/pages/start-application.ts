import { type Page, type Locator, expect } from "@playwright/test";

export class StartApplication {
  readonly page: Page;
  readonly AddApplicationDetailsHeading: Locator;
  readonly ChangeCaseNameLink: Locator;
  readonly OrdersAndDirectionsSoughtLink: Locator;
  readonly HearingUrgencyLink: Locator;
  readonly AddGroundsForTheApplicationHeading: Locator;
  readonly GroundsForTheApplicationLink: Locator;
  readonly RiskAndHarmToChildrenLink: Locator;
  readonly HearingUrgencyHeader: Locator;

  public constructor(page: Page) {
    this.page = page;
    this.AddApplicationDetailsHeading = page.getByRole("heading", { name: "Add application details"} );
    this.OrdersAndDirectionsSoughtLink = page.getByRole("heading", { name: "Orders and directions sought",});
    this.HearingUrgencyLink = page.getByRole('link', { name: 'Hearing urgency' })  
    this.HearingUrgencyHeader = page.getByRole('heading', { name: 'Hearing urgency' })
  }

  async addApplicationDetails(){
    await expect (this.AddApplicationDetailsHeading).toBeVisible();
  }

  async ordersAndDirectionsSought() {
    await this.OrdersAndDirectionsSoughtLink.isVisible();
    await this.OrdersAndDirectionsSoughtLink.click();
  }

  async hearingUrgency() {
    await this.HearingUrgencyLink.isVisible();
    await this.HearingUrgencyLink.click();
    await expect (this.HearingUrgencyHeader).toBeVisible();
  }
}
