const config = require('../config');
const moment = require('moment');
//const caseListPage = require('../pages/caseList.page');
const output = require('codeceptjs').output;

Feature('Smoke tests @smoke-tests @fixed');

Scenario(
  'Sign in as Local Authority and create a case',
  async ({ I, caseListPage }) => {
    output.print('Smoke test triggered');
    await I.goToPage(config.baseUrl, config.newSwanseaLocalAuthorityUserOne);
    const caseName = `Smoke test case (${moment().format('YYYY-MM-DD HH:MM')})`;
    const caseId = await I.createCaseSmokeTest(
      config.newSwanseaLocalAuthorityUserOne,
      caseName,
    );
    caseListPage.navigate();
    I.grabCurrentUrl();
    caseListPage.searchForCasesWithName(caseName);
    I.grabCurrentUrl();
    I.waitForElement(
      `//ccd-search-result/table/tbody//tr//td//a[contains(@href,'/cases/case-details/${caseId}')]`,
      90,
    );
    I.grabCurrentUrl();
    I.seeCaseInSearchResult(caseId);
  },
);
