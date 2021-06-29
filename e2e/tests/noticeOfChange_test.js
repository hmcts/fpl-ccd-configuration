const config = require('../config.js');
const dateFormat = require('dateformat');
const apiHelper = require('../helpers/api_helper.js');
const mandatoryWithMultipleRespondents = require('../fixtures/caseData/mandatoryWithMultipleRespondents.json');

const solicitor1 = config.privateSolicitorOne;
const solicitor2 = config.hillingdonLocalAuthorityUserOne;
const solicitor3 = config.wiltshireLocalAuthorityUserOne;

let caseId;

Feature('Notice of change');

async function setupScenario(I) {
  if (!caseId) { caseId = await I.submitNewCaseWithData(mandatoryWithMultipleRespondents); }
  if (!solicitor1.details) {
    solicitor1.details = await apiHelper.getUser(solicitor1);
    solicitor1.details.organisation = 'Private solicitors';
  }
  if (!solicitor2.details) {
    solicitor2.details = await apiHelper.getUser(solicitor2);
    solicitor2.details.organisation = 'London Borough Hillingdon';
  }
  if (!solicitor3.details) {
    solicitor3.details = await apiHelper.getUser(solicitor3);
    solicitor3.details.organisation = 'Wiltshire County Council';
  }
}

Scenario('Solicitor can request representation only after case submission', async ({I, caseListPage, caseViewPage, submitApplicationEventPage, noticeOfChangePage}) => {
  await setupScenario(I);
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

  caseViewPage.selectTab(caseViewPage.tabs.changeOfRepresentatives);
  assertChangeOfRepresentative(I, 1, 'Notice of change', 'Joe Bloggs', solicitor1.details.email, { addedUser: solicitor1.details });
});

Scenario('Solicitor request representation of second unrepresented respondent', async ({I, caseListPage, caseViewPage, noticeOfChangePage}) => {
  await setupScenario(I);
  await I.signIn(solicitor2);
  caseListPage.verifyCaseIsNotAccessible(caseId);

  await noticeOfChangePage.userCompletesNoC(caseId, 'Swansea City Council', 'Emma', 'White');
  caseViewPage.selectTab(caseViewPage.tabs.casePeople);
  assertRepresentative(I, solicitor2.details, 'London Borough Hillingdon', 2);

  caseViewPage.selectTab(caseViewPage.tabs.changeOfRepresentatives);
  assertChangeOfRepresentative(I, 2, 'Notice of change', 'Emma White', solicitor2.details.email, { addedUser: solicitor2.details });
});

Scenario('Solicitor request representation of represented respondent', async ({I, caseListPage, caseViewPage, noticeOfChangePage}) => {
  await setupScenario(I);
  await I.signIn(solicitor3);
  caseListPage.verifyCaseIsNotAccessible(caseId);

  await noticeOfChangePage.userCompletesNoC(caseId, 'Swansea City Council', 'Joe', 'Bloggs');
  caseViewPage.selectTab(caseViewPage.tabs.casePeople);
  assertRepresentative(I, solicitor3.details, 'Wiltshire County Council');

  caseViewPage.selectTab(caseViewPage.tabs.changeOfRepresentatives);
  assertChangeOfRepresentative(I, 3, 'Notice of change', 'Joe Bloggs', solicitor3.details.email, { addedUser: solicitor3.details, removedUser: solicitor1.details });

  await I.signIn(solicitor1);
  caseListPage.verifyCaseIsNotAccessible(caseId);
});

Scenario('Hmcts admin replaces respondent solicitor', async ({I, caseListPage, caseViewPage, enterRespondentsEventPage}) => {
  await setupScenario(I);
  await I.navigateToCaseDetailsAs(config.hmctsAdminUser, caseId);
  await caseViewPage.goToNewActions(config.administrationActions.amendRespondents);

  await enterRespondentsEventPage.updateRegisteredOrganisation('Swansea City Council', 1);
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.amendRespondents);

  caseViewPage.selectTab(caseViewPage.tabs.changeOfRepresentatives);
  assertChangeOfRepresentative(I, 4, 'FPL', 'Emma White', 'HMCTS', { addedUser: { ...solicitor2.details, organisation: 'Swansea City Council'}, removedUser: solicitor2.details });

  await I.signIn(solicitor2);
  caseListPage.verifyCaseIsNotAccessible(caseId);
});

Scenario('Hmcts admin removes respondent solicitor', async ({I, caseListPage, caseViewPage, enterRespondentsEventPage}) => {
  await setupScenario(I);
  await I.navigateToCaseDetailsAs(config.hmctsAdminUser, caseId);
  await caseViewPage.goToNewActions(config.administrationActions.amendRespondents);

  await enterRespondentsEventPage.enterRepresentationDetails('No', {}, 0);
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.amendRespondents);

  caseViewPage.selectTab(caseViewPage.tabs.changeOfRepresentatives);
  assertChangeOfRepresentative(I, 5, 'FPL', 'Joe Bloggs', 'HMCTS', {removedUser: solicitor3.details });

  await I.signIn(solicitor3);
  caseListPage.verifyCaseIsNotAccessible(caseId);
});

const assertRepresentative = (I, user, organisation, index = 1) => {
  I.seeInTab(['Representative', 'Representative\'s first name'], user.forename);
  I.seeInTab(['Representative', 'Representative\'s last name'], user.surname);
  I.seeInTab(['Representative', 'Email address'], user.email);

  if (organisation) {
    I.waitForText(organisation, 40);
    I.seeOrganisationInTab([`Respondents ${index}`, 'Representative', 'Name'], organisation);
  }
};

const assertChangeOfRepresentative = (I, index, method, respondentName, actingUserEmail, change) => {
  let representative = `Change of representative ${index}`;
  let addedUser = change.addedUser;
  let removedUser = change.removedUser;

  I.seeInTab([representative, 'Respondent'], respondentName);
  I.seeInTab([representative, 'Date'], dateFormat(new Date(), 'd mmm yyyy'));
  I.seeInTab([representative, 'Updated by'], actingUserEmail);
  I.seeInTab([representative, 'Updated via'], method);

  if (addedUser) {
    I.seeInTab([representative, 'Added representative', 'First name'], addedUser.forename);
    I.seeInTab([representative, 'Added representative', 'Last name'], addedUser.surname);
    I.seeInTab([representative, 'Added representative', 'Email'], addedUser.email);
    I.waitForText(addedUser.organisation, 40);
    I.seeOrganisationInTab([representative, 'Added representative', 'Name'], addedUser.organisation);
  }

  if (removedUser) {
    I.seeInTab([representative, 'Removed representative', 'First name'], removedUser.forename);
    I.seeInTab([representative, 'Removed representative', 'Last name'], removedUser.surname);
    I.seeInTab([representative, 'Removed representative', 'Email'], removedUser.email);
    I.waitForText(removedUser.organisation, 40);
    I.seeOrganisationInTab([representative, 'Removed representative', 'Name'], removedUser.organisation);
  }
};
