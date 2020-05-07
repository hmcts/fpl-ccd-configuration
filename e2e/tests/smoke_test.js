/* global process */

Feature('Smoke tests @smoke-tests');

Scenario('Sign in as local authority', async I => {
  I.amOnPage(process.env.URL || 'http://localhost:3000');
});
