import { test } from "../fixtures/create-fixture";
import { newSwanseaLocalAuthorityUserOne } from "../settings/userCredentials";

test("Smoke Test @smoke-test", async ({ signInPage, smokeCreateCase, ordersAndDirectionSought, startApplication }) => {

  await signInPage.visit();
  await signInPage.login(
    newSwanseaLocalAuthorityUserOne.email,
    newSwanseaLocalAuthorityUserOne.password,
  );
  await signInPage.isSignedIn();
  await smokeCreateCase.CaseName();
  await smokeCreateCase.CreateCase();
  await smokeCreateCase.SubmitCase(smokeCreateCase.generatedCaseName);
  await smokeCreateCase.CheckCaseIsCreated(smokeCreateCase.generatedCaseName);
  await ordersAndDirectionSought.OrdersAndDirectionsNeeded();
  await startApplication.AddApplicationDetailsHeading.isVisible();
});
