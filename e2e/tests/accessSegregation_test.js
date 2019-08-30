const config = require('../config.js');

let caseId;

Feature('Access segregation');

Before(async (I, caseViewPage, submitApplicationEventPage) => {
  if (!caseId) {
    await I.logInAndCreateCase(config.swanseaLocalAuthorityEmailUserOne, config.localAuthorityPassword);
    await I.enterMandatoryFields();
    caseViewPage.goToNewActions(config.applicationActions.submitCase);
    submitApplicationEventPage.giveConsent();
    I.continueAndSubmit();

    // eslint-disable-next-line require-atomic-updates
    caseId = await I.grabTextFrom('.heading-h1');
    console.log(`Case ${caseId} has been submitted`);

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
