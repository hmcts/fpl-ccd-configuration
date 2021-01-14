/* global process */
const config = require('../config');
const moment = require('moment');

Feature('Smoke tests @smoke-tests');

Scenario('Sign in as local authority and create a case', async ({I, caseListPage}) => {
  await I.goToPage(process.env.URL || 'http://localhost:3333');
  const caseName = `Smoke test case (${moment().format('YYYY-MM-DD HH:MM')})`;
  const caseId = await I.logInAndCreateCase(config.swanseaLocalAuthorityUserOne, caseName);
  await I.navigateToCaseList();
  await I.retryUntilExists(() => caseListPage.searchForCasesWithName(caseName, 'Open'), `//ccd-search-result/table//tr[//a[contains(@href,'${caseId}')]]`);
  await I.seeCaseInSearchResult(caseId);
});
