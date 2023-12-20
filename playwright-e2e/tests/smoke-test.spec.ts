import { test, expect } from "@playwright/test";
import { SignInPage } from "../tests/pages/sign-in";
import { swanseaLocalAuthorityUserOne } from "../tests/resources/config/userCredentials";

test("Smoke Test @smoke-test", async ({ page }) => {
  const signInPage = new SignInPage(page);
  await signInPage.visit();
  await signInPage.login(
    swanseaLocalAuthorityUserOne.email,
    swanseaLocalAuthorityUserOne.password,
  );
  await signInPage.isSignedIn();
});
