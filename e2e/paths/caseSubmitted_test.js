const config = require('../config.js');

Feature('Submit Case').retry(2);

Before((I, caseViewPage) => {
  I.logInAndCreateCase(config.localAuthorityEmail, config.localAuthorityPassword);
  I.selectOption(caseViewPage.actionsDropdown, config.applicationActions.submitCase);
  I.click(caseViewPage.goButton);
});

Scenario('Submitting case', (I, caseViewPage, submitApplicationPage) => {
  submitApplicationPage.giveConsentAndContinue();
  I.click('Continue');  
  I.click('Submit');
  I.waitForElement('.tabs');
  I.seeEventSubmissionConfirmation(config.applicationActions.submitCase);
  I.dontSee(caseViewPage.actionsDropdown);
  caseViewPage.selectTab(caseViewPage.tabs.evidence);
  I.reloadPage();
  I.see('Barnet_Council_v_Smith.pdf');
});

Scenario('Authenticated user\'s fullname is displayed', I => {
  I.see('I, Tester Tester, believe that the facts stated in this application are true.');
});
