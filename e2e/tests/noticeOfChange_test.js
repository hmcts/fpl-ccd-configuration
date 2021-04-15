const config = require('../config.js');
const mandatorySubmissionFields = require('../fixtures/caseData/mandatorySubmissionFields.json');

let caseId;

Scenario('Private solicitor requests case access through notice of change', async ({I, caseListPage, noticeOfChangePage}) => {
  caseId = await I.submitNewCaseWithData(mandatorySubmissionFields);
  await I.signIn(config.privateSolicitorOne);
  I.navigateToCaseList();
  caseListPage.searchForCasesWithId(caseId);
  I.see('No cases found.');
  noticeOfChangePage.navigate();
  noticeOfChangePage.enterCaseReference(caseId);
  I.click('Continue');
  noticeOfChangePage.enterApplicantName('Swansea City Council');
  noticeOfChangePage.enterRespondentName('Joe', 'Bloggs');
  I.click('Continue');
  noticeOfChangePage.confirmNoticeOfChange();
  I.click('Submit');
  I.see('Notice of change successful');
  I.navigateToCaseList();
  caseListPage.searchForCasesWithId(caseId);
  I.dontSee('No cases found.');
});
