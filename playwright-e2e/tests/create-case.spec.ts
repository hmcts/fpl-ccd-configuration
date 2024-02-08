import { test, expect } from "@playwright/test";
import { SignInPage } from "../pages/sign-in";
import { newSwanseaLocalAuthorityUserOne } from "../settings/userCredentials";
import { CreateCase } from "../pages/create-case";
import { StartApplication } from "../pages/start-application";
import { OrdersAndDirectionSought } from "../pages/orders-and-directions";
import {  AllocationProposal  } from "../pages/allocation-proposal";

// WIP
test.skip("Create a case", async ({ page }) => {
  // 1. Sign in as local-authority user
  const signInPage = new SignInPage(page);
  const createCase = new CreateCase(page);
  const startApplication = new StartApplication(page);
  const ordersAndDirectionSought = new OrdersAndDirectionSought(page);
  const allocationProposal = new AllocationProposal(page);

  await signInPage.visit();
  await signInPage.login(
    newSwanseaLocalAuthorityUserOne.email,
    newSwanseaLocalAuthorityUserOne.password,
  );
  await signInPage.isSignedIn();
  // Add application details
  // Start new case, get case id and assert case id is created
  await createCase.caseName();
  await createCase.createCase();
  await createCase.submitCase(createCase.generatedCaseName);
  await createCase.checkCaseIsCreated(createCase.generatedCaseName);

  // Orders and directions sought
  await startApplication.ordersAndDirectionsSoughtLink.isVisible();
  await startApplication.ordersAndDirectionsSoughtLink.click();
  await ordersAndDirectionSought.OrdersAndDirectionsHeading.isVisible();
});
