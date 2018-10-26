const config = require('../config.js');

Feature('Login as hmcts admin');

Scenario('HMCTS admin can login and see submitted Case List', (I, loginPage) => {
  loginPage.signIn(config.hmctsAdminEmail, config.hmctsAdminPassword);
  I.wait(3);
  I.see('Case List');
});
