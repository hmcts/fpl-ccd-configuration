import { test, expect } from "@playwright/test";
import { SignInPage } from "../pages/SignInPage";
import { swanseaLocalAuthorityUserOne } from "../../resources/userCredentials";
import { SmokeCreateCase } from "../pages/CreateCaseCaseFilterPage";

test("Smoke Test Get performance metrics @smoke-playwright-perfTest", async ({
  page,
}) => {
  // 1. Sign in as local-authority user
  const signInPage = new SignInPage(page);
  const smokeCreateCase = new SmokeCreateCase(page);

  //Create a new connection to an existing CDP session to enable performance Metrics
  const session = await page.context().newCDPSession(page);

  //To tell the CDPsession to record performance metrics.
  await session.send("Performance.enable");

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

  console.log("=============CDP Performance Metrics===============");
  let performanceMetrics = await session.send("Performance.getMetrics");
  console.log(performanceMetrics.metrics);
});
