//const config = require('../config');
//const output = require('codeceptjs').output;

Feature('Example Test');

Scenario('Test Something', async ({ I }) => {

  I.amOnPage('https://github.com');
  I.click('Sign in', '.HeaderMenu-link--sign-in');
  I.see('Sign in to GitHub', 'h1');
  I.fillField('Username or email address', 'something@totest.com');
  I.fillField('Password', '123456');
  I.click('Sign in');
  I.see('Incorrect username or password.', '.flash-error');

});
