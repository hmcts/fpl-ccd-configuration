const config = require('../config.js');

Feature('Smoke tests @smoke-tests').retry(2);

Scenario('Sign in as local authority', (I, loginPage) => {
  loginPage.signIn(config.swanseaLocalAuthorityEmailUserOne, config.localAuthorityPassword);
  I.see('Create new case');
  I.signOut();
});

Scenario('Sign in as court admin', (I, loginPage) => {
  loginPage.signIn(config.hmctsAdminEmail, config.hmctsAdminPassword);
  I.see('Create new case');
  I.signOut();
});

Scenario('Sign in as CAFCASS', (I, loginPage) => {
  loginPage.signIn(config.cafcassEmail, config.cafcassPassword);
  I.see('Create new case');
  I.signOut();
});
