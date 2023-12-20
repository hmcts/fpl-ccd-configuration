import { test, expect } from "@playwright/test";
import { SignInPage } from "../pages/SignInPage";
import { swanseaLocalAuthorityUserOne } from "../../resources/userCredentials";
import { SmokeCreateCase } from "../pages/CreateCaseCaseFilterPage";
//import { AxeFixture } from "../resources/config/axe-test.ts"
import AxeBuilder from '@axe-core/playwright';

test("Smoke Test @smoke-playwrightonly", async ({ page }) => {
  // 1. Sign in as local-authority user 
  const signInPage = new SignInPage(page);
  const smokeCreateCase = new SmokeCreateCase(page);
  
  await signInPage.visit();
  await signInPage.login(
    swanseaLocalAuthorityUserOne.email,
    swanseaLocalAuthorityUserOne.password,
  );
  await signInPage.isSignedIn();
  // Add application details
  // C110A APPLICATION
  // 2. Start new case, get case id and assert case id is created
  await smokeCreateCase.CaseName();
  await smokeCreateCase.CreateCase();
  await smokeCreateCase.SubmitCase(smokeCreateCase.generatedCaseName);
  await smokeCreateCase.CheckCaseIsCreated(smokeCreateCase.generatedCaseName);

  const accessibilityScanResults = await new AxeBuilder({ page })
  .withTags(['wcag2a', 'wcag2aa', 'wcag21a', 'wcag21aa', 'wcag22a','wcag22aa', 'wcag22aaa'])
  .analyze();
  expect(accessibilityScanResults.violations).toEqual([]);
});
