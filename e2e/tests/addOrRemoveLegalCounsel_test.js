const assert = require('assert');
const config = require('../config.js');
const apiHelper = require('../helpers/api_helper.js');
const caseData = require('../fixtures/caseData/mandatoryWithMultipleRespondents.json');
const legalCounsellors = require('../fixtures/legalCounsellors.js');

const solicitor1 = config.wiltshireLocalAuthorityUserOne;
const solicitor2 = config.hillingdonLocalAuthorityUserOne;

let caseId;
let legalCounselAdded = false;

Feature('Legal counsel');

async function setupScenario(I, caseViewPage, noticeOfChangePage, submitApplicationEventPage, enterChildrenEventPage) {
  if (!solicitor1.details) {
    solicitor1.details = await apiHelper.getUser(solicitor1);
    solicitor1.details.organisation = 'Wiltshire County Council';
  }
  if (!solicitor2.details) {
    solicitor2.details = await apiHelper.getUser(solicitor2);
    solicitor2.details.organisation = 'Hillingdon'; // org search on aat does not like London Borough Hillingdon
  }

  if (!caseId) {
    caseId = await I.submitNewCaseWithData(caseData);

    //Submit case
    await I.navigateToCaseDetailsAs(config.swanseaLocalAuthorityUserOne, caseId);
    await caseViewPage.goToNewActions(config.applicationActions.submitCase);
    await submitApplicationEventPage.giveConsent();
    await I.completeEvent('Submit', null, true);

    //Add child representation (required for noc)
    await addChildMainRepresentative(I, caseViewPage, enterChildrenEventPage, solicitor2);
    I.seeEventSubmissionConfirmation(config.administrationActions.amendChildren);
    caseViewPage.selectTab(caseViewPage.tabs.casePeople);
    assertChild(I, solicitor2);

    //Use NoC to change representative for respondent and child
    await I.signIn(solicitor1);

    //Solicitor completes Notice of Change - for child
    await performNoC(I, caseViewPage, noticeOfChangePage, 'Swansea City Council', 'Alex', 'White', 'Child 1', solicitor1);
    //Solicitor completes Notice of Change - for respondent
    await performNoC(I, caseViewPage, noticeOfChangePage, 'Swansea City Council', 'Joe', 'Bloggs', 'Respondents 1', solicitor1);
  }
}

Scenario('Add legal counsel', async ({ I, caseViewPage, noticeOfChangePage, submitApplicationEventPage, manageLegalCounsellorsEventPage, enterChildrenEventPage }) => {
  await setupScenario(I, caseViewPage, noticeOfChangePage, submitApplicationEventPage, enterChildrenEventPage);

  await I.navigateToCaseDetailsAs(solicitor1, caseId);
  await caseViewPage.goToNewActions(config.applicationActions.addOrRemoveLegalCounsel);
  I.waitForText('Add or remove legal counsel');
  I.waitForText('Use this feature to add or remove a legal representative');
  await I.goToNextPage();
  I.waitForText('Add or remove legal counsel');
  I.click('Add new');
  await manageLegalCounsellorsEventPage.addLegalCounsellor(legalCounsellors.legalCounsellor);
  await I.completeEvent('Save and continue');

  I.seeEventSubmissionConfirmation(config.applicationActions.addOrRemoveLegalCounsel);

  assertLegalCounsellorWasAdded(caseViewPage, I);
  legalCounselAdded = true;
});

//TODO - remove legal counsel - on add or remove legal counsel event - do it last - optional

Scenario('Legal counsel to be remove when respondent representative is removed', async ({ I, caseViewPage, enterRespondentsEventPage }) => {
  checkLegalCounselWasAdded();
  await I.navigateToCaseDetailsAs(config.hmctsAdminUser, caseId);
  await caseViewPage.goToNewActions(config.administrationActions.amendRespondents);

  await enterRespondentsEventPage.enterRepresentationDetails('No', {}, 0);
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.amendRespondents);

  caseViewPage.selectTab(caseViewPage.tabs.casePeople);
  I.dontSeeInTab(['Respondents 1', 'Legal Counsellor']);
});

