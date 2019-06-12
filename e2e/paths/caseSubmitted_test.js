/* global locate*/
const config = require('../config.js');
const fields = {
  documentLink: 'ccd-read-document-field>a',
};
const monthNames = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun',
  'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
let caseId;

Feature('Submit Case');
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

Scenario('Can submit a case and see date submitted', (I, caseViewPage, caseListPage, submitApplicationPage) => {
  submitApplicationPage.giveConsent();
  I.continueAndSubmit();
  I.seeEventSubmissionConfirmation(config.applicationActions.submitCase);
  caseViewPage.goToCaseList();
  caseListPage.changeStateFilter('Submitted');
  const row = locate('.//tr').withChild(`.//td/a[text()='${caseId.slice(1)}']`);
  let currentDate = new Date();

  I.seeElement(locate(row.withChild('.//td[4]').withText(currentDate.getDate() + ' ' + monthNames[currentDate.getMonth()] + ' ' + currentDate.getFullYear())));
});
