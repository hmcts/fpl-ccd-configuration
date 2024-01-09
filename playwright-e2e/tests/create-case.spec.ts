import { test, expect } from "@playwright/test";
import { SignInPage } from "../pages/sign-in";
import { newSwanseaLocalAuthorityUserOne } from "../utils/userCredentials";
import { SmokeCreateCase } from "../pages/create-case";
import { StartApplication } from "../pages/start-application";
import { OrdersAndDirectionSought } from "../pages/orders-and-directions";

test("Create case @georgina", async ({ page }) => {
  // 1. Sign in as local-authority user 
  const signInPage = new SignInPage(page);
  const smokeCreateCase = new SmokeCreateCase(page);
  const startApplication = new StartApplication(page);
  const ordersAndDirectionSought = new OrdersAndDirectionSought(page);

  // TODO Navigate to case created

  await signInPage.visit();
  await signInPage.login(
    newSwanseaLocalAuthorityUserOne.email,
    newSwanseaLocalAuthorityUserOne.password,
  );
  await signInPage.isSignedIn();
  // Add application details
  // 2. Start new case, get case id and assert case id is created
  await smokeCreateCase.CaseName();
  await smokeCreateCase.CreateCase();
  await smokeCreateCase.SubmitCase(smokeCreateCase.generatedCaseName);
  await smokeCreateCase.CheckCaseIsCreated(smokeCreateCase.generatedCaseName);

  // 3. Orders and directions sought
  await startApplication.OrdersAndDirectionsSoughtLink.isVisible();
  await startApplication.OrdersAndDirectionsSoughtLink.click();
  await ordersAndDirectionSought.OrdersAndDirectionsHeading.isVisible();



  // 4. Hearing urgency

  // Add application details
  // 5. Grounds for the application

  // 6. Risk and harm to children 

  // 7. Grounds for the application

  // 8. Risk and harm to children

  // 9. Factors affecting parenting

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
