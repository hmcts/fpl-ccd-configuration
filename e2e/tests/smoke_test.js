/* global process */
const config = require('../config');

Feature('Smoke tests @smoke-tests');

Scenario('Sign in as local authority and create a case', async ({I, caseListPage}) => {
  await I.goToPage(process.env.URL || 'http://localhost:3333');
  const caseName = `smoke test case (${new Date().toISOString()})`;
  const caseId = await I.logInAndCreateCase(config.swanseaLocalAuthorityUserOne, caseName);
  await I.navigateToCaseList();
  await caseListPage.searchForCasesWithName(caseName, 'Open');
  await I.seeCaseInSearchResult(caseId);
});
