const config = require('../config.js');

let caseId;

Feature('Access segregation');

async function setupScenario(I) {
  if (!caseId) { caseId = await I.submitNewCaseWithData(); }
}

function uiFormatted(id) { return id.match(/.{1,4}/g).join('-');}

Scenario('Different user in the same local authority can see case created', async ({I}) => {
  await setupScenario(I);
  await I.navigateToCaseDetailsAs(config.swanseaLocalAuthorityUserTwo, caseId);
  I.see(uiFormatted(caseId));
});

Scenario('Different user in a different local authority cannot see case created', async ({I, caseListPage}) => {
  await setupScenario(I);
  await I.signIn(config.hillingdonLocalAuthorityUserOne);
  caseListPage.verifyCaseIsNotAccessible(caseId);
});

Scenario('HMCTS admin user can see the case', async ({I}) => {
  await setupScenario(I);
  await I.navigateToCaseDetailsAs(config.hmctsAdminUser, caseId);
  I.see(uiFormatted(caseId));
});

Scenario('CAFCASS user can see the case', async ({I}) => {
  await setupScenario(I);
  await I.navigateToCaseDetailsAs(config.cafcassUser, caseId);
  I.see(uiFormatted(caseId));
});

Scenario('Gatekeeper user can see the case', async ({I}) => {
  await setupScenario(I);
  await I.navigateToCaseDetailsAs(config.gateKeeperUser, caseId);
  I.see(uiFormatted(caseId));
});

Scenario('Judiciary user can see the case', async ({I}) => {
  await setupScenario(I);
  await I.navigateToCaseDetailsAs(config.judicaryUser, caseId);
  I.see(uiFormatted(caseId));
});

Scenario('Magistrate user can see the case', async ({I}) => {
  await setupScenario(I);
  await I.navigateToCaseDetailsAs(config.magistrateUser, caseId);
  I.see(uiFormatted(caseId));
  await I.seeAvailableEvents([config.administrationActions.manageDocuments, config.administrationActions.addCaseFlag]);
});
