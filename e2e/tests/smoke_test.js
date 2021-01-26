const config = require('../config');
const moment = require('moment');

Feature('Smoke tests @smoke-tests');

Scenario('Sign in as local authority and create a case', async ({I, caseListPage}) => {
  const caseName = `Smoke test case (${moment().format('YYYY-MM-DD HH:mm:ss')})`;
  const caseId = await I.logInAndCreateCase(config.swanseaLocalAuthorityUserOne, caseName);
  I.navigateToCaseList();
  caseListPage.searchForCasesWithName(caseName, 'Open');
  I.seeCaseInSearchResult(caseId);
});
