import { type Page, type Locator, expect } from "@playwright/test";

export class StartApplication {
  readonly page: Page;
  readonly addApplicationDetailsHeading: Locator;
  readonly changeCaseNameLink: Locator;
  readonly ordersAndDirectionsSoughtLink: Locator;
  readonly hearingUrgencyLink: Locator;
  readonly addGroundsForTheApplicationHeading: Locator;
  readonly groundsForTheApplicationLink: Locator;
  readonly riskAndHarmToChildrenLink: Locator;
  readonly hearingUrgencyHeader: Locator;
  readonly groundsForTheApplicationHeading: Locator;
  readonly groundsForTheApplicationHasBeenUpdatedAlert: Locator;

  public constructor(page: Page) {
    this.page = page;
    this.addApplicationDetailsHeading = page.getByRole("heading", { name: "Add application details"} );
    this.ordersAndDirectionsSoughtLink = page.getByRole("heading", { name: "Orders and directions sought",});
    this.hearingUrgencyLink = page.getByRole('link', { name: 'Hearing urgency' });  
    this.hearingUrgencyHeader = page.getByRole('heading', { name: 'Hearing urgency' });
    this.groundsForTheApplicationLink = page.getByRole('link', { name: 'Grounds for the application' });
    this.groundsForTheApplicationHeading = page.getByRole('heading', { name: 'Grounds for the application' });
    this.groundsForTheApplicationHasBeenUpdatedAlert = page.getByText('ping pong rubbish has been updated with event: Grounds for the application');
  }

  async addApplicationDetails(){
    await expect (this.addApplicationDetailsHeading).toBeVisible();
  }

  async ordersAndDirectionsSought() {
    await this.ordersAndDirectionsSoughtLink.isVisible();
    await this.ordersAndDirectionsSoughtLink.click();
  }

  async hearingUrgency() {
    await this.hearingUrgencyLink.isVisible();
    await this.hearingUrgencyLink.click();
    await expect (this.hearingUrgencyHeader).toBeVisible();
  }

  async groundsForTheApplication() {
    await this.groundsForTheApplicationLink.isVisible();
    await this.groundsForTheApplicationLink.click();
    await expect (this.groundsForTheApplicationHeading).toBeVisible();
  }

  async groundsForTheApplicationHasBeenUpdated() {
    await expect (this.groundsForTheApplicationHasBeenUpdatedAlert).toBeVisible
  }
}
