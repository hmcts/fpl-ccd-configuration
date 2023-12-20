import { type Page, type Locator, expect } from "@playwright/test";

export class OrdersAndDirections {
    readonly page: Page;
    readonly headingOrdersAndDirections: Locator;

    public constructor(page: Page) {
        this.page = page;
        this.headingOrdersAndDirections = page.getByRole('heading', { name: 'Orders and directions needed' });
    
      }

      async OrdersAndDirectionsNeeded() {
        await this.headingOrdersAndDirections.isVisible();
        // await page.getByRole('heading', { name: 'Orders and directions needed' }).click();
        // await page.getByLabel('Care order', { exact: true }).check();
        // await page.getByLabel('Interim care order').check();
        // await page.getByRole('radio', { name: 'No' }).check();
        // await page.getByRole('group', { name: 'Orders and directions needed' }).getByLabel('*Which court are you issuing for? (Optional)').selectOption('37: 262');
        // await page.getByRole('button', { name: 'Continue' }).click();
        // await page.getByRole('heading', { name: 'Check your answers' }).click();
        // await page.getByRole('button', { name: 'Save and continue' }).click();
        // await page.getByText('C110a Application', { exact: true }).click();

      }
}