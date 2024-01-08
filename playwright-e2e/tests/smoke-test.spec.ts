import { test, expect } from "@playwright/test";
import { SignInPage } from "../tests/pages/sign-in";
import { newSwanseaLocalAuthorityUserOne } from "../utils/userCredentials";

test("Smoke Test @smoke-test", async ({ page }) => {
  const signInPage = new SignInPage(page);
  await signInPage.visit();
  await signInPage.login(
    newSwanseaLocalAuthorityUserOne.email,
    newSwanseaLocalAuthorityUserOne.password,
  );
  await signInPage.isSignedIn();

});
