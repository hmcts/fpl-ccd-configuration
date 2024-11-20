import { type Page, type Locator, expect } from "@playwright/test";

export class OrdersAndDirectionSought {
  readonly page: Page;
  readonly OrdersAndDirectionsHeading: Locator;
  readonly OrdersAndDirectionsSought: Locator;
  readonly WhichOrdersDoYouNeedCareOrder: Locator;
  readonly DoYouNeedAnyOtherDirectionsRadioNo: Locator;
  readonly WhichCourtAreYouIssuingFor: Locator;
  readonly Continue: Locator;
  readonly CheckYourAnswers: Locator;
  readonly SaveAndContinue: Locator;

  public constructor(page: Page) {
    this.page = page;
    this.OrdersAndDirectionsHeading = page.getByRole("heading", {name: "Orders and directions needed"});
    this.OrdersAndDirectionsSought = page.getByRole("link", { name: "Orders and directions sought" });
    this.WhichOrdersDoYouNeedCareOrder = page.getByLabel("Care order", { exact: true });
    this.DoYouNeedAnyOtherDirectionsRadioNo = page.getByRole('radio', { name: 'No' });
    this.WhichCourtAreYouIssuingFor = page.getByRole('group', { name: 'Orders and directions needed' }).getByLabel('*Which court are you issuing');
    this.Continue = page.getByRole('button', { name: 'Continue' });
    this.CheckYourAnswers = page.getByRole('heading', { name: 'Check your answers' });
    this.SaveAndContinue = page.getByRole('button', { name: 'Save and continue' });
  }

  async ordersAndDirectionsNeeded() {
    await this.OrdersAndDirectionsSought.click();
    await expect(this.OrdersAndDirectionsHeading).toBeVisible();
    await this.WhichOrdersDoYouNeedCareOrder.check();
    await this.DoYouNeedAnyOtherDirectionsRadioNo.scrollIntoViewIfNeeded({timeout: 100})
    await this.DoYouNeedAnyOtherDirectionsRadioNo.check({force: true});
    await this.WhichCourtAreYouIssuingFor.selectOption('2: 117');
    await this.Continue.click();
    await expect(this.CheckYourAnswers).toBeVisible;
    await this.SaveAndContinue.click();
  }
}
