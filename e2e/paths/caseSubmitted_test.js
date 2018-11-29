const config = require('../config.js');

Feature('Submit Case').retry(2);

Before((I, caseViewPage) => {
  I.logInAndCreateCase(config.swanseaLocalAuthorityEmailKurt, config.localAuthorityPassword);
  I.selectOption(caseViewPage.actionsDropdown, config.applicationActions.submitCase);
  I.click(caseViewPage.goButton);
});


Scenario('Submitting case', (I, caseViewPage) => {
  I.click('Submit');
  I.waitForElement('.tabs');
  I.seeEventSubmissionConfirmation(config.applicationActions.submitCase);
  I.dontSee(caseViewPage.actionsDropdown);
  caseViewPage.selectTab(caseViewPage.tabs.evidence);
  I.reloadPage();
  I.see('Barnet_Council_v_Smith.pdf');
});
