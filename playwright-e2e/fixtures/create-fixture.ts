import { test as base } from "@playwright/test";
import { SignInPage } from "../pages/sign-in";
import { CreateCase } from "../pages/create-case";
import { StartApplication } from "../pages/start-application";
import { OrdersAndDirectionSought } from "../pages/orders-and-directions";
import { HearingUrgency } from "../pages/hearing-urgency";
import { GroundsForTheApplication } from "../pages/grounds-for-the-application";

type CreateFixtures = {
  signInPage: SignInPage;
  createCase: CreateCase;
  startApplication: StartApplication;
  ordersAndDirectionSought: OrdersAndDirectionSought;
  hearingUrgency : HearingUrgency;
  groundsForTheApplication : GroundsForTheApplication;
};

export const test = base.extend<CreateFixtures>({
  signInPage: async ({ page }, use) => {
    await use(new SignInPage(page));
  },

  createCase: async ({ page }, use) => {
    await use(new CreateCase(page));
  },

  startApplication: async ({ page }, use) => {
    await use(new StartApplication(page));
  },

  ordersAndDirectionSought: async ({ page }, use) => {
    await use(new OrdersAndDirectionSought(page));
  },

  hearingUrgency: async ({ page }, use) => {
    await use(new HearingUrgency(page));
  },

  groundsForTheApplication: async ({ page }, use) => {
    await use(new GroundsForTheApplication(page));
  },
});
