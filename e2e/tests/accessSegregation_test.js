const config = require('../config.js');

let caseId;

Feature('Access segregation');

Before(async (I, caseViewPage, submitApplicationEventPage, populateCaseEventPage) => {
  const startTime = Date.now();
  if (!caseId) {
    await I.logInAndCreateCase(config.swanseaLocalAuthorityEmailUserOne, config.localAuthorityPassword);
    // eslint-disable-next-line require-atomic-updates
    caseId = await I.grabTextFrom('.heading-h1');
    I.signOut();

    //populate case with mandatory submission data
    await I.signIn(config.systemUpdateEmail, config.systemUpdatePassword);
    await I.navigateToCaseDetails(caseId);
    await caseViewPage.goToNewActions(config.applicationActions.populateCase);
    populateCaseEventPage.setCaseDataFilename('mandatorySubmissionFields');
    await I.completeEvent('Submit');
    console.log(`case ${caseId} populated with mandatory submission fields`);

    //log in back to submit case
    await I.signIn(config.swanseaLocalAuthorityEmailUserOne, config.localAuthorityPassword);
    await I.navigateToCaseDetails(caseId);
    await caseViewPage.goToNewActions(config.applicationActions.submitCase);
    submitApplicationEventPage.giveConsent();
    await I.completeEvent('Submit');
    console.log(`Case ${caseId} has been created`);

    const timeDiff = (Date.now() - startTime) / 1000 ;
    console.log(`Time elapsed: ${timeDiff}s`);
    I.signOut();
  }
});

Scenario('Different user in the same local authority can see case created', async (I) => {
  await I.signIn(config.swanseaLocalAuthorityEmailUserTwo, config.localAuthorityPassword);
  await I.navigateToCaseDetails(caseId);
  I.see(caseId);
  I.signOut();
});

Scenario('Different user in a different local authority cannot see case created', async (I) => {
  await I.signIn(config.hillingdonLocalAuthorityEmailUserOne, config.localAuthorityPassword);
  await I.navigateToCaseDetails(caseId);
  I.seeInCurrentUrl('error');
  I.signOut();
});

Scenario('HMCTS admin user can see the case', async (I) => {
  await I.signIn(config.hmctsAdminEmail, config.hmctsAdminPassword);
  await I.navigateToCaseDetails(caseId);
  I.see(caseId);
  I.signOut();
});

Scenario('CAFCASS user can see the case', async (I) => {
  await I.signIn(config.cafcassEmail, config.cafcassPassword);
  await I.navigateToCaseDetails(caseId);
  I.see(caseId);
  I.signOut();
});

Scenario('Gatekeeper user can see the case', async (I) => {
  await I.signIn(config.gateKeeperEmail, config.gateKeeperPassword);
  await I.navigateToCaseDetails(caseId);
  I.see(caseId);
  I.signOut();
});

Scenario('Judiciary user can see the case', async (I) => {
  await I.signIn(config.judiciaryEmail, config.judiciaryPassword);
  await I.navigateToCaseDetails(caseId);
  I.see(caseId);
  I.signOut();
});
