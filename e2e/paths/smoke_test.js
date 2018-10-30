const config = require('../config.js');

Feature('Smoke tests @smoke-tests');

Scenario('Sign in as local authority', (I, loginPage) => {
  loginPage.signIn(config.localAuthorityEmail, config.localAuthorityPassword);
  I.see('Create new case');
  I.signOut();
});

Scenario('Sign in as court admin', (I, loginPage) => {
  loginPage.signIn(config.hmctsAdminEmail, config.hmctsAdminPassword);
  I.see('Create new case');
  I.signOut();
});
