const config = require('../config.js');
const mandatorySubmissionFields = require('../fixtures/caseData/mandatorySubmissionFields.json');
const representedCase = require('../fixtures/caseData/representedCase.json');

let caseId;

Feature('Notice of change');

Scenario('Private solicitor obtains access to an unrepresented case', async ({I, caseListPage, caseViewPage, noticeOfChangePage}) => {
  caseId = await I.submitNewCaseWithData(mandatorySubmissionFields);
  await I.signIn(config.privateSolicitorOne);
  I.navigateToCaseList();
  caseListPage.searchForCasesWithId(caseId);
  I.dontSeeCaseInSearchResult(caseId);
  
  await userCompletesNoC(I, caseListPage, noticeOfChangePage);
  
  caseViewPage.selectTab(caseViewPage.tabs.casePeople);
  assertRepresentative(I, config.privateSolicitorOne.email, 'External', config.privateSolicitorOne.email, 'Private solicitors');
});

Scenario('Private solicitor replaces respondent solicitor on a represented case', async ({I, caseListPage, caseViewPage, noticeOfChangePage}) => {
  caseId = await I.submitNewCaseWithData(representedCase);
  await I.navigateToCaseDetailsAs(config.hmctsAdminUser, caseId);
  caseViewPage.selectTab(caseViewPage.tabs.casePeople);
  assertRepresentative(I, 'Tom', 'Jones', 'test@test.co.uk');
  
  await I.signIn(config.privateSolicitorOne);
  I.navigateToCaseList();
  caseListPage.searchForCasesWithId(caseId);
  I.dontSeeCaseInSearchResult(caseId);
  
  await userCompletesNoC(I, caseListPage, noticeOfChangePage);
  caseViewPage.selectTab(caseViewPage.tabs.casePeople);
  assertRepresentative(I, config.privateSolicitorOne.email, 'External', config.privateSolicitorOne.email, 'Private solicitors');
});

const userCompletesNoC = async (I, caseListPage, noticeOfChangePage) => {
  noticeOfChangePage.navigate();
  await noticeOfChangePage.enterCaseReference(caseId);
  await I.retryUntilExists(() => I.click('Continue'), noticeOfChangePage.fields.applicantName);
  await noticeOfChangePage.enterApplicantName('Swansea City Council');
  noticeOfChangePage.enterRespondentName('Joe', 'Bloggs');
  await I.retryUntilExists(() => I.click('Continue'), noticeOfChangePage.fields.confirmNoC);
  noticeOfChangePage.confirmNoticeOfChange();
  await I.retryUntilExists(() => I.click('Submit'), '.govuk-panel--confirmation');
  I.see('Notice of change successful');
  await I.retryUntilExists(() => I.click('View this case'), '.case-title');
};

const assertRepresentative = (I, firstName, lastName, email, organisation) => {
  I.seeInTab(['Representative', 'Representative\'s first name'], firstName);
  I.seeInTab(['Representative', 'Representative\'s last name'], lastName);
  I.seeInTab(['Representative', 'Email address'], email);

  if (organisation) {
    I.seeInTab(['Representative', 'Organisation'], 'Private solicitors');
  }
};
