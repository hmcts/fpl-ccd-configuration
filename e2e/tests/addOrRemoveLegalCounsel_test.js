const config = require('../config.js');
const dateFormat = require('dateformat');
const apiHelper = require('../helpers/api_helper.js');
const mandatoryWithMultipleRespondents = require('../fixtures/caseData/mandatoryWithMultipleRespondents.json');
const legalCounsellors = require('../fixtures/legalCounsellors.js');

const solicitor1 = config.wiltshireLocalAuthorityUserOne;

let caseId = 1628670095271359;//TODO - undo

Feature('Legal counsel');

async function setupScenario(I, caseViewPage, noticeOfChangePage, submitApplicationEventPage) {
  if (!solicitor1.details) {
    solicitor1.details = await apiHelper.getUser(solicitor1);
    solicitor1.details.organisation = 'Wiltshire County Council';
  }
  if (!caseId) {
    caseId = await I.submitNewCaseWithData(mandatoryWithMultipleRespondents);

    //Submit case
    await I.navigateToCaseDetailsAs(config.swanseaLocalAuthorityUserOne, caseId);
    await caseViewPage.goToNewActions(config.applicationActions.submitCase);
    await submitApplicationEventPage.giveConsent();
    await I.completeEvent('Submit', null, true);

    //Solicitor completes Notice of Change (which gives the a case solicitor role)
    await I.signIn(solicitor1);
    await noticeOfChangePage.userCompletesNoC(caseId, 'Swansea City Council', 'Joe', 'Bloggs');
    caseViewPage.selectTab(caseViewPage.tabs.casePeople);
    assertRepresentative(I, solicitor1.details, solicitor1.details.organisation);
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
});

//TODO - remove legal counsel - do it last - optional

Scenario('Legal counsel to be remove when respondent representative is removed', async ({ I, caseViewPage, enterRespondentsEventPage }) => {
  await I.navigateToCaseDetailsAs(config.hmctsAdminUser, caseId);
  await caseViewPage.goToNewActions(config.administrationActions.amendRespondents);

  await enterRespondentsEventPage.enterRepresentationDetails('No', {}, 0);
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.amendRespondents);

  caseViewPage.selectTab(caseViewPage.tabs.casePeople);
  I.dontSeeInTab(['Respondents 1', 'Legal Counsellor']);

  //TODO - what should we do if the representative is updated, not removed - check logic with Sam
  //the solicitor shoudl only be considered to have changed if the organisation changes

  //TODO - what happens if the new solicitor already has a legal counsel - should I assign their legal counsel to this party - this link is with a person, not an organisation
  //RESET IT
  //
  //TODO - if we do the above, I'd suggest a size bump on this story
  //TODO - write comment with the below info, implement it and explain it to Lewis
  //if solicitor changes to another organisation, grab the first counsel we find in the case for that organisation and copy it over to the party having a solicitor change
});

//TODO - remove child representative - maybe I can write the scenario where representative is updated, not removed - maybe even both?

//TODO - assign a different solicitor using notice of change

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

function assertLegalCounsellorWasAdded(caseViewPage, I) {
  caseViewPage.selectTab(caseViewPage.tabs.casePeople);
  const legalCounsellor = legalCounsellors.legalCounsellor;
  I.seeInTab(['Legal Counsellor 1', 'First name'], legalCounsellor.firstName);
  I.seeInTab(['Legal Counsellor 1', 'Last name'], legalCounsellor.lastName);
  I.seeInTab(['Legal Counsellor 1', 'Email address'], legalCounsellor.email);
  I.seeOrganisationInTab(['Respondents 1', 'Legal Counsellor 1', 'Name'], legalCounsellor.organisation);
}

