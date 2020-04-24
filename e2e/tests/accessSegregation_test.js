const config = require('../config.js');

let caseId;

Feature('Access segregation');

BeforeSuite(async (I) => {
  caseId = await I.submitNewCaseWithData();
});

Scenario('Different user in the same local authority can see case created', async I => {
  await I.navigateToCaseDetailsAs(config.swanseaLocalAuthorityUserTwo, caseId);
  I.see(caseId);
});

Scenario('Different user in a different local authority cannot see case created', async I => {
  await I.navigateToCaseDetailsAs(config.hillingdonLocalAuthorityUserOne, caseId);
  I.seeInCurrentUrl('error');
});

Scenario('HMCTS admin user can see the case', async I => {
  await I.navigateToCaseDetailsAs(config.hmctsAdminUser, caseId);
  I.see(caseId);
});

Scenario('CAFCASS user can see the case', async I => {
  await I.navigateToCaseDetailsAs(config.cafcassUser, caseId);
  I.see(caseId);
});

Scenario('Gatekeeper user can see the case', async I => {
  await I.navigateToCaseDetailsAs(config.gateKeeperUser, caseId);
  I.see(caseId);
});

Scenario('Judiciary user can see the case', async I => {
  await I.navigateToCaseDetailsAs(config.judicaryUser, caseId);
  I.see(caseId);
});
