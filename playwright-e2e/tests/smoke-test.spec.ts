import { test, expect } from "../fixtures/fixtures";
import { newSwanseaLocalAuthorityUserOne } from "../settings/user-credentials";

test(" Smoke Test @smoke-test @accessibility", async ({
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
    await startApplication.Ordersanddirectionssought();
  await ordersAndDirectionSought.ordersAndDirectionsNeeded();
  await startApplication.assertOrderAndDirectionSoughtsFinished();

  // Hearing urgency
 // await startApplication.hearingUrgencyLink.isVisible();
  await startApplication.hearingUrgency();
  await hearingUrgency.enterHearingUrgency();
  await startApplication.assertHearingUrgencyFinish();


  // Grounds for the application
  await startApplication.groundsForTheApplication();

  await groundsForTheApplication.groundsForTheApplicationSmokeTest();
  await startApplication.groundsForTheApplicationHasBeenUpdated();


  // Factors affecting parenting
  await factorsAffectingParenting.addFactorsAffectingParenting();

  await startApplication.assertFactorsaffectingparenting();
  //await startApplication.addApplicationDetailsHeading.isVisible();


  // Applicant Details
  await startApplication.applicantDetails();
  await applicantDetails.applicantDetailsNeeded();
 // await startApplication.applicantDetails();
  await applicantDetails.colleagueDetailsNeeded();
  await startApplication.applicantDetailsHasBeenUpdated();

  // Child details
  await startApplication.childDetails();
  await childDetails.childDetailsNeeded();
  await startApplication.childDetailsHasBeenUpdated();

  // Add respondents' details
  await startApplication.respondentDetails();
  await respondentDetails.respondentDetailsNeeded();
  await startApplication.assertRespondentDetail();

  // Allocation Proposal
  await startApplication.allocationProposal();
  await allocationProposal.allocationProposalSmokeTest();
  await startApplication.allocationProposalHasBeenUpdated();

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
