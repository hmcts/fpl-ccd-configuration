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
  email: 'hilindon@test.com',
  court: 'Family Court sitting at West London',
};

const transferredLocalAuthoritySolicitor = {
  name: 'Emma Green',
  email: 'emma.green@test.com',
};

let caseId;

Feature('HMCTS admin manages local authorities');

async function setupScenario(I) {
  if (!caseId) {
    caseId = await I.submitNewCaseWithData(mandatoryWithMultipleChildren);
  }
}

Scenario('HMCTS admin adds secondary local authority', async ({I, caseViewPage, manageLocalAuthoritiesEventPage, login}) => {
  await setupScenario(I);
  await login('hmctsAdminUser');
  await I.navigateToCaseDetails(caseId);

  await caseViewPage.goToNewActions(config.administrationActions.manageLocalAuthorities);
  manageLocalAuthoritiesEventPage.selectAddLocalAuthority();
  await manageLocalAuthoritiesEventPage.selectLocalAuthority(hillingdonLocalAuthority.name);
  await I.goToNextPage();

  const email = manageLocalAuthoritiesEventPage.getEmailAddress();
  assert.ok(email);
  await manageLocalAuthoritiesEventPage.setEmailAddress(hillingdonLocalAuthority.email);

  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.manageLocalAuthorities);

  caseViewPage.selectTab(caseViewPage.tabs.casePeople);

  I.seeInTab(['Applicant 1', 'Name'], swanseaLocalAuthority.name);
  I.seeTagInTab(['Applicant 1', 'Designated local authority']);

  I.seeInTab(['Applicant 2', 'Name'], hillingdonLocalAuthority.name);
  I.seeInTab(['Applicant 2', 'Group email address'], hillingdonLocalAuthority.email);
  I.dontSeeTagInTab(['Applicant 2', 'Designated local authority']);
  I.dontSeeInTab('Applicant 2', 'Colleague 1');
});

Scenario('Designated local authority solicitor can see all local authorities but updates only his own @broken-AAT', async ({I, caseViewPage, enterLocalAuthorityEventPage, login}) => {
  await setupScenario(I);
  await login('swanseaLocalAuthorityUserOne');
  await I.navigateToCaseDetails(caseId);

  caseViewPage.selectTab(caseViewPage.tabs.casePeople);

  I.seeInTab(['Applicant 1', 'Name'], swanseaLocalAuthority.name);
  I.seeTagInTab(['Applicant 1', 'Designated local authority']);

  I.seeInTab(['Applicant 2', 'Name'], hillingdonLocalAuthority.name);
  I.dontSeeTagInTab(['Applicant 2', 'Designated local authority']);

  await caseViewPage.goToNewActions(config.applicationActions.enterLocalAuthority);

  // assert.strictEqual(await enterLocalAuthorityEventPage.getLocalAuthorityName(), swanseaLocalAuthority.name); todo - fix flakyness in AAT - has fpla_test_friday27?
  assert.strictEqual(await enterLocalAuthorityEventPage.getLocalAuthorityEmail(), swanseaLocalAuthority.email);
});

Scenario('Secondary local authority solicitor can see all local authorities but updates only his own @flaky', async ({I, caseViewPage, enterLocalAuthorityEventPage, login}) => {

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
    config.applicationActions.manageLegalRepresentatives,
  ]);

  await caseViewPage.goToNewActions(config.applicationActions.enterLocalAuthority);

  assert.strictEqual(await enterLocalAuthorityEventPage.getLocalAuthorityName(), hillingdonLocalAuthority.name);
  assert.strictEqual(await enterLocalAuthorityEventPage.getLocalAuthorityEmail(), hillingdonLocalAuthority.email);

  await enterLocalAuthorityEventPage.enterDetails(hillingdonLocalAuthorityUpdates);
  await I.goToNextPage();
  await enterLocalAuthorityEventPage.enterColleague(hillingdonColleague);

  await I.seeCheckAnswersAndCompleteEvent('Save and continue');
  //I.seeEventSubmissionConfirmation(config.applicationActions.enterLocalAuthority);

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

Scenario('HMCTS admin removes secondary local authority', async ({I, caseViewPage, caseListPage, manageLocalAuthoritiesEventPage, login}) => {
  await setupScenario(I);
  await login('hmctsAdminUser');
  await I.navigateToCaseDetails(caseId);

  await caseViewPage.goToNewActions(config.administrationActions.manageLocalAuthorities);
  manageLocalAuthoritiesEventPage.selectRemoveLocalAuthority();

  await I.goToNextPage();
  I.see(hillingdonLocalAuthority.name);

  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.manageLocalAuthorities);

  caseViewPage.selectTab(caseViewPage.tabs.casePeople);

  I.seeInTab(['Applicant 1', 'Name'], swanseaLocalAuthority.name);
  I.seeTagInTab(['Applicant 1', 'Designated local authority']);

  I.dontSeeInTab(['Applicant 2', 'Name'], hillingdonLocalAuthority.name);

  await I.signIn(config.hillingdonLocalAuthorityUserOne);

  await caseListPage.verifyCaseIsNotAccessible(caseId);
});

