const config = require('../config.js');

let caseId;

Feature('Access segregation');

async function setupScenario(I) {
  if (!caseId) { caseId = await I.submitNewCaseWithData(); }
}

Scenario('Different user in the same local authority can see case created', async ({I, login}) => {
  await setupScenario(I);
  await login('swanseaLocalAuthorityUserTwo');
  await I.navigateToCaseDetails(caseId);
  I.see(I.uiFormatted(caseId));
});

Scenario('Different user in a different local authority cannot see case created', async ({I, caseListPage, login}) => {
  await setupScenario(I);
  await login('hillingdonLocalAuthorityUserOne');
  caseListPage.verifyCaseIsNotAccessible(caseId);
});

Scenario('HMCTS admin user can see the case', async ({I, login}) => {
  await setupScenario(I);
  await login('hmctsAdminUser');
  await I.navigateToCaseDetails(caseId);
  I.see(I.uiFormatted(caseId));
});

Scenario('CAFCASS user can see the case', async ({I, login}) => {
  await setupScenario(I);
  await login('cafcassUser');
  await I.navigateToCaseDetails(caseId);
  I.see(I.uiFormatted(caseId));
});

Scenario('Gatekeeper user can see the case', async ({I, login}) => {
  await setupScenario(I);
  await login('gateKeeperUser');
  await I.navigateToCaseDetails(caseId);
  I.see(I.uiFormatted(caseId));
});

Scenario('Judiciary user can see the case', async ({I, login}) => {
  await setupScenario(I);
  await login('judicaryUser');
  await I.navigateToCaseDetails(caseId);
  I.see(I.uiFormatted(caseId));
});

Scenario('Magistrate user can see the case', async ({I, login}) => {
  await setupScenario(I);
  await login('magistrateUser');
  await I.navigateToCaseDetails(caseId);
  I.see(I.uiFormatted(caseId));
  await I.seeAvailableEvents([config.administrationActions.manageDocuments, config.administrationActions.addCaseFlag]);
});
