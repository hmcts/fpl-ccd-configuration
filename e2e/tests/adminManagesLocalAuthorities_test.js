const config = require('../config.js');
const api = require('../helpers/api_helper');
const assert = require('assert');

const mandatoryWithMultipleChildren = require('../fixtures/caseData/mandatoryWithMultipleChildren.json');
const designatedLocalAuthority = mandatoryWithMultipleChildren.caseData.localAuthorities[0].value;
const secondaryLocalAuthorityName = 'London Borough Hillingdon';
const secondaryLocalAuthorityEmail = 'hilindon@test.com';

let caseId;

Feature('HMCTS admin manages local authorities');

async function setupScenario(I) {
  if (!caseId) {
    caseId = await I.submitNewCaseWithData(mandatoryWithMultipleChildren);
  }
  await I.navigateToCaseDetailsAs(config.hmctsAdminUser, caseId);
}

Scenario('HMCTS admin adds secondary local authority', async ({I, caseViewPage, manageLocalAuthoritiesEventPage}) => {

  await setupScenario(I);
  await caseViewPage.goToNewActions(config.administrationActions.manageLocalAuthorities);
  manageLocalAuthoritiesEventPage.selectAddLocalAuthority();
  manageLocalAuthoritiesEventPage.selectLocalAuthority(secondaryLocalAuthorityName);
  await I.goToNextPage();

  const email = manageLocalAuthoritiesEventPage.getEmailAddress();
  assert.ok(email);
  manageLocalAuthoritiesEventPage.setEmailAddress(secondaryLocalAuthorityEmail);

  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.manageLocalAuthorities);

  caseViewPage.selectTab(caseViewPage.tabs.casePeople);

  I.seeInTab(['Local authority 1', 'Name'], designatedLocalAuthority.name);
  I.seeTagInTab(['Local authority 1', 'Designated local authority']);

  I.seeInTab(['Local authority 2', 'Name'], secondaryLocalAuthorityName);
  I.seeInTab(['Local authority 2', 'Group email address'], secondaryLocalAuthorityEmail);
  I.dontSeeTagInTab(['Local authority 2', 'Designated local authority']);
  I.dontSeeInTab('Local authority 2', 'Colleague 1');
});

Scenario('Designated local authority solicitor can see all local authorities but updates only his own', async ({I, caseViewPage, enterLocalAuthorityEventPage}) => {

  await setupScenario(I);
  await I.navigateToCaseDetailsAs(config.swanseaLocalAuthorityUserOne, caseId);

  caseViewPage.selectTab(caseViewPage.tabs.casePeople);

  I.seeInTab(['Local authority 1', 'Name'], designatedLocalAuthority.name);
  I.seeTagInTab(['Local authority 1', 'Designated local authority']);

  I.seeInTab(['Local authority 2', 'Name'], secondaryLocalAuthorityName);
  I.dontSeeTagInTab(['Local authority 2', 'Designated local authority']);

  await caseViewPage.goToNewActions(config.applicationActions.enterLocalAuthority);

  assert.strictEqual(await enterLocalAuthorityEventPage.getLocalAuthorityName(), designatedLocalAuthority.name);
  assert.strictEqual(await enterLocalAuthorityEventPage.getLocalAuthorityEmail(), designatedLocalAuthority.email);
});

Scenario('Secondary local authority solicitor can see all local authorities but updates only his own', async ({I, caseViewPage, enterLocalAuthorityEventPage}) => {

  const secondaryOrganisationUpdates = {
    pbaNumber: '1234567',
    phone: '777777',
  };

  const secondaryOrganisationColleague = {
    role: 'Solicitor',
    fullName: 'Alex Brown',
    email: 'alex@test.com',
    notificationRecipient: 'Yes',
  };

  await setupScenario(I);

  await api.grantCaseAccess(caseId, config.hillingdonLocalAuthorityUserOne, '[LASHARED]');

  await I.navigateToCaseDetailsAs(config.hillingdonLocalAuthorityUserOne, caseId);

  await I.seeAvailableEvents([
    config.applicationActions.enterLocalAuthority,
    config.administrationActions.uploadAdditionalApplications,
    config.administrationActions.manageDocuments,
  ]);

  await caseViewPage.goToNewActions(config.applicationActions.enterLocalAuthority);

  assert.strictEqual(await enterLocalAuthorityEventPage.getLocalAuthorityName(), secondaryLocalAuthorityName);
  assert.strictEqual(await enterLocalAuthorityEventPage.getLocalAuthorityEmail(), secondaryLocalAuthorityEmail);

  await enterLocalAuthorityEventPage.enterDetails(secondaryOrganisationUpdates);
  await I.goToNextPage();
  await enterLocalAuthorityEventPage.enterColleague(secondaryOrganisationColleague);

  await I.seeCheckAnswersAndCompleteEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.applicationActions.enterLocalAuthority);

  caseViewPage.selectTab(caseViewPage.tabs.casePeople);

  I.seeInTab(['Local authority 1', 'Name'], designatedLocalAuthority.name);
  I.seeTagInTab(['Local authority 1', 'Designated local authority']);
  I.seeInTab(['Local authority 1', 'Colleague 1', 'Full name'], designatedLocalAuthority.colleagues[0].value.fullName);
  I.seeTagInTab(['Local authority 1', 'Colleague 1', 'Main contact']);

  I.seeInTab(['Local authority 2', 'Name'], secondaryLocalAuthorityName);
  I.seeInTab(['Local authority 2', 'Group email address'], secondaryLocalAuthorityEmail);
  I.seeInTab(['Local authority 2', 'PBA number'], `PBA${secondaryOrganisationUpdates.pbaNumber}`);
  I.seeInTab(['Local authority 2', 'Phone number'], secondaryOrganisationUpdates.phone);
  I.dontSeeTagInTab(['Local authority 2', 'Designated local authority']);

  I.seeInTab(['Local authority 2', 'Colleague 1', 'Role'], secondaryOrganisationColleague.role);
  I.seeInTab(['Local authority 2', 'Colleague 1', 'Full name'], secondaryOrganisationColleague.fullName);
  I.seeInTab(['Local authority 2', 'Colleague 1', 'Email address'], secondaryOrganisationColleague.email);
  I.seeInTab(['Local authority 2', 'Colleague 1', 'Send them case update notifications?'], secondaryOrganisationColleague.notificationRecipient);
  I.seeTagInTab(['Local authority 2', 'Colleague 1', 'Main contact']);
});

Scenario('HMCTS admin removes secondary local authority', async ({I, caseViewPage, caseListPage, manageLocalAuthoritiesEventPage}) => {

  await setupScenario(I);

  await caseViewPage.goToNewActions(config.administrationActions.manageLocalAuthorities);
  manageLocalAuthoritiesEventPage.selectRemoveLocalAuthority();

  await I.goToNextPage();
  I.see(secondaryLocalAuthorityName);

  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.manageLocalAuthorities);

  caseViewPage.selectTab(caseViewPage.tabs.casePeople);

  I.seeInTab(['Local authority 1', 'Name'], designatedLocalAuthority.name);
  I.seeTagInTab(['Local authority 1', 'Designated local authority']);

  I.dontSeeInTab(['Local authority 2', 'Name'], secondaryLocalAuthorityName);

  await I.signIn(config.hillingdonLocalAuthorityUserOne);

  caseListPage.verifyCaseIsNotAccessible(caseId);
});
