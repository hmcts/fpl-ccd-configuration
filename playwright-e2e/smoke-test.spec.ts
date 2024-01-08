import { test, expect } from "@playwright/test";
import { SignInPage } from "../playwright-e2e/pages/sign-in";
import { newSwanseaLocalAuthorityUserOne } from "../playwright-e2e/utils/userCredentials";

test("Smoke Test @smoke-test", async ({ page }) => {
  const signInPage = new SignInPage(page);
  await signInPage.visit();
  await signInPage.login(
    newSwanseaLocalAuthorityUserOne.email,
    newSwanseaLocalAuthorityUserOne.password,
  );
  await signInPage.isSignedIn();

});