const config = require('../config.js');
const apiHelper = require('../helpers/api_helper.js');
const mandatoryWithMultipleRespondents = require('../fixtures/caseData/mandatoryWithMultipleRespondents.json');

const solicitor1 = config.privateSolicitorOne;
const solicitor2 = config.hillingdonLocalAuthorityUserOne;
const solicitor3 = config.wiltshireLocalAuthorityUserOne;

let caseId;

Feature('Notice of change').retry(config.maxTestRetries);

BeforeSuite(async ({I}) => {
  caseId = await I.submitNewCaseWithData(mandatoryWithMultipleRespondents);
  solicitor1.details = await apiHelper.getUser(solicitor1);
  solicitor2.details = await apiHelper.getUser(solicitor2);
  solicitor3.details = await apiHelper.getUser(solicitor3);
});

Scenario('Solicitor can request representation only after case submission', async ({I, caseListPage, caseViewPage, submitApplicationEventPage, noticeOfChangePage}) => {
  await I.signIn(solicitor1);
  caseListPage.verifyCaseIsNotAccessible(caseId);

  await noticeOfChangePage.navigate();
  await noticeOfChangePage.enterCaseReference(caseId);
  I.click('Continue');
  I.see('Your notice of change request has not been submitted');

  await I.navigateToCaseDetailsAs(config.swanseaLocalAuthorityUserOne, caseId);
  await caseViewPage.goToNewActions(config.applicationActions.submitCase);
  await submitApplicationEventPage.giveConsent();
  await I.completeEvent('Submit', null, true);

  await I.signIn(solicitor1);

  await noticeOfChangePage.userCompletesNoC(caseId, 'Swansea City Council', 'Joe', 'Bloggs');
  caseViewPage.selectTab(caseViewPage.tabs.casePeople);
  assertRepresentative(I, solicitor1.details, 'Private solicitors');
});

Scenario('Solicitor request representation of second unrepresented respondent', async ({I, caseListPage, caseViewPage, noticeOfChangePage}) => {
  await I.signIn(solicitor2);
  caseListPage.verifyCaseIsNotAccessible(caseId);

  await noticeOfChangePage.userCompletesNoC(caseId, 'Swansea City Council', 'Emma', 'White');
  caseViewPage.selectTab(caseViewPage.tabs.casePeople);
  assertRepresentative(I, solicitor2.details, 'London Borough Hillingdon', 2);
});

Scenario('Solicitor request representation of represented respondent', async ({I, caseListPage, caseViewPage, noticeOfChangePage}) => {
  await I.signIn(solicitor3);
  caseListPage.verifyCaseIsNotAccessible(caseId);

  await noticeOfChangePage.userCompletesNoC(caseId, 'Swansea City Council', 'Joe', 'Bloggs');
  caseViewPage.selectTab(caseViewPage.tabs.casePeople);
  assertRepresentative(I, solicitor3.details, 'Wiltshire County Council');

  await I.signIn(solicitor1);
  caseListPage.verifyCaseIsNotAccessible(caseId);
});

Scenario('Hmcts admin replaces respondent solicitor', async ({I, caseListPage, caseViewPage, enterRespondentsEventPage}) => {
  await I.navigateToCaseDetailsAs(config.hmctsAdminUser, caseId);
  await caseViewPage.goToNewActions(config.administrationActions.amendRespondents);

  await enterRespondentsEventPage.updateRegisteredOrganisation('Swansea City Council', 1);
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.amendRespondents);

  await I.signIn(solicitor2);
  caseListPage.verifyCaseIsNotAccessible(caseId);
});

Scenario('Hmcts admin removes respondent solicitor', async ({I, caseListPage, caseViewPage, enterRespondentsEventPage}) => {
  await I.navigateToCaseDetailsAs(config.hmctsAdminUser, caseId);
  await caseViewPage.goToNewActions(config.administrationActions.amendRespondents);

  await enterRespondentsEventPage.enterRepresentationDetails('No', {}, 0);
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.amendRespondents);

  await I.signIn(solicitor3);
  caseListPage.verifyCaseIsNotAccessible(caseId);
});

const assertRepresentative = (I, user, organisation, index = 1) => {
  I.seeInTab(['Representative', 'Representative\'s first name'], user.forename);
  I.seeInTab(['Representative', 'Representative\'s last name'], user.surname);
  I.seeInTab(['Representative', 'Email address'], user.email);

  if (organisation) {
    I.waitForText(organisation);
    I.seeOrganisationInTab([`Respondents ${index}`, 'Representative', 'Name'], organisation);
  }
};

