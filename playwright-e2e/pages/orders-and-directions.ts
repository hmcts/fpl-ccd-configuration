import { type Page, type Locator, expect } from "@playwright/test";

export class OrdersAndDirectionSought {
  readonly page: Page;
  readonly OrdersAndDirectionsLink: Locator;
  readonly OrdersAndDirectionsHeading: Locator;

  public constructor(page: Page) {
    this.page = page;
    this.OrdersAndDirectionsHeading = page.getByRole("heading", {
      name: "Orders and directions needed",
    });
  }

  async OrdersAndDirectionsNeeded() {
    await this.OrdersAndDirectionsHeading.isVisible;

    await this.page.getByLabel('Care order', { exact: true }).check();
    await this.page.getByLabel('Interim care order').check();
    await this.page.getByLabel('Interim care order').uncheck();
    await this.page.getByRole('radio', { name: 'No' }).check();
    await this.page.getByRole('group', { name: 'Orders and directions needed' }).getByLabel('*Which court are you issuing for? (Optional)').selectOption('37: 262');
    await this.page.getByRole('button', { name: 'Continue' }).click();
    await this.page.getByRole('heading', { name: 'Check your answers' }).click();
    await this.page.getByRole('button', { name: 'Save and continue' }).click();
    await this.page.goto('https://manage-case.aat.platform.hmcts.net/cases/case-details/1699273737290970');
    await this.page.goto('https://manage-case.aat.platform.hmcts.net/cases/case-details/1699273737290970#Start%20application');
    await this.page.getByText('C110a Application', { exact: true }).click();
  }
}
