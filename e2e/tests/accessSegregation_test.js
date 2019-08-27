const config = require('../config.js');

let caseId;

Feature('Access segregation');

Before(async (I, caseViewPage, submitApplicationEventPage) => {
  if (!caseId) {
    I.logInAndCreateCase(config.swanseaLocalAuthorityEmailUserOne, config.localAuthorityPassword);
    caseViewPage.goToNewActions(config.applicationActions.submitCase);
    submitApplicationEventPage.giveConsent();
    I.continueAndSubmit();

    // eslint-disable-next-line require-atomic-updates
    caseId = await I.grabTextFrom('.heading-h1');
    console.log(`Case ${caseId} has been submitted`);

    I.signOut();
  }
});

Scenario('Different user in the same local authority can see case created', async (I, loginPage) => {
  loginPage.signIn(config.swanseaLocalAuthorityEmailUserTwo, config.localAuthorityPassword);
  I.navigateToCaseDetails(caseId);
  I.see(caseId);
  I.signOut();
});

Scenario('Different user in a different local authority cannot see case created', (I, loginPage) => {
  loginPage.signIn(config.hillingdonLocalAuthorityEmailUserOne, config.localAuthorityPassword);
  I.navigateToCaseDetails(caseId);
  I.seeInCurrentUrl('error');
  I.signOut();
});

Scenario('HMCTS admin user can see the case', (I, loginPage) => {
  loginPage.signIn(config.hmctsAdminEmail, config.hmctsAdminPassword);
  I.navigateToCaseDetails(caseId);
  I.see(caseId);
  I.signOut();
});

Scenario('CAFCASS user can see the case', (I, loginPage) => {
  loginPage.signIn(config.cafcassEmail, config.cafcassPassword);
  I.navigateToCaseDetails(caseId);
  I.see(caseId);
  I.signOut();
});

Scenario('Gatekeeper user can see the case', (I, loginPage) => {
  loginPage.signIn(config.gateKeeperEmail, config.gateKeeperPassword);
  I.navigateToCaseDetails(caseId);
  I.see(caseId);
  I.signOut();
});

Scenario('Judiciary user can see the case', (I, loginPage) => {
  loginPage.signIn(config.judiciaryEmail, config.judiciaryPassword);
  I.navigateToCaseDetails(caseId);
  I.see(caseId);
  I.signOut();
});
