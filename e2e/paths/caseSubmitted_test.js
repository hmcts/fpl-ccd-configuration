const config = require('../config.js');

Feature('Submit Case').retry(2);

Before((I, caseViewPage) => {
  I.logInAndCreateCase(config.swanseaLocalAuthorityEmailUserOne, config.localAuthorityPassword);
  I.selectOption(caseViewPage.actionsDropdown, config.applicationActions.submitCase);
  I.click(caseViewPage.goButton);
});

Scenario('Can give consent and submit the case', (I, caseViewPage, submitApplicationPage) => {
  submitApplicationPage.giveConsent();
  I.click('Continue');  
  I.click('Submit');
  I.waitForElement('.tabs');
  I.seeEventSubmissionConfirmation(config.applicationActions.submitCase);
  I.dontSee(caseViewPage.actionsDropdown);
  caseViewPage.selectTab(caseViewPage.tabs.evidence);
  I.reloadPage();
  I.see('Barnet_Council_v_Smith.pdf');
});

Scenario('Cannot submit a case unless consent is given', I => {
  I.see(`I, ${config.swanseaLocalAuthorityUserOne.forename} ${config.swanseaLocalAuthorityUserOne.surname}, believe that the facts stated in this application are true.`);  
  I.click('Continue');
  I.seeInCurrentUrl('/submitApplication');
});
