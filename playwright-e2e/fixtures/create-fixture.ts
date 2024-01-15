import { test as base } from '@playwright/test';
import { SignInPage } from "../pages/sign-in";
import { SmokeCreateCase } from "../pages/create-case";
import { StartApplication } from "../pages/start-application";
import { OrdersAndDirectionSought } from "../pages/orders-and-directions";

type CreateFixtures = {
    signInPage: SignInPage;
    smokeCreateCase : SmokeCreateCase
    startApplication : StartApplication
    ordersAndDirectionSought : OrdersAndDirectionSought
}

export const test = base.extend<CreateFixtures>({

    signInPage: async ({ page }, use) => {
      await use(new SignInPage(page));
    },

    smokeCreateCase: async ({ page }, use) => {
        await use(new SmokeCreateCase(page));
      },
  
    startApplication: async ({ page }, use) => {
      await use(new StartApplication(page));
    },

    ordersAndDirectionSought: async ({ page }, use) => {
        await use(new OrdersAndDirectionSought(page));
      },
  });