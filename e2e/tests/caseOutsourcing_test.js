const config = require('../config');

Feature('Case outsourcing');

Scenario('Private solicitor creates case on behalf of local authority ', async ({I, caseListPage}) => {
  const localAuthority = 'Swansea City Council';
  const caseName = `On behalf of ${localAuthority}`;

  const caseId = await I.logInAndCreateCase(config.privateSolicitorOne, caseName, localAuthority);
  await caseListPage.verifyCaseIsShareable(caseId);

  await I.signIn(config.privateSolicitorTwo);
  caseListPage.verifyCaseIsNotAccessible(caseId);

  await I.signIn(config.swanseaLocalAuthorityUserOne);
  caseListPage.verifyCaseIsNotAccessible(caseId);
});

Scenario('Local authority creates case on behalf of other local authority', async ({I, caseListPage}) => {
  const localAuthority = 'Swansea City Council';
  const caseName = `On behalf of ${localAuthority}`;

  const caseId = await I.logInAndCreateCase(config.wiltshireLocalAuthorityUserOne, caseName, localAuthority);
  await caseListPage.verifyCaseIsShareable(caseId);

  await I.signIn(config.wiltshireLocalAuthorityUserTwo, caseId);
  caseListPage.verifyCaseIsNotAccessible(caseId);

  await I.signIn(config.swanseaLocalAuthorityUserOne, caseId);
  caseListPage.verifyCaseIsNotAccessible(caseId);
});

Scenario('Local authority creates case for its own', async ({I, caseListPage}) => {
  const localAuthority = 'Wiltshire County Council';
  const caseName = `${localAuthority} case`;

  const caseId = await I.logInAndCreateCase(config.wiltshireLocalAuthorityUserOne, caseName, localAuthority);
  await caseListPage.verifyCaseIsShareable(caseId);

  await I.signIn(config.wiltshireLocalAuthorityUserTwo);
  await caseListPage.verifyCaseIsShareable(caseId);
});
