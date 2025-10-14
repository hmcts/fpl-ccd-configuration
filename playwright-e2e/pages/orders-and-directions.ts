import { type Page, type Locator, expect } from "@playwright/test";
import {BasePage} from "./base-page";

export class OrdersAndDirectionSought extends BasePage{
  readonly page: Page;
  readonly OrdersAndDirectionsHeading: Locator;
  readonly ordersAndDirectionsSoughtLink: Locator;
  readonly careOrderCheckbox: Locator;
  readonly DoYouNeedAnyOtherDirections: Locator;
  readonly WhichCourtAreYouIssuingFor: Locator;
  readonly variationOfSupervisionOrder: Locator;
  readonly orderDetails: Locator;
  readonly contactWithChild: Locator;

  public constructor(page: Page) {
    super(page);
    this.page = page;
    this.OrdersAndDirectionsHeading = page.getByRole("heading", {name: "Orders and directions sought",level:1});
    this.ordersAndDirectionsSoughtLink = page.getByRole("link", { name: "Orders and directions sought" });
    this.careOrderCheckbox = page.getByLabel("Care order", { exact: true });
    this.DoYouNeedAnyOtherDirections = page.getByRole('group', { name: 'Do you need any other directions?' });
    this.WhichCourtAreYouIssuingFor = page.getByRole('group', { name: 'Orders and directions sought' }).getByLabel('Which court are you issuing');
    this.variationOfSupervisionOrder =page.getByRole('group', { name: 'Which orders are you asking' }).getByRole('checkbox', { name: 'Variation of supervision' });
    this.contactWithChild = page.getByRole('group', { name: 'Which orders are you asking' }).getByRole('checkbox', { name: 'Contact with child in care' });
    this.orderDetails = page.getByRole('textbox', { name: 'Which order do you need?' });
  }

  async ordersAndDirectionsNeeded(): Promise<void> {
      await Promise.all([
          this.page.waitForResponse((response) =>
              response.url().includes('ordersNeeded') &&
              response.status() === 200
          ),
          this.ordersAndDirectionsSoughtLink.click()
      ]);
      await this.careOrderCheckbox.click();
      await this.DoYouNeedAnyOtherDirections.getByRole('radio', { name: 'No' }).check();
      await this.WhichCourtAreYouIssuingFor.selectOption('Barnet');
      await Promise.all([
          this.page.waitForResponse((response) =>
              response.url().includes('validate?pageId=ordersNeeded1') &&
              response.status() === 200
          ),
          this.clickContinue()
      ]);
      await Promise.all([
          this.page.waitForResponse((response) =>
            response.url().includes('api/wa-supported-jurisdiction/get') &&
            response.status() === 200
          ),
          this.checkYourAnsAndSubmit()
      ]);
  }

  async SoliciotrC110AAppOrderAndDirectionNeeded(){

      // await expect(this.OrdersAndDirectionsSought).toBeVisible();
      // await this.OrdersAndDirectionsSought.click();
      await expect(()=>{
          expect(this.ordersAndDirectionsSoughtLink).toBeVisible();
          this.ordersAndDirectionsSoughtLink.click();
          expect(this.ordersAndDirectionsSoughtLink).toBeHidden();
          this.page.reload();
      }).toPass();
      await expect(this.OrdersAndDirectionsHeading).toBeVisible();

      await this.variationOfSupervisionOrder.check();
      await this.contactWithChild.check();
      await this.orderDetails.fill('Order to contact the child at foster care');
      await this.page.getByRole('radio', { name: 'No' }).check();
      await this.WhichCourtAreYouIssuingFor.selectOption('Barnet');

      await this.clickContinue();
      await expect.soft(this. page.getByText('You have selected a standalone order, this cannot be applied for alongside other orders.')).toBeVisible();
      await this.contactWithChild.uncheck();
      await this.clickContinue();
      await this.checkYourAnsAndSubmit();
      await expect(this.page.getByText('has been updated with event:')).toBeVisible();
  }
}
