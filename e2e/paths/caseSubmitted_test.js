const config = require('../config.js');
const fields = {
  documentLink: 'ccd-read-document-field>a',
};

Feature('Submit Case');

Before((I, caseViewPage) => {
  I.logInAndCreateCase(config.swanseaLocalAuthorityEmailUserOne, config.localAuthorityPassword);
});

Scenario('Cannot submit an incomplete case', (I, caseViewPage, submitApplicationPage) => {
  caseViewPage.goToNewActions(config.applicationActions.submitCase);
  submitApplicationPage.giveConsent();
  I.click('Continue');
  I.waitForElement('.check-your-answers');
  I.click('Submit');
  I.waitForElement('.error-summary-list');
  I.see("Select at least one type of order");
  I.see("You need to add details to children");
  I.see("You need to add details to applicant");
  I.see("You need to add details to hearing");
});

Scenario('Can give consent and submit the case', (I, caseViewPage, submitApplicationPage) => {
  I.enterMandatoryFields();
  caseViewPage.goToNewActions(config.applicationActions.submitCase);
  submitApplicationPage.giveConsent();
  I.continueAndSubmit();
  I.seeEventSubmissionConfirmation(config.applicationActions.submitCase);
  I.dontSee(caseViewPage.actionsDropdown);
  caseViewPage.selectTab(caseViewPage.tabs.documents);
  I.reloadPage();
  I.waitForElement(fields.documentLink, 5);
  I.see('Barnet_Council_v_Smith.pdf');
});

Scenario('Cannot submit a case unless consent is given', (I, caseViewPage) => {
  caseViewPage.goToNewActions(config.applicationActions.submitCase);
  I.see(`I, ${config.swanseaLocalAuthorityUserOne.forename} ${config.swanseaLocalAuthorityUserOne.surname}, believe that the facts stated in this application are true.`);
  I.click('Continue');
  I.seeInCurrentUrl('/submitApplication');
});
