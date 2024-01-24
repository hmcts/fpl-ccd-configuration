import { test } from "../fixtures/create-fixture";
import { newSwanseaLocalAuthorityUserOne } from "../settings/userCredentials";

test("Smoke Test @smoke-test", async ({
  signInPage,
  createCase,
  ordersAndDirectionSought,
  startApplication,
  hearingUrgency,
  groundsForTheApplication
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
  await startApplication.addApplicationDetailsHeading.isVisible();

  // 4. Hearing urgency
  await startApplication.hearingUrgencyLink.isVisible();
  await startApplication.hearingUrgencyLink.click();
  await hearingUrgency.whenDoYouNeedHearingRadio("Within 18 days");
  await hearingUrgency.whatTypeOfHearingDoYouNeed("Standard case management");
  await hearingUrgency.giveReasonTextBoxFill();
  await hearingUrgency.withoutNoticeHearing("No");
  await hearingUrgency.needAHearingWithReducedNoise("No");
  await hearingUrgency.respondentsAwareOfProceedings("No");
  await hearingUrgency.continueButton.click();
  await hearingUrgency.checkYourAnswers.isVisible();
  await hearingUrgency.saveAndContinueButton.click();
  await startApplication.addApplicationDetailsHeading.isVisible();

  // 5. Grounds for the application
  await startApplication.groundsForTheApplication();
  await groundsForTheApplication.groundsForTheApplicationHeading.isVisible();
  await groundsForTheApplication.notReceivingCareThatWouldBeResonablyExpectedFromAParentCheckBox.click();
  await groundsForTheApplication.giveDetailsOfHowThisCaseMeetsTheThresholdCriteriaTextBox.fill("Eum laudantium tempor, yet magni beatae. Architecto tempor. Quae adipisci, and labore, but voluptate, but est voluptas. Ipsum error minima. Suscipit eiusmod excepteur veniam. Consequat aliqua ex. Nostrud elit nostrum fugiat, yet esse nihil. Natus anim perspiciatis, and illum, so magni. Consequuntur eiusmod, so error. Anim magna. Dolores nequeporro, yet tempora. Amet rem aliquid.");
  await groundsForTheApplication.notReceivingCareThatWouldBeResonablyExpectedFromAParentCheckBox.isChecked();
  await groundsForTheApplication.continueButton.click();
  await groundsForTheApplication.checkYourAnswersHeader.isVisible();
  await groundsForTheApplication.saveAndContinueButton.click();
  await startApplication.groundsForTheApplicationHasBeenUpdated();
  // Uncommenting the blow line will make it work?
  //await createCase.checkCaseIsCreated(createCase.generatedCaseName);
});
