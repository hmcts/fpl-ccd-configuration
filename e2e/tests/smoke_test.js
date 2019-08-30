const config = require('../config.js');

Feature('Smoke tests @smoke-tests');

Scenario('Sign in as local authority', async (I) => {
  await I.signIn(config.smokeTestLocalAuthorityEmail, config.smokeTestLocalAuthorityPassword);
  I.see('Create new case');
  I.signOut();
});
