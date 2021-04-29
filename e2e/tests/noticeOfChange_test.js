const config = require('../config.js');
const apiHelper = require('../helpers/api_helper.js');
const mandatorySubmissionFields = require('../fixtures/caseData/mandatorySubmissionFields.json');
const representedCase = require('../fixtures/caseData/representedCase.json');

let caseId;
let privateSolicitorDetails;

Feature('Notice of change');

BeforeSuite(async () => {
  privateSolicitorDetails = await apiHelper.getUser(config.privateSolicitorOne);
});

Scenario('Private solicitor obtains access to an unrepresented case', async ({I, caseListPage, caseViewPage, noticeOfChangePage}) => {
  caseId = await I.submitNewCaseWithData(mandatorySubmissionFields);
  await I.signIn(config.privateSolicitorOne);
  I.navigateToCaseList();
  caseListPage.searchForCasesWithId(caseId);
  I.dontSeeCaseInSearchResult(caseId);

  await noticeOfChangePage.userCompletesNoC(caseId, 'Swansea City Council', 'Joe', 'Bloggs');
  caseViewPage.selectTab(caseViewPage.tabs.casePeople);
  assertRepresentative(I, privateSolicitorDetails.forename, privateSolicitorDetails.surname, privateSolicitorDetails.email, 'Private solicitors');
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

  await noticeOfChangePage.userCompletesNoC(caseId, 'Swansea City Council', 'Joe', 'Bloggs');
  caseViewPage.selectTab(caseViewPage.tabs.casePeople);
  assertRepresentative(I, privateSolicitorDetails.forename, privateSolicitorDetails.surname, privateSolicitorDetails.email, 'Private solicitors');
});

const assertRepresentative = (I, firstName, lastName, email, organisation) => {
  I.seeInTab(['Representative', 'Representative\'s first name'], firstName);
  I.seeInTab(['Representative', 'Representative\'s last name'], lastName);
  I.seeInTab(['Representative', 'Email address'], email);

  if (organisation) {
    I.waitForText(organisation);
    I.seeOrganisationInTab(['Respondents 1', 'Representative', 'Name'], organisation);
  }
};
