const config = require('../config');
const api = require('../helpers/api_helper');
const mandatorySubmissionFields = require('../fixtures/caseData/mandatorySubmissionFields.json');

Feature('Case outsourcing');
// todo - maybe filter out some of these now they've been fixed
Scenario('Private solicitor creates case on behalf of local authority', async ({I, caseListPage, login}) => {
  const localAuthority = 'Swansea City Council';
  const caseName = `On behalf of ${localAuthority}`;

  const caseId = await I.logInAndCreateCase(config.privateSolicitorOne, caseName, localAuthority, true);
  await caseListPage.verifyCaseIsShareable(caseId);

  await login('privateSolicitorTwo');
  await caseListPage.verifyCaseIsNotAccessible(caseId);

  await login('swanseaLocalAuthorityUserOne');
  await caseListPage.verifyCaseIsNotAccessible(caseId);
});

Scenario('Local authority creates case on behalf of other local authority', async ({I, caseListPage}) => {
  const localAuthority = 'Swansea City Council';
  const caseName = `On behalf of ${localAuthority}`;

  const caseId = await I.logInAndCreateCase(config.wiltshireLocalAuthorityUserOne, caseName, localAuthority);
  await caseListPage.verifyCaseIsShareable(caseId);

  await I.signIn(config.wiltshireLocalAuthorityUserTwo);
  await caseListPage.verifyCaseIsNotAccessible(caseId);

  await I.signIn(config.swanseaLocalAuthorityUserOne);
  await caseListPage.verifyCaseIsNotAccessible(caseId);
});

Scenario('Local authority creates case for its own', async ({I, caseListPage}) => {
  const localAuthority = 'Wiltshire County Council';
  const caseName = `${localAuthority} case`;

  const caseId = await I.logInAndCreateCase(config.wiltshireLocalAuthorityUserOne, caseName, localAuthority);
  await caseListPage.verifyCaseIsShareable(caseId);

  await I.signIn(config.wiltshireLocalAuthorityUserTwo);
  await caseListPage.verifyCaseIsShareable(caseId);
});

Scenario('Local authority revokes access from managing organisation', async ({I, caseListPage, caseViewPage, login}) => {
  const localAuthority = 'Swansea City Council';
  const managingLocalAuthority = 'Wiltshire County Council';
  const caseName = `On behalf of ${localAuthority}`;

  const caseId = await I.logInAndCreateCase(config.wiltshireLocalAuthorityUserOne, caseName, localAuthority);
  await caseListPage.verifyCaseIsShareable(caseId);

  await submitCase(caseId);
  await grantAccessToManagedLocalAuthority(caseId, config.swanseaLocalAuthorityUserOne);

  await login('swanseaLocalAuthorityUserOne');
  await I.navigateToCaseDetails(caseId);
  await I.seeEvent(config.applicationActions.removeManagingOrganisation);
  await caseViewPage.goToNewActions(config.applicationActions.removeManagingOrganisation);
  I.see(managingLocalAuthority);
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.applicationActions.removeManagingOrganisation);
  await I.dontSeeEvent(config.applicationActions.removeManagingOrganisation);

  await login('wiltshireLocalAuthorityUserOne');
  await caseListPage.verifyCaseIsNotAccessible(caseId);
});

const submitCase = async (caseId) => {
  await api.populateWithData(caseId, mandatorySubmissionFields);
};

const grantAccessToManagedLocalAuthority = async (caseId, user) => {
  await api.grantCaseAccess(caseId, user, '[LASOLICITOR]');
};
