const config = require('../config.js');
const apiHelper = require('../helpers/api_helper.js');

const solicitor1 = config.privateSolicitorOne;
const solicitor2 = config.hillingdonLocalAuthorityUserOne;

let caseId;

Feature('Notice of change');

BeforeSuite(async ({I}) => {
  caseId = await I.submitNewCaseWithData();
  solicitor1.details = await apiHelper.getUser(solicitor1);
  solicitor2.details = await apiHelper.getUser(solicitor2);
});

Scenario('Private solicitor obtains access to an unrepresented case', async ({I, caseListPage, caseViewPage, noticeOfChangePage}) => {

  await I.signIn(solicitor1);
  I.navigateToCaseList();
  caseListPage.searchForCasesWithId(caseId);
  I.dontSeeCaseInSearchResult(caseId);

  await noticeOfChangePage.userCompletesNoC(caseId, 'Swansea City Council', 'Joe', 'Bloggs');
  caseViewPage.selectTab(caseViewPage.tabs.casePeople);
  await assertRepresentative(I, solicitor1.details, 'Private solicitors');
});

Scenario('Private solicitor replaces respondent solicitor on a represented case', async ({I, caseListPage, caseViewPage, noticeOfChangePage}) => {
  await I.signIn(solicitor2);
  I.navigateToCaseList();
  caseListPage.searchForCasesWithId(caseId);
  I.dontSeeCaseInSearchResult(caseId);

  await noticeOfChangePage.userCompletesNoC(caseId, 'Swansea City Council', 'Joe', 'Bloggs');
  caseViewPage.selectTab(caseViewPage.tabs.casePeople);
  await assertRepresentative(I, solicitor2.details, 'Private solicitors');

  await I.navigateToCaseDetailsAs(solicitor1, caseId);

  I.see('No cases found.');
});

Scenario('Hmcts admin removes respondent solicitor', async ({I, caseViewPage, enterRespondentsEventPage}) => {
  await I.navigateToCaseDetailsAs(config.hmctsAdminUser, caseId);
  await caseViewPage.goToNewActions(config.administrationActions.amendRespondents);

  await enterRespondentsEventPage.enterRepresentationDetails('No', {}, 0);
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.amendRespondents);

  await I.navigateToCaseDetailsAs(solicitor2, caseId);

  I.see('No cases found.');
});

const assertRepresentative = async (I, user, organisation) => {
  I.seeInTab(['Representative', 'Representative\'s first name'], user.forename);
  I.seeInTab(['Representative', 'Representative\'s last name'], user.surname);
  I.seeInTab(['Representative', 'Email address'], user.email);

  if (organisation) {
    I.waitForText(organisation);
    I.seeOrganisationInTab(['Respondents 1', 'Representative', 'Name'], organisation);
  }
};
