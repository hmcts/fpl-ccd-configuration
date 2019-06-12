/* global xScenario */
const config = require('../config.js');
const fields = {
  documentLink: 'ccd-read-document-field>a',
};
let caseId;

Feature('Submit Case').retry(2);
Before(async(I, caseViewPage) => {
  I.logInAndCreateCase(config.swanseaLocalAuthorityEmailUserOne, config.localAuthorityPassword);
  caseId = await I.grabTextFrom('.heading-h1');
  I.selectOption(caseViewPage.actionsDropdown, config.applicationActions.submitCase);
  I.click(caseViewPage.goButton);
});

Scenario('Can give consent and submit the case', (I, caseViewPage, submitApplicationPage) => {
  submitApplicationPage.giveConsent();
  I.continueAndSubmit();
  I.seeEventSubmissionConfirmation(config.applicationActions.submitCase);
  I.dontSee(caseViewPage.actionsDropdown);
  caseViewPage.selectTab(caseViewPage.tabs.documents);
  I.reloadPage();
  I.waitForElement(fields.documentLink, 5);
  I.see('Barnet_Council_v_Smith.pdf');
});

Scenario('Cannot submit a case unless consent is given', I => {
  I.see(`I, ${config.swanseaLocalAuthorityUserOne.forename} ${config.swanseaLocalAuthorityUserOne.surname}, believe that the facts stated in this application are true.`);
  I.click('Continue');
  I.seeInCurrentUrl('/submitApplication');
});

//test skipped for now as after number of tests go on to next page to un-skip remove x from before scenario, clear volumes and run test
xScenario('Can submit a case and see date submitted', (I, caseViewPage, caseListPage, submitApplicationPage) => {
  submitApplicationPage.giveConsent();
  I.continueAndSubmit();
  I.seeEventSubmissionConfirmation(config.applicationActions.submitCase);
  caseViewPage.goToCaseList();
  caseListPage.changeStateFilter('Submitted');
  let row = caseListPage.findCase(caseId);
  I.seeSubmissionDate(row);
});
