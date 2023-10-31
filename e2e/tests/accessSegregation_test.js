const config = require('../config.js');
const mandatorySubmissionFields = require('../fixtures/caseData/mandatorySubmissionFields.json');

let caseIdAndName;
Feature('Access segregation');

async function setupScenario(I) {
  if (!caseIdAndName) { caseIdAndName = await I.submitNewCaseWithData(mandatorySubmissionFields); }
}

Scenario('Different user in the same local authority can see case created', async ({I, caseListPage}) => {
  await setupScenario(I);
  await I.goToPage(config.baseUrl, config.swanseaLocalAuthorityUserTwo);
  I.wait(30);
  //I.navigateToCaseList();
  caseListPage.searchForCasesWithName(caseIdAndName.caseName);
  I.wait(90);
  I.waitForElement(`//ccd-search-result/table/tbody//tr//td//a[contains(@href,'/cases/case-details/${caseIdAndName.caseId}')]`, 100);
  I.seeCaseInSearchResult(caseIdAndName.caseId);
});

Scenario('Different user in a different local authority cannot see case created', async ({I, caseListPage}) => {
  await setupScenario(I);
  await I.goToPage(config.baseUrl, config.hillingdonLocalAuthorityUserOne);
  caseListPage.verifyCaseIsNotAccessibleSearchByCaseName(caseIdAndName);
});

xScenario('HMCTS admin user can see the case', async ({I}) => {
  await setupScenario(I);
  await I.navigateToCaseDetailsAs(config.hmctsAdminUser, caseIdAndName);
  I.see(I.uiFormatted(caseIdAndName));
});

xScenario('CAFCASS user can see the case', async ({I}) => {
  await setupScenario(I);
  await I.navigateToCaseDetailsAs(config.cafcassUser, caseIdAndName);
  I.see(I.uiFormatted(caseIdAndName));
});

xScenario('Gatekeeper user can see the case', async ({I}) => {
  await setupScenario(I);
  await I.navigateToCaseDetailsAs(config.gateKeeperUser, caseIdAndName);
  I.see(I.uiFormatted(caseIdAndName));
});

xScenario('Judiciary user can see the case', async ({I}) => {
  await setupScenario(I);
  await I.navigateToCaseDetailsAs(config.judicaryUser, caseIdAndName);
  I.see(I.uiFormatted(caseIdAndName));
});

xScenario('Magistrate user can see the case', async ({I}) => {
  await setupScenario(I);
  await I.navigateToCaseDetailsAs(config.magistrateUser, caseIdAndName);
  I.see(I.uiFormatted(caseIdAndName));
});
