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

  public constructor(page: Page) {
    this.page = page;
    this.OrdersAndDirectionsSoughtLink = page.getByRole("heading", {
      name: "Orders and directions sought",
    });
  }

  async OrdersAndDirectionsSought() {
    await this.OrdersAndDirectionsSoughtLink.isVisible();
    await this.OrdersAndDirectionsSoughtLink.click();
  }
}
