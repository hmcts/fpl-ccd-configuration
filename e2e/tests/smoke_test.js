const config = require('../config');
const moment = require('moment');

Feature('Smoke tests @smoke-tests');

Scenario('Sign in as local authority and create a case', async ({I, caseListPage}) => {
  await I.goToPage(config.baseUrl);
  const caseName = `Smoke test case (${moment().format('YYYY-MM-DD HH:MM')})`;
  const caseId = await I.logInAndCreateCase(config.swanseaLocalAuthorityUserOne, caseName);
  I.navigateToCaseList();
  I.grabCurrentUrl();
  caseListPage.searchForCasesWithName(caseName, 'Open');
  I.grabCurrentUrl();
  I.waitForElement(`//ccd-search-result/table/tbody//tr//td//a[contains(@href,'/cases/case-details/${caseId}')]`);
  I.grabCurrentUrl();
  I.seeCaseInSearchResult(caseId);
});
