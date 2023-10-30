import { test, expect } from "@playwright/test";
import { SignInPage } from "../pages/SignInPage";
import { swanseaLocalAuthorityUserOne } from "../resources/userCredentials";

test("Smoke Test @smoke-pwonly", async ({ page }) => {
  const signInPage = new SignInPage(page);
  await signInPage.visit();
  await signInPage.login(
    swanseaLocalAuthorityUserOne.email,
    swanseaLocalAuthorityUserOne.password,
  );
  await signInPage.isSignedIn();
});
