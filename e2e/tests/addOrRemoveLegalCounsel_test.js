const assert = require('assert');
const config = require('../config.js');
const dateFormat = require('dateformat');
const apiHelper = require('../helpers/api_helper.js');
const caseData = require('../fixtures/caseData/mandatoryWithChildSolicitorAndMultipleRespondents.json');
const legalCounsellors = require('../fixtures/legalCounsellors.js');

const solicitor1 = config.wiltshireLocalAuthorityUserOne;

let caseId;//TODO - undo
let legalCounselAdded = false;

Feature('Legal counsel');

async function setupScenario(I, caseViewPage, noticeOfChangePage, submitApplicationEventPage) {
  if (!solicitor1.details) {
    solicitor1.details = await apiHelper.getUser(solicitor1);
    solicitor1.details.organisation = 'Wiltshire County Council';
  }
  if (!caseId) {
    caseId = await I.submitNewCaseWithData(caseData);

    //Submit case
    await I.navigateToCaseDetailsAs(config.swanseaLocalAuthorityUserOne, caseId);
    await caseViewPage.goToNewActions(config.applicationActions.submitCase);
    await submitApplicationEventPage.giveConsent();
    await I.completeEvent('Submit', null, true);

    //TODO - add representative for children from another organisation, then use NoC to get access and case role to a different solicitor
    //TODO - it would be a good idea to try this manually before writing code!!!!!
    //"solicitor": {
    //   "dx": "160010 Kingsway 7",
    //   "name": "John Smith",
    //   "email": "solicitor@email.com",
    //   "mobile": "07000000000",
    //   "reference": "reference",
    //   "telephone": "00000000000"
    // }

    await I.signIn(solicitor1);

    //Solicitor completes Notice of Change - for respondent
    await noticeOfChangePage.userCompletesNoC(caseId, 'Swansea City Council', 'Alex', 'White');//TODO - maybe put this in a function
    caseViewPage.selectTab(caseViewPage.tabs.casePeople);
    assertRepresentative(I, solicitor1.details, solicitor1.details.organisation, 'Child 1');
    caseViewPage.selectTab(caseViewPage.tabs.changeOfRepresentatives);
    assertChangeOfRepresentative(I, 1, 'Notice of change', 'Alex White', solicitor1.details.email, { addedUser: solicitor1.details });

    //TODO - this will only work if child has a cafcass representative
    //Solicitor completes Notice of Change - for child
    await noticeOfChangePage.userCompletesNoC(caseId, 'Swansea City Council', 'Joe', 'Bloggs');//TODO - maybe put this in a function
    caseViewPage.selectTab(caseViewPage.tabs.casePeople);
    assertRepresentative(I, solicitor1.details, solicitor1.details.organisation, 'Respondents 1');
    caseViewPage.selectTab(caseViewPage.tabs.changeOfRepresentatives);
    assertChangeOfRepresentative(I, 1, 'Notice of change', 'Joe Bloggs', solicitor1.details.email, { addedUser: solicitor1.details });
  }
}

Scenario('Add legal counsel', async ({ I, caseViewPage, noticeOfChangePage, submitApplicationEventPage, manageLegalCounsellorsEventPage }) => {
  await setupScenario(I, caseViewPage, noticeOfChangePage, submitApplicationEventPage);

  await I.navigateToCaseDetailsAs(solicitor1, caseId);
  await caseViewPage.goToNewActions(config.applicationActions.addOrRemoveLegalCounsel);
  I.see('Add or remove legal counsel');
  I.see('Use this feature to add or remove a legal representative');
  await I.goToNextPage();
  I.see('Add or remove legal counsel');
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

Scenario('Legal counsel to be remove when child representative is removed', async ({ I, caseViewPage, enterChildrenEventPage }) => {
  checkLegalCounselWasAdded();
  await I.navigateToCaseDetailsAs(config.hmctsAdminUser, caseId);
  await caseViewPage.goToNewActions(config.administrationActions.amendChildren);

  await I.goToNextPage();
  enterChildrenEventPage.selectAnyChildHasLegalRepresentation(enterChildrenEventPage.fields().mainSolicitor.childrenHaveLegalRepresentation.options.no);
  await I.goToNextPage();
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.amendChildren);

  caseViewPage.selectTab(caseViewPage.tabs.casePeople);
  I.dontSeeInTab(['Child 1', 'Legal Counsellor']);
});

//TODO - assign a different solicitor using notice of change - NoC scenario in story

function checkLegalCounselWasAdded(){
  if (!legalCounselAdded){
    assert.fail('Cannot proceed with tests if legal counsel was not added');
  }
}

const assertRepresentative = (I, user, organisation, representedParty) => {
  I.seeInTab([representedParty, 'Representative', 'Representative\'s first name'], user.forename);
  I.seeInTab([representedParty, 'Representative', 'Representative\'s last name'], user.surname);
  I.seeInTab([representedParty, 'Representative', 'Email address'], user.email);

  if (organisation) {
    I.waitForText(organisation, 40);
    I.seeOrganisationInTab([representedParty, 'Representative', 'Name'], organisation);
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

