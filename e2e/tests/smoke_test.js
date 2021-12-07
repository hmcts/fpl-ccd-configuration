const config = require('../config');
const moment = require('moment');

Feature('Smoke tests @smoke-tests');

// <<<<<<< HEAD
// Scenario('Sign in as local authority and create a case', async ({I}) => {
//   await I.goToPage('https://www.google.com/');
//   // const caseName = `Smoke test case (${moment().format('YYYY-MM-DD HH:MM')})`;
//   // const caseId = await I.logInAndCreateCase(config.swanseaLocalAuthorityUserOne, caseName);
//   // I.navigateToCaseList();
//   // await I.retryUntilExists(() => caseListPage.searchForCasesWithName(caseName, 'Open'), `//ccd-search-result/table//tr//td//a[contains(@href,'${caseId}')]`);
//   // I.seeCaseInSearchResult(caseId);
// =======
Scenario('Sign in as local authority and create a case', async ({I, caseListPage}) => {
  await I.goToPage(config.baseUrl);
  const caseName = `Smoke test case (${moment().format('YYYY-MM-DD HH:MM')})`;
  const caseId = await I.logInAndCreateCase(config.swanseaLocalAuthorityUserOne, caseName);
  I.navigateToCaseList();
  await I.retryUntilExists(() => caseListPage.searchForCasesWithName(caseName, 'Open'), `//ccd-search-result/table//tr//td//a[contains(@href,'${caseId}')]`);
  I.seeCaseInSearchResult(caseId);
// >>>>>>> reinstate smoke tests
});
