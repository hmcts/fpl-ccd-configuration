import { type Page, type Locator, expect } from "@playwright/test";
import { BasePage } from "./base-page";

export class OrdersAndDirectionSought extends BasePage{
  readonly OrdersAndDirectionsHeading: Locator;
  readonly OrdersAndDirectionsSought: Locator;
  readonly WhichOrdersDoYouNeedCareOrder: Locator;
  readonly DoYouNeedAnyOtherDirectionsRadioNo: Locator;
  readonly WhichCourtAreYouIssuingFor: Locator;

  public constructor(page: Page) {
    super(page);
    this.OrdersAndDirectionsHeading = page.getByRole("heading", {name: "Orders and directions needed"});
    this.OrdersAndDirectionsSought = page.getByRole("link", { name: "Orders and directions sought" });
    this.WhichOrdersDoYouNeedCareOrder = page.getByLabel("Care order", { exact: true });
    this.DoYouNeedAnyOtherDirectionsRadioNo = page.getByRole('radio', { name: 'No' });
    this.WhichCourtAreYouIssuingFor = page.getByRole('group', { name: 'Orders and directions needed' }).getByLabel('*Which court are you issuing');
  }

  async ordersAndDirectionsNeeded() {
    await this.OrdersAndDirectionsSought.click();
    await expect(this.OrdersAndDirectionsHeading).toHaveText("Orders and directions needed");
    await this.WhichOrdersDoYouNeedCareOrder.check();
    await this.DoYouNeedAnyOtherDirectionsRadioNo.check();
    await this.WhichCourtAreYouIssuingFor.selectOption('2: 117');
    await this.clickContinue();
    await this.checkYourAnsAndSubmit();
  }
}
