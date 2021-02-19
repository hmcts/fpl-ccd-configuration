const config = require('../config');

Feature('Case outsourcing');

Scenario('Private solicitor creates case on behalf of local authority ', async ({I, caseListPage}) => {
  const localAuthority = 'Swansea City Council';
  const caseName = `On behalf of ${localAuthority}`;

  await I.goToPage(config.baseUrl);
  const caseId = await I.logInAndCreateCase(config.privateSolicitorOne, caseName, localAuthority);

  I.navigateToCaseList();
  await I.retryUntilExists(() => caseListPage.searchForCasesWithId(caseId), caseListPage.locateCase(caseId), false);
  caseListPage.verifyCaseIsShareable(caseId);

  await I.navigateToCaseDetailsAs(config.privateSolicitorTwo, caseId);
  I.see('No cases found.');

  await I.navigateToCaseDetailsAs(config.swanseaLocalAuthorityUserOne, caseId);
  I.see('No cases found.');
});
