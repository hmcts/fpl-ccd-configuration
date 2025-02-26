import { type Page, type Locator, expect } from "@playwright/test";
import {BasePage} from "./base-page";

export class OrdersAndDirectionSought extends BasePage{
  readonly page: Page;
  readonly OrdersAndDirectionsHeading: Locator;
  readonly OrdersAndDirectionsSought: Locator;
  readonly WhichOrdersDoYouNeedCareOrder: Locator;
  readonly DoYouNeedAnyOtherDirectionsRadioNo: Locator;
  readonly WhichCourtAreYouIssuingFor: Locator;

  public constructor(page: Page) {
    super(page);
    this.page = page;
    this.OrdersAndDirectionsHeading = page.getByRole("heading", {name: "Orders and directions sought",level:1});
    this.OrdersAndDirectionsSought = page.getByRole("link", { name: "Orders and directions sought" });
    this.WhichOrdersDoYouNeedCareOrder = page.getByLabel("Care order", { exact: true });
    this.DoYouNeedAnyOtherDirectionsRadioNo = page.getByRole('radio', { name: 'No' });
    this.WhichCourtAreYouIssuingFor = page.getByRole('group', { name: 'Orders and directions sought' }).getByLabel('Which court are you issuing');
  }

  async ordersAndDirectionsNeeded() {
      await expect(this.OrdersAndDirectionsSought).toBeVisible();
    await this.OrdersAndDirectionsSought.click();
    await expect(this.OrdersAndDirectionsHeading).toBeVisible();
    await this.WhichOrdersDoYouNeedCareOrder.click();
    await this.DoYouNeedAnyOtherDirectionsRadioNo.check();
    await this.WhichCourtAreYouIssuingFor.selectOption('Barnet');
    await this.clickContinue();
    await this.checkYourAnsAndSubmit();
  }
}
