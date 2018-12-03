/*global process*/

const config = require('../config.js');

let caseId;
let caseUrl;
let baseUrl = process.env.URL || 'http://localhost:3451';

Feature('Cases visible only to respective local authority and admin');

Before(async (I, caseViewPage) => {
  I.logInAndCreateCase(config.swanseaLocalAuthorityEmailUserOne, config.localAuthorityPassword);
  caseId = await I.grabTextFrom('.heading-medium');
  caseUrl = caseId.toString().replace('#', '').replace(/-/g, '');
  caseViewPage.goToNewActions(config.applicationActions.submitCase);
  I.click('Submit');
  I.click('Sign Out');
});

Scenario('Different user in the same local authority can see case created', async (I, loginPage) => {
  loginPage.signIn(config.swanseaLocalAuthorityEmailUserTwo, config.localAuthorityPassword);
  I.navigateToUrl(baseUrl + '/case/PUBLICLAW/Shared_Storage_DRAFTType/' + caseUrl);
  I.see(caseId);
});

Scenario('Different user in a different local authority cannot see case created', (I, loginPage) => {
  loginPage.signIn(config.hillingdonLocalAuthorityEmailUserOne, config.localAuthorityPassword);
  I.navigateToUrl(baseUrl + '/case/PUBLICLAW/Shared_Storage_DRAFTType/' + caseUrl);
  I.seeInCurrentUrl('error');
});

Scenario('HMCTS admin user can see the case', (I, loginPage) => {
  loginPage.signIn(config.hmctsAdminEmail, config.hmctsAdminPassword);
  I.navigateToUrl(baseUrl + '/case/PUBLICLAW/Shared_Storage_DRAFTType/' + caseUrl);
  I.see(caseId);
});

Scenario('CAFCASS user can see the case', (I, loginPage) => {
  loginPage.signIn(config.cafcassEmail, config.cafcassPassword);
  I.navigateToUrl(baseUrl + '/case/PUBLICLAW/Shared_Storage_DRAFTType/' + caseUrl);
  I.see(caseId);
});
