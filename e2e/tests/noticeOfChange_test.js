const config = require('../config.js');
const mandatorySubmissionFields = require('../fixtures/caseData/mandatorySubmissionFields.json');

let caseId;

Feature('Notice of change');

BeforeSuite(async ({I}) => {
  caseId = await I.submitNewCaseWithData(mandatorySubmissionFields);
});

Scenario('Private solicitor obtains case access through NoC', async ({I, caseListPage, noticeOfChangePage}) => {
  await I.signIn(config.privateSolicitorOne);
  I.navigateToCaseList();
  caseListPage.searchForCasesWithId(caseId);
  I.dontSeeCaseInSearchResult(caseId);
  noticeOfChangePage.navigate();
  noticeOfChangePage.enterCaseReference(caseId);
  await I.retryUntilExists(() => I.click('Continue'), noticeOfChangePage.fields.applicantName);
  noticeOfChangePage.enterApplicantName('Swansea City Council');
  noticeOfChangePage.enterRespondentName('Joe', 'Bloggs');
  await I.retryUntilExists(() => I.click('Continue'), noticeOfChangePage.fields.confirmNoC);
  noticeOfChangePage.confirmNoticeOfChange();
  await I.retryUntilExists(() => I.click('Submit'), '.govuk-panel--confirmation');
  I.see('Notice of change successful');
  I.navigateToCaseList();
  caseListPage.searchForCasesWithId(caseId);
  I.seeCaseInSearchResult(caseId);
});
