import { type Page, type Locator, expect } from "@playwright/test";
import {BasePage} from "./base-page";

export class OrdersAndDirectionSought extends BasePage{

    get OrdersAndDirectionsHeading(): Locator {
        return this.page.getByRole("heading", {name: "Orders and directions sought",level:1});
    }

    get OrdersAndDirectionsSought(): Locator {
        return this.page.getByRole("link", { name: "Orders and directions sought" });
    }

    get WhichOrdersDoYouNeedCareOrder(): Locator {
        return this.page.getByLabel("Care order", { exact: true });
    }

    get DoYouNeedAnyOtherDirections(): Locator {
        return this.page.getByRole('group', { name: 'Do you need any other directions?' });
    }

    get WhichCourtAreYouIssuingFor(): Locator {
        return this.page.getByRole('group', { name: 'Orders and directions sought' }).getByLabel('Which court are you issuing');
    }

    get variationOfSupervisionOrder(): Locator {
        return this.page.getByRole('group', { name: 'Which orders are you asking' }).getByRole('checkbox', { name: 'Variation of supervision' });
    }

    get orderDetails(): Locator {
        return this.page.getByRole('textbox', { name: 'Which order do you need?' });
    }

    get contactWithChild(): Locator {
        return this.page.getByRole('group', { name: 'Which orders are you asking' }).getByRole('checkbox', { name: 'Contact with child in care' });
    }
  //private readonly _page: Page;
  // private readonly _OrdersAndDirectionsHeading: Locator;
  // private readonly _OrdersAndDirectionsSought: Locator;
  // private readonly _WhichOrdersDoYouNeedCareOrder: Locator;
  // private readonly _DoYouNeedAnyOtherDirections: Locator;
  // private readonly _WhichCourtAreYouIssuingFor: Locator;
  // private readonly _variationOfSupervisionOrder: Locator;
  // private readonly _orderDetails: Locator;
  // private readonly _contactWithChild: Locator;

  // public constructor(page: Page) {
  //   super(page);
  //   this._page = page;
  //   this._OrdersAndDirectionsHeading =
  //   this._OrdersAndDirectionsSought =
  //   this._WhichOrdersDoYouNeedCareOrder =
  //   this._DoYouNeedAnyOtherDirections =
  //   this._WhichCourtAreYouIssuingFor =
  //   this._variationOfSupervisionOrder =
  //   this._contactWithChild =
  //   this._orderDetails =
  // }

  async ordersAndDirectionsNeeded() {
    await expect(this.OrdersAndDirectionsSought).toBeVisible();
    await this.OrdersAndDirectionsSought.click();
    await expect(this.OrdersAndDirectionsHeading).toBeVisible();
    await this.WhichOrdersDoYouNeedCareOrder.click();
    await this.DoYouNeedAnyOtherDirections.getByRole('radio', { name: 'No' }).check();
    await this.WhichCourtAreYouIssuingFor.selectOption('Barnet');
    await this.clickContinue();
    await this.checkYourAnsAndSubmit();
  }
  async SoliciotrC110AAppOrderAndDirectionNeeded(){

      await expect(this.OrdersAndDirectionsSought).toBeVisible();
      await this.OrdersAndDirectionsSought.click();
      await expect(this.OrdersAndDirectionsHeading).toBeVisible();

      await this.variationOfSupervisionOrder.check();
      await this.contactWithChild.check();
      await this.orderDetails.fill('Order to contact the child at foster care');
      await this.page.getByRole('radio', { name: 'No' }).check();
      await this.WhichCourtAreYouIssuingFor.selectOption('Barnet');

      await this.clickContinue();
      await expect.soft(this.page.getByText('You have selected a standalone order, this cannot be applied for alongside other orders.')).toBeVisible();
      await this.contactWithChild.uncheck();
      await this.clickContinue();
      await this.checkYourAnsAndSubmit();
  }
}
