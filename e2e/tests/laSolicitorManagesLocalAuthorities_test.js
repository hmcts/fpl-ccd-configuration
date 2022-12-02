const config = require('../config.js');
const api = require('../helpers/api_helper');
const assert = require('assert');

const mandatoryWithMultipleChildren = require('../fixtures/caseData/mandatoryWithMultipleChildren.json');

const SHARED_LA_ROLE = '[LASHARED]';

const swanseaLocalAuthority = {
  name: 'Swansea City Council',
  email: 'swansea@test.com',
  court: 'Family Court sitting at Swansea',
};

const hillingdonLocalAuthority = {
  name: 'London Borough Hillingdon',
  email: 'hilingdon@test.com',
  court: 'Family Court sitting at West London',
};

let caseId;

Feature('LA solicitor manages local authorities');

async function setupScenario(I) {
  if (!caseId) {
    caseId = await I.submitNewCaseWithData(mandatoryWithMultipleChildren);
  }
}

Scenario('LA Solicitor adds secondary local authority and can update only his own local authority ', async ({I, caseViewPage, manageLocalAuthoritiesEventPage, enterLocalAuthorityEventPage, login}) => {
  await setupScenario(I);
  await login('swanseaLocalAuthorityUserOne');
  await I.navigateToCaseDetails(caseId);

  await caseViewPage.goToNewActions(config.administrationActions.manageLocalAuthorities);
  manageLocalAuthoritiesEventPage.selectAddLocalAuthorityForLASolicitor();
  await manageLocalAuthoritiesEventPage.selectLocalAuthority(hillingdonLocalAuthority.name);
  await I.goToNextPage();

  const email = manageLocalAuthoritiesEventPage.getEmailAddress();
  assert.ok(email);
  await manageLocalAuthoritiesEventPage.setEmailAddress(hillingdonLocalAuthority.email);

  await I.completeEvent('Save and continue');
  //I.seeEventSubmissionConfirmation(config.administrationActions.manageLocalAuthorities); //flaky

  caseViewPage.selectTab(caseViewPage.tabs.casePeople);

  I.seeInTab(['Applicant 1', 'Name'], swanseaLocalAuthority.name);
  I.seeTagInTab(['Applicant 1', 'Designated local authority']);

  I.seeInTab(['Applicant 2', 'Name'], hillingdonLocalAuthority.name);
  I.seeInTab(['Applicant 2', 'Group email address'], hillingdonLocalAuthority.email);
  I.dontSeeTagInTab(['Applicant 2', 'Designated local authority']);
  I.dontSeeInTab('Applicant 2', 'Colleague 1');

  await caseViewPage.goToNewActions(config.applicationActions.enterLocalAuthority);

  assert.strictEqual(await enterLocalAuthorityEventPage.getLocalAuthorityName(), swanseaLocalAuthority.name);
  assert.strictEqual(await enterLocalAuthorityEventPage.getLocalAuthorityEmail(), swanseaLocalAuthority.email);
});

Scenario('Secondary LA solicitor can see all local authorities but updates only his own @flaky', async ({I, caseViewPage, enterLocalAuthorityEventPage, login}) => {

  const hillingdonLocalAuthorityUpdates = {
    pbaNumber: 'PBA1234567',
    phone: '777777',
  };

  const hillingdonColleague = {
    role: 'Solicitor',
    fullName: 'Alex Brown',
    email: 'alex@test.com',
    notificationRecipient: 'Yes',
  };

  await setupScenario(I);

  await organisationAdminGrantAccess(config.hillingdonLocalAuthorityUserOne, SHARED_LA_ROLE);

  await login('hillingdonLocalAuthorityUserOne');
  await I.navigateToCaseDetails(caseId);

  await I.seeAvailableEvents([
    config.applicationActions.enterLocalAuthority,
    config.administrationActions.uploadAdditionalApplications,
    config.administrationActions.manageDocuments,
  ]);

  await caseViewPage.goToNewActions(config.applicationActions.enterLocalAuthority);

  assert.strictEqual(await enterLocalAuthorityEventPage.getLocalAuthorityName(), hillingdonLocalAuthority.name);
  assert.strictEqual(await enterLocalAuthorityEventPage.getLocalAuthorityEmail(), hillingdonLocalAuthority.email);

  await enterLocalAuthorityEventPage.enterDetails(hillingdonLocalAuthorityUpdates);
  await I.goToNextPage();
  await enterLocalAuthorityEventPage.enterColleague(hillingdonColleague);

  await I.seeCheckAnswersAndCompleteEvent('Save and continue');
  //I.seeEventSubmissionConfirmation(config.applicationActions.enterLocalAuthority);//flaky

  caseViewPage.selectTab(caseViewPage.tabs.casePeople);

  I.seeInTab(['Applicant 1', 'Name'], swanseaLocalAuthority.name);
  I.seeTagInTab(['Applicant 1', 'Designated local authority']);
  I.seeInTab(['Applicant 1', 'Colleague 1', 'Full name'], 'Alex Brown');
  I.seeTagInTab(['Applicant 1', 'Colleague 1', 'Main contact']);

  I.seeInTab(['Applicant 2', 'Name'], hillingdonLocalAuthority.name);
  I.seeInTab(['Applicant 2', 'Group email address'], hillingdonLocalAuthority.email);
  I.seeInTab(['Applicant 2', 'PBA number'], hillingdonLocalAuthorityUpdates.pbaNumber);
  I.seeInTab(['Applicant 2', 'Phone number'], hillingdonLocalAuthorityUpdates.phone);
  I.dontSeeTagInTab(['Applicant 2', 'Designated local authority']);

  I.seeInTab(['Applicant 2', 'Colleague 1', 'Role'], hillingdonColleague.role);
  I.seeInTab(['Applicant 2', 'Colleague 1', 'Full name'], hillingdonColleague.fullName);
  I.seeInTab(['Applicant 2', 'Colleague 1', 'Email address'], hillingdonColleague.email);
  I.seeInTab(['Applicant 2', 'Colleague 1', 'Send them case update notifications?'], hillingdonColleague.notificationRecipient);
  I.seeTagInTab(['Applicant 2', 'Colleague 1', 'Main contact']);
});

Scenario('Designated LA Solicitor removes secondary local authority', async ({I, caseViewPage, caseListPage, manageLocalAuthoritiesEventPage, login}) => {

  await setupScenario(I);

  await login('swanseaLocalAuthorityUserOne');
  await I.navigateToCaseDetails(caseId);
  await caseViewPage.goToNewActions(config.administrationActions.manageLocalAuthorities);
  manageLocalAuthoritiesEventPage.selectRemoveLocalAuthorityForLASolicitor();

  await I.goToNextPage();
  I.see(hillingdonLocalAuthority.name);

  await I.completeEvent('Save and continue');
  //I.seeEventSubmissionConfirmation(config.administrationActions.manageLocalAuthorities);//flaky

  caseViewPage.selectTab(caseViewPage.tabs.casePeople);

  I.seeInTab(['Applicant 1', 'Name'], swanseaLocalAuthority.name);
  I.seeTagInTab(['Applicant 1', 'Designated local authority']);

  I.dontSeeInTab(['Applicant 2', 'Name'], hillingdonLocalAuthority.name);

  await login('hillingdonLocalAuthorityUserOne');

  caseListPage.verifyCaseIsNotAccessible(caseId);
});

async function organisationAdminGrantAccess(user, role) {
  await api.grantCaseAccess(caseId, user, role);
}
