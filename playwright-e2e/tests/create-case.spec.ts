import { test, expect } from "@playwright/test";
import { SignInPage } from "../pages/sign-in";
import { newSwanseaLocalAuthorityUserOne } from "../settings/userCredentials";
import { CreateCase } from "../pages/create-case";
import { StartApplication } from "../pages/start-application";
import { OrdersAndDirectionSought } from "../pages/orders-and-directions";
import {FactorsAffectingParenting} from "../pages/factors-affecting-parenting";
import { AllocationProposal } from "../pages/allocation-proposal";

// WIP
test.skip("Create a case", async ({ page }) => {
  // 1. Sign in as local-authority user
  const signInPage = new SignInPage(page);
  const createCase = new CreateCase(page);
  const startApplication = new StartApplication(page);
  const ordersAndDirectionSought = new OrdersAndDirectionSought(page);
  const factorsAffectingParenting = new FactorsAffectingParenting(page);
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

  // 4. Hearing urgency

  // Add application details
  // 5. Grounds for the application

  // 6. Risk and harm to children

  // 7. Grounds for the application

  // 8. Risk and harm to children

  // 9. Factors affecting parenting
  await startApplication.factorsAffectingParentingLink.isVisible();
  await startApplication.factorsAffectingParentingLink.click();
  await factorsAffectingParenting.factorsAffectingParentingHeading.isVisible();

  // Add application documents
  // 10. Upload documents

  // Add information about the parties
  // 11. Applicant's details

  // 12. Child's details

  // 13. Respondents' details

  // Add court requirements
  // 14. Allocation proposal

  // Add additional information
  // 15. Other proceedings

  // 16. International element

  // 17. Other people in the case
});
