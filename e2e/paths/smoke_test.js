const config = require('../config.js');

Feature('Smoke tests @smoke-tests').retry(2);

Scenario('Sign in as local authority', (I, loginPage) => {
  loginPage.signIn(config.localAuthorityEmail, config.localAuthorityPassword);
  I.see('Create new case');
});
