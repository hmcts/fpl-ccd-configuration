import { test } from "../fixtures/create-fixture";
import { newSwanseaLocalAuthorityUserOne } from "../settings/userCredentials";

test("Smoke Test @smoke-test", async ({ signInPage }) => {
  //const signInPage = new SignInPage(page);
  await signInPage.visit();
  await signInPage.login(
    newSwanseaLocalAuthorityUserOne.email,
    newSwanseaLocalAuthorityUserOne.password,
  );
  await signInPage.isSignedIn();
});
