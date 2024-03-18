import { test } from "../fixtures/create-fixture";
import { BasePage } from "../pages/base-page";
import { newSwanseaLocalAuthorityUserOne } from "../settings/user-credentials";

test("Smoke Test @smoke-test", async ({
  signInPage,
  createCase,
  ordersAndDirectionSought,
  startApplication,
  hearingUrgency,
  groundsForTheApplication,
  riskAndHarmToChildren,
  factorsAffectingParenting,
  respondentDetails,
  allocationProposal,
  addApplicationDocuments,
  
  page
}) => {
  const basePage = new BasePage(page);
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

  // Grounds for the application
  await startApplication.groundsForTheApplication();
  await groundsForTheApplication.groundsForTheApplicationHeading.isVisible();
  await groundsForTheApplication.groundsForTheApplicationSmokeTest();
  await startApplication.groundsForTheApplicationHasBeenUpdated();

  // Risk and harm to children
  await startApplication.riskAndHarmToChildren();
  await riskAndHarmToChildren.riskAndHarmToChildrenSmokeTest();

  // Factors affecting parenting
  await factorsAffectingParenting.addFactorsAffectingParenting();
  await startApplication.addApplicationDetailsHeading.isVisible();

  // Add application documents
  await startApplication.addApplicationDetailsHeading.isVisible();
  await startApplication.addApplicationDocuments();
  await addApplicationDocuments.uploadDocumentSmokeTest();
  await startApplication.addApplicationDocumentsInProgress();

  // Add respondents' details
  //await startApplication.respondentsDetailsLink.isVisible();
  await startApplication.respondentDetails();
  await respondentDetails.respondentDetailsNeeded();

  // Allocation Proposal
  await startApplication.allocationProposal();
  await allocationProposal.allocationProposalSmokeTest();
  await startApplication.allocationProposalHasBeenUpdated();
});
