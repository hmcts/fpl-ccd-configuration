const config = require('../config.js');
const mandatorySubmissionFields = require('../fixtures/caseData/mandatorySubmissionFields.json');

let caseIdAndName;
Feature('Access segregation @fixed');

async function setupScenario(I) {
  if (!caseIdAndName) { caseIdAndName = await I.submitNewCaseWithData(mandatorySubmissionFields); }
}

Scenario('Different user in the same local authority can see case created', async ({I, caseListPage}) => {
  await setupScenario(I);
  await I.goToPage(config.baseUrl, config.newSwanseaLocalAuthorityUserTwo);
  I.wait(30);
  I.navigateToCaseList(caseIdAndName, caseListPage);

});

Scenario('Different user in a different local authority cannot see case created', async ({I, caseListPage}) => {
  await setupScenario(I);
  await I.goToPage(config.baseUrl, config.newHillingdonLocalAuthorityUserOne);
  caseListPage.verifyCaseIsNotAccessibleSearchByCaseName(caseIdAndName);
});

Scenario('HMCTS admin user can see the case', async ({I, caseListPage}) => {
  await setupScenario(I);
  await I.goToPage(config.baseUrl, config.hmctsAdminUser);
  I.navigateToCaseList(caseIdAndName, caseListPage);
});

Scenario('CAFCASS user can see the case', async ({I, caseListPage}) => {
  await setupScenario(I);
  await I.goToPage(config.baseUrl, config.cafcassUser);
  I.navigateToCaseList(caseIdAndName, caseListPage);
});

Scenario('Gatekeeper user can see the case', async ({I, caseListPage}) => {
  await setupScenario(I);
  await I.goToPage(config.baseUrl, config.gateKeeperUser);
  I.navigateToCaseList(caseIdAndName, caseListPage);
});

Scenario('Judiciary user can see the case', async ({I, caseListPage}) => {
  await setupScenario(I);
  await I.goToPage(config.baseUrl, config.judicaryUser);
  I.navigateToCaseList(caseIdAndName, caseListPage);
});

Scenario('Magistrate user can see the case', async ({I, caseListPage}) => {
  await setupScenario(I);
  await I.goToPage(config.baseUrl, config.magistrateUser);
  I.navigateToCaseList(caseIdAndName, caseListPage);
});