Scenario('HMCTS admin transfer case to new local authority @broken', async ({I, caseViewPage, manageLocalAuthoritiesEventPage, login}) => {
  await setupScenario(I);
  await login('hmctsAdminUser');
  await I.navigateToCaseDetails(caseId);

  await caseViewPage.goToNewActions(config.administrationActions.manageLocalAuthorities);

  manageLocalAuthoritiesEventPage.selectTransferLocalAuthority();
  await I.goToNextPage();

  manageLocalAuthoritiesEventPage.selectLocalAuthorityToTransfer(hillingdonLocalAuthority.name);
  await I.goToNextPage();

  manageLocalAuthoritiesEventPage.fillSolicitorDetails(transferredLocalAuthoritySolicitor.name, transferredLocalAuthoritySolicitor.email);
  await I.goToNextPage();

  I.see(swanseaLocalAuthority.court);
  manageLocalAuthoritiesEventPage.selectChangeCourt();
  manageLocalAuthoritiesEventPage.selectCourt(hillingdonLocalAuthority.court);
  await I.completeEvent('Save and continue');

  //I.seeEventSubmissionConfirmation(config.administrationActions.manageLocalAuthorities);//flaky

  caseViewPage.selectTab(caseViewPage.tabs.casePeople);

  I.seeInTab(['Applicant 1', 'Name'], hillingdonLocalAuthority.name);
  I.seeTagInTab(['Applicant 1', 'Designated local authority']);
  I.seeInTab(['Applicant 1', 'Colleague 1', 'Full name'], transferredLocalAuthoritySolicitor.name);
  I.seeInTab(['Applicant 1', 'Colleague 1', 'Email address'], transferredLocalAuthoritySolicitor.email);
  I.seeTagInTab(['Applicant 1', 'Colleague 1', 'Main contact']);

  I.dontSeeInTab(['Applicant 2', 'Name']);

  caseViewPage.selectTab(caseViewPage.tabs.summary);
  I.seeInTab(['Court to issue'], hillingdonLocalAuthority.court);
});

Scenario('Old LA can not seen transferred case @broken @relies-on-previous', async ({I, caseListPage, login}) => {
  await setupScenario(I);
  await login('swanseaLocalAuthorityUserOne');
  await caseListPage.verifyCaseIsNotAccessible(caseId);
});

Scenario('HMCTS admin transfer case to secondary local authority @flaky', async ({I, caseViewPage, manageLocalAuthoritiesEventPage, login}) => {
  await setupScenario(I);
  await login('hmctsAdminUser');
  await I.navigateToCaseDetails(caseId);

  await caseViewPage.goToNewActions(config.administrationActions.manageLocalAuthorities);
  manageLocalAuthoritiesEventPage.selectAddLocalAuthority();
  await manageLocalAuthoritiesEventPage.selectLocalAuthority(swanseaLocalAuthority.name);
  await I.goToNextPage();
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.manageLocalAuthorities);

  await organisationAdminGrantAccess(config.swanseaLocalAuthorityUserOne, SHARED_LA_ROLE);

  await caseViewPage.goToNewActions(config.administrationActions.manageLocalAuthorities);

  manageLocalAuthoritiesEventPage.selectTransferLocalAuthority();
  await I.goToNextPage();

  manageLocalAuthoritiesEventPage.selectSharedLocalAuthorityToTransfer();
  I.see(`The case will be transferred to ${swanseaLocalAuthority.name}`);
  await I.goToNextPage();

  manageLocalAuthoritiesEventPage.fillSolicitorDetails(transferredLocalAuthoritySolicitor.name, transferredLocalAuthoritySolicitor.email);
  await I.goToNextPage();

  I.see(hillingdonLocalAuthority.court);
  manageLocalAuthoritiesEventPage.selectSameCourt();
  await I.completeEvent('Save and continue');

  I.seeEventSubmissionConfirmation(config.administrationActions.manageLocalAuthorities);

  caseViewPage.selectTab(caseViewPage.tabs.casePeople);

  I.seeInTab(['Applicant 1', 'Name'], swanseaLocalAuthority.name);
  I.seeTagInTab(['Applicant 1', 'Designated local authority']);
  I.seeInTab(['Applicant 1', 'Colleague 1', 'Full name'], transferredLocalAuthoritySolicitor.name);
  I.seeInTab(['Applicant 1', 'Colleague 1', 'Email address'], transferredLocalAuthoritySolicitor.email);
  I.seeTagInTab(['Applicant 1', 'Colleague 1', 'Main contact']);

  I.dontSeeInTab(['Applicant 2', 'Name']);

  caseViewPage.selectTab(caseViewPage.tabs.summary);
  I.seeInTab(['Court to issue'], hillingdonLocalAuthority.court);
});

Scenario('Original LA cannot see case when transferred to secondary LA', async ({I, caseListPage, login}) => {
  await setupScenario(I);
  await login('hillingdonLocalAuthorityUserOne');
  await caseListPage.verifyCaseIsNotAccessible(caseId);
});

Scenario('Secondary LA can see case after transfer', async ({I, login}) => {
  await setupScenario(I);
  await login('swanseaLocalAuthorityUserOne');
  I.seeCaseInSearchResult(caseId);
});

async function organisationAdminGrantAccess(user, role) {
  await api.grantCaseAccess(caseId, user, role);
}