Scenario('Legal counsel to be remove when child representative is updated', async ({ I, caseViewPage, enterChildrenEventPage }) => {
  checkLegalCounselWasAdded();
  await I.navigateToCaseDetailsAs(config.hmctsAdminUser, caseId);
  await addChildMainRepresentative(I, caseViewPage, enterChildrenEventPage, solicitor2);
  I.seeEventSubmissionConfirmation(config.administrationActions.amendChildren);
  caseViewPage.selectTab(caseViewPage.tabs.casePeople);
  assertChild(I, solicitor2);

  caseViewPage.selectTab(caseViewPage.tabs.casePeople);
  I.dontSeeInTab(['Child 1', 'Legal Counsellor']);
});

//TODO - assign a different solicitor using notice of change - NoC scenario in story

function checkLegalCounselWasAdded() {
  if (!legalCounselAdded) {
    assert.fail('Cannot proceed with tests if legal counsel was not added');
  }
}

async function performNoC(I, caseViewPage, noticeOfChangePage, applicantName, firstName, lastName, representedParty, solicitor) {
  await noticeOfChangePage.userCompletesNoC(caseId, applicantName, firstName, lastName);
  caseViewPage.selectTab(caseViewPage.tabs.casePeople);
  assertRepresentative(I, solicitor.details, solicitor.details.organisation, representedParty);
}

function assertRepresentative(I, user, organisation, representedParty) {
  I.seeInTab([representedParty, 'Representative', 'Representative\'s first name'], user.forename);
  I.seeInTab([representedParty, 'Representative', 'Representative\'s last name'], user.surname);
  I.seeInTab([representedParty, 'Representative', 'Email address'], user.email);

  if (organisation) {
    I.waitForText(organisation, 40);
    I.seeOrganisationInTab([representedParty, 'Representative', 'Name'], organisation);
  }
}

function assertLegalCounsellorWasAdded(caseViewPage, I) {
  caseViewPage.selectTab(caseViewPage.tabs.casePeople);
  const legalCounsellor = legalCounsellors.legalCounsellor;

  let representedParty = 'Respondents 1';
  I.seeInTab([representedParty, 'Legal Counsellor 1', 'First name'], legalCounsellor.firstName);
  I.seeInTab([representedParty, 'Legal Counsellor 1', 'Last name'], legalCounsellor.lastName);
  I.seeInTab([representedParty, 'Legal Counsellor 1', 'Email address'], legalCounsellor.email);
  I.seeOrganisationInTab([representedParty, 'Legal Counsellor 1', 'Name'], legalCounsellor.organisation);

  representedParty = 'Child 1';
  I.seeInTab([representedParty, 'Legal Counsellor 1', 'First name'], legalCounsellor.firstName);
  I.seeInTab([representedParty, 'Legal Counsellor 1', 'Last name'], legalCounsellor.lastName);
  I.seeInTab([representedParty, 'Legal Counsellor 1', 'Email address'], legalCounsellor.email);
  I.seeOrganisationInTab([representedParty, 'Legal Counsellor 1', 'Name'], legalCounsellor.organisation);
}

async function addChildMainRepresentative(I, caseViewPage, enterChildrenEventPage, solicitor) {
  await caseViewPage.goToNewActions(config.administrationActions.amendChildren);
  await I.goToNextPage();
  enterChildrenEventPage.selectAnyChildHasLegalRepresentation(enterChildrenEventPage.fields().mainSolicitor.childrenHaveLegalRepresentation.options.yes);
  enterChildrenEventPage.enterChildrenMainRepresentation(solicitor);
  await enterChildrenEventPage.enterRegisteredOrganisation(solicitor);
  await I.completeEvent('Save and continue');
}

function assertChild(I, solicitor) {
  const childElement = 'Child 1';

  I.seeInTab([childElement, 'Representative', 'Representative\'s first name'], solicitor.details.forename);
  I.seeInTab([childElement, 'Representative', 'Representative\'s last name'], solicitor.details.surname);
  I.seeInTab([childElement, 'Representative', 'Email address'], solicitor.details.email);

  I.waitForText(solicitor.details.organisation, 40);
  I.seeOrganisationInTab([childElement, 'Representative', 'Name'], solicitor.details.organisation);
}
