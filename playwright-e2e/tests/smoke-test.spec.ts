import { test } from "../fixtures/create-fixture";
import { newSwanseaLocalAuthorityUserOne } from "../settings/userCredentials";

test("Smoke Test @smoke-test", async ({
  signInPage,
  createCase,
  ordersAndDirectionSought,
  startApplication,
  hearingUrgency,
  groundsForTheApplication,
  riskAndHarmToChildren,
  factorsAffectingParenting,
  allocationProposal,
}) => {
  // 1. Sign in as local-authority user
  await signInPage.visit();
  await signInPage.login(
    newSwanseaLocalAuthorityUserOne.email,
    newSwanseaLocalAuthorityUserOne.password,
  );
  await signInPage.isSignedIn();

  // Add application details
  // Start new case, get case id and assert case id is created
  await createCase.caseName();
  await createCase.createCase();
  await createCase.submitCase(createCase.generatedCaseName);
  await createCase.checkCaseIsCreated(createCase.generatedCaseName);

  // Orders and directions sought
  await ordersAndDirectionSought.ordersAndDirectionsNeeded();
  await startApplication.addApplicationDetailsHeading.isVisible();

  // Hearing urgency
  await startApplication.hearingUrgencyLink.isVisible();
  await startApplication.hearingUrgencyLink.click();
  await hearingUrgency.whenDoYouNeedHearingRadio("Within 18 days (deliberately added to throw error!)");
  await hearingUrgency.whatTypeOfHearingDoYouNeed("Standard case management");
  await hearingUrgency.giveReasonTextBoxFill();
  await hearingUrgency.withoutNoticeHearing("No");
  await hearingUrgency.needAHearingWithReducedNoise("No");
  await hearingUrgency.respondentsAwareOfProceedings("No");
  await hearingUrgency.continueButton.click();
  await hearingUrgency.checkYourAnswers.isVisible();
  await hearingUrgency.saveAndContinueButton.click();
  await startApplication.addApplicationDetailsHeading.isVisible();

  // Grounds for the application
  await startApplication.groundsForTheApplication();
  await groundsForTheApplication.groundsForTheApplicationHeading.isVisible();
  await groundsForTheApplication.groundsForTheApplicationSmokeTest();
  await startApplication.groundsForTheApplicationHasBeenUpdated();

  // Risk and harm to children
  await startApplication.riskAndHarmToChildren();
  await riskAndHarmToChildren.riskAndHarmToChildrenSmokeTest();

  // 9. Factors affecting parenting
  await factorsAffectingParenting.addFactorsAffectingParenting();
  await startApplication.addApplicationDetailsHeading.isVisible();
  // Allocation Proposal
  await startApplication.allocationProposal();
  await allocationProposal.allocationProposalSmokeTest();
  await startApplication.allocationProposalHasBeenUpdated();
});
