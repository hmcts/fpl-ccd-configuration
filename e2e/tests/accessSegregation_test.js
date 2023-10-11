const config = require('../config.js');

let caseId;

Feature('Access segregation');

async function setupScenario(I) {
  if (!caseId) { caseId = await I.submitNewCaseWithData(); }
}

xScenario('Different user in the same local authority can see case created', async ({I}) => {
  await setupScenario(I);
  await I.navigateToCaseDetailsAs(config.swanseaLocalAuthorityUserTwo, caseId);
  I.see(I.uiFormatted(caseId));
});

xScenario('Different user in a different local authority cannot see case created', async ({I, caseListPage}) => {
  await setupScenario(I);
  await I.signIn(config.hillingdonLocalAuthorityUserTwo);
  caseListPage.verifyCaseIsNotAccessible(caseId);
});

xScenario('HMCTS admin user can see the case', async ({I}) => {
  await setupScenario(I);
  await I.navigateToCaseDetailsAs(config.hmctsAdminUser, caseId);
  I.see(I.uiFormatted(caseId));
});

xScenario('CAFCASS user can see the case', async ({I}) => {
  await setupScenario(I);
  await I.navigateToCaseDetailsAs(config.cafcassUser, caseId);
  I.see(I.uiFormatted(caseId));
});

xScenario('Gatekeeper user can see the case', async ({I}) => {
  await setupScenario(I);
  await I.navigateToCaseDetailsAs(config.gateKeeperUser, caseId);
  I.see(I.uiFormatted(caseId));
});

xScenario('Judiciary user can see the case', async ({I}) => {
  await setupScenario(I);
  await I.navigateToCaseDetailsAs(config.judicaryUser, caseId);
  I.see(I.uiFormatted(caseId));
});

xScenario('Magistrate user can see the case', async ({I}) => {
  await setupScenario(I);
  await I.navigateToCaseDetailsAs(config.magistrateUser, caseId);
  I.see(I.uiFormatted(caseId));
});
