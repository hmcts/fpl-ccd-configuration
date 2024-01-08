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

    // await page.getByLabel('Care order', { exact: true }).check();
    // await page.getByLabel('Interim care order').check();
    // await page.getByLabel('Interim care order').uncheck();
    // await page.getByRole('radio', { name: 'No' }).check();
    // await page.getByRole('group', { name: 'Orders and directions needed' }).getByLabel('*Which court are you issuing for? (Optional)').selectOption('37: 262');
    // await page.getByRole('button', { name: 'Continue' }).click();
    // await page.getByRole('heading', { name: 'Check your answers' }).click();
    // await page.getByRole('button', { name: 'Save and continue' }).click();
    // await page.goto('https://manage-case.aat.platform.hmcts.net/cases/case-details/1699273737290970');
    // await page.goto('https://manage-case.aat.platform.hmcts.net/cases/case-details/1699273737290970#Start%20application');
    // await page.getByText('C110a Application', { exact: true }).click();
  }
}
