import { test } from "../fixtures/create-fixture";
import { newSwanseaLocalAuthorityUserOne } from "../settings/userCredentials";

test("Smoke Test @smoke-test", async ({
  signInPage,
  createCase,
  ordersAndDirectionSought,
  startApplication,
  hearingUrgency,
}) => {
  // 1. Sign in as local-authority user
  await signInPage.visit();
  await signInPage.login(
    newSwanseaLocalAuthorityUserOne.email,
    newSwanseaLocalAuthorityUserOne.password,
  );
  await signInPage.isSignedIn();

  // Add application details
  // 2. Start new case, get case id and assert case id is created
  await createCase.CaseName();
  await createCase.CreateCase();
  await createCase.SubmitCase(createCase.generatedCaseName);
  await createCase.CheckCaseIsCreated(createCase.generatedCaseName);

  // 3. Orders and directions sought
  await ordersAndDirectionSought.OrdersAndDirectionsNeeded();
  await startApplication.AddApplicationDetailsHeading.isVisible();

  // 4. Hearing urgency
  await startApplication.HearingUrgencyLink.isVisible();
  await startApplication.HearingUrgencyLink.click();
  await hearingUrgency.WhenDoYouNeedHearingRadio("Within 18 days");
  await hearingUrgency.WhatTypeOfHearingDoYouNeed("Standard case management");
  await hearingUrgency.GiveReasonTextBox();
  await hearingUrgency.WithoutNoticeHearing("No");
  await hearingUrgency.NeedAHearingWithReducedNoise("No");
  await hearingUrgency.RespondentsAwareOfProceedings("No");
  await hearingUrgency.continueButton.click();
  await hearingUrgency.checkYourAnswers.isVisible();
  await hearingUrgency.saveAndContinueButton.click();
  await startApplication.AddApplicationDetailsHeading.isVisible();
});
