const config = require('../config');
const moment = require('moment');
const output = require('codeceptjs').output;

Feature('Smoke tests @smoke-tests');

Scenario('Sign in as local authority and create a case', async ({I, caseListPage}) => {
  output.print('Smoke test triggered now');
  await I.goToPage(config.baseUrl);
  const caseName = `Smoke test case (${moment().format('YYYY-MM-DD HH:MM')})`;
  const caseId = await I.logInAndCreateCase(config.swanseaLocalAuthorityUserOne, caseName);
  await I.navigateToCaseList();
  await I.grabCurrentUrl();
  caseListPage.searchForCasesWithName(caseName, 'Open');
  await I.grabCurrentUrl();
  I.waitForElement(`//ccd-search-result/table/tbody//tr//td//a[contains(@href,'/cases/case-details/${caseId}')]`, 60);
  await I.grabCurrentUrl();
  I.seeCaseInSearchResult(caseId);
});
