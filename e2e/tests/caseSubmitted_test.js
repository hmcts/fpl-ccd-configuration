const config = require('../config.js');
const fields = {
  documentLink: 'ccd-read-document-field>a',
};

Feature('Submit Case');

Before((I) => {
  I.logInAndCreateCase(config.swanseaLocalAuthorityEmailUserOne, config.localAuthorityPassword);
});

Scenario('Cannot submit an incomplete case', (I, caseViewPage, submitApplicationEventPage) => {
  caseViewPage.goToNewActions(config.applicationActions.submitCase);
  submitApplicationEventPage.giveConsent();
  I.click('Continue');
  I.waitForElement('.error-summary-list');
  I.see('Tell us the status of all documents including those that you haven\'t uploaded');
  I.see('You need to add details to orders and directions needed');
  I.see('You need to add details to children');
  I.see('You need to add details to applicant');
  I.see('You need to add details to hearing needed');
});

Scenario('Can give consent and submit the case', async (I, caseViewPage, submitApplicationEventPage) => {
  await I.enterMandatoryFields();
  caseViewPage.goToNewActions(config.applicationActions.submitCase);
  submitApplicationEventPage.giveConsent();
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
