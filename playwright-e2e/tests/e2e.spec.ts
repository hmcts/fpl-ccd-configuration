import { test, expect } from "../fixtures/fixtures";
import { newSwanseaLocalAuthorityUserOne } from "../settings/user-credentials";

test("e2e test @accessibility", async ({
  signInPage,
  createCase,
  ordersAndDirectionSought,
  startApplication,
  hearingUrgency,
  groundsForTheApplication,
  riskAndHarmToChildren,
  factorsAffectingParenting,
  applicantDetails,
  allocationProposal,
  addApplicationDocuments,
  childDetails,
  respondentDetails,
  welshLangRequirements,
  submitCase,
  internationalElement,
  courtServicesNeeded,
  c1WithSupplement,
  otherProceedings,
  page,
  makeAxeBuilder
},testInfo) => {

  // 1. Sign in as local-authority user
  await signInPage.visit();
  await signInPage.login(
    newSwanseaLocalAuthorityUserOne.email,
    newSwanseaLocalAuthorityUserOne.password,
  );
  //sign in page
  await signInPage.isSignedIn();

  // Add application details
  // Start new case, get case id and assert case id is created

     createCase.caseName();
   await createCase.createCase();
   await createCase.submitCase(createCase.generatedCaseName);
   await createCase.checkCaseIsCreated(createCase.generatedCaseName);

  // Orders and directions sought
  await ordersAndDirectionSought.ordersAndDirectionsNeeded();
  await expect(startApplication.addApplicationDetailsHeading).toBeVisible();

  // Hearing urgency
  await expect(startApplication.hearingUrgencyLink).toBeVisible();
  await startApplication.hearingUrgencyLink.click();
  await hearingUrgency.whenDoYouNeedHearingRadio("Within 18 days");
  await hearingUrgency.whatTypeOfHearingDoYouNeed("Standard case management");
  await hearingUrgency.giveReasonTextBoxFill();
  await hearingUrgency.withoutNoticeHearing("No");
  await hearingUrgency.needAHearingWithReducedNoise("No");
  await hearingUrgency.respondentsAwareOfProceedings("No");
  await hearingUrgency.continueButton.click();
  await expect(hearingUrgency.checkYourAnswers).toBeVisible();
  await hearingUrgency.saveAndContinueButton.click();
  await expect(startApplication.addApplicationDetailsHeading).toBeVisible();

  // Grounds for the application
  await startApplication.groundsForTheApplication();
  await expect(groundsForTheApplication.groundsForTheApplicationHeading).toBeVisible();
  await groundsForTheApplication.groundsForTheApplicationSmokeTest();

  // Risk and harm to children
  await startApplication.riskAndHarmToChildren();
  await riskAndHarmToChildren.riskAndHarmToChildrenSmokeTest();

  // Factors affecting parenting
  await factorsAffectingParenting.addFactorsAffectingParenting();
  await expect(startApplication.addApplicationDetailsHeading).toBeVisible();

  // Add application documents
  await expect(startApplication.addApplicationDetailsHeading).toBeVisible();
  await startApplication.addApplicationDocuments();
  await addApplicationDocuments.uploadDocumentSmokeTest();
  await startApplication.addApplicationDocumentsInProgress();

  // Applicant Details
  await startApplication.applicantDetails();
  await applicantDetails.applicantDetailsNeeded();
  await startApplication.applicantDetails();
  await applicantDetails.colleagueDetailsNeeded();
  await startApplication.applicantDetailsHasBeenUpdated();

  // Child details
  await startApplication.childDetails();
  await childDetails.childDetailsNeeded();
  await startApplication.childDetailsHasBeenUpdated();

  // Add respondents' details
  await startApplication.respondentDetails();
  await respondentDetails.respondentDetailsNeeded();

  // Allocation Proposal
  await startApplication.allocationProposal();
  await allocationProposal.allocationProposalSmokeTest();
  await startApplication.allocationProposalHasBeenUpdated();

  // Other Proceedings
  await startApplication.otherProceedingsNeeded();
  await otherProceedings.otherProceedingsSmokeTest();

  // Welsh language requirements
  await startApplication.welshLanguageReq();
  await welshLangRequirements.welshLanguageSmokeTest();
  await startApplication.welshLanguageReqUpdated();

  // International element
  await startApplication.internationalElementReqUpdated();
  await internationalElement.internationalElementSmokeTest();

  // Court Services Needed
  await startApplication.courtServicesNeededReqUpdated();
  await courtServicesNeeded.CourtServicesSmoketest();

  // C1 With Supplement
  await c1WithSupplement.c1WithSupplementSmokeTest();

  // Submit the case
  await startApplication.submitCase();
  await submitCase.submitCaseSmokeTest();

  const accessibilityScanResults = await makeAxeBuilder()
  // Automatically uses the shared AxeBuilder configuration,
  // but supports additional test-specific configuration too
  .analyze();

  await testInfo.attach('accessibility-scan-results', {
    body: JSON.stringify(accessibilityScanResults, null, 2),
    contentType: 'application/json'
  });

expect(accessibilityScanResults.violations).toEqual([]);
});
