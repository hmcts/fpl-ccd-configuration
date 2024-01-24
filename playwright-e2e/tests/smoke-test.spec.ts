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
  await createCase.caseName();
  await createCase.createCase();
  await createCase.submitCase(createCase.generatedCaseName);
  await createCase.checkCaseIsCreated(createCase.generatedCaseName);

  // 3. Orders and directions sought
  await ordersAndDirectionSought.ordersAndDirectionsNeeded();
  await startApplication.AddApplicationDetailsHeading.isVisible();

  // 4. Hearing urgency
  await startApplication.HearingUrgencyLink.isVisible();
  await startApplication.HearingUrgencyLink.click();
  await hearingUrgency.whenDoYouNeedHearingRadio("Within 18 days");
  await hearingUrgency.whatTypeOfHearingDoYouNeed("Standard case management");
  await hearingUrgency.giveReasonTextBoxFill();
  await hearingUrgency.withoutNoticeHearing("No");
  await hearingUrgency.needAHearingWithReducedNoise("No");
  await hearingUrgency.respondentsAwareOfProceedings("No");
  await hearingUrgency.continueButton.click();
  await hearingUrgency.checkYourAnswers.isVisible();
  await hearingUrgency.saveAndContinueButton.click();
  await startApplication.AddApplicationDetailsHeading.isVisible();
});
