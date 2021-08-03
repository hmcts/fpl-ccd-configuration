const config = require('../config.js');
const dateFormat = require('dateformat');
const apiHelper = require('../helpers/api_helper.js');
const mandatoryWithMultipleRespondents = require('../fixtures/caseData/mandatoryWithMultipleRespondents.json');
const legalCounsellors = require('../fixtures/legalCounsellors.js');

const solicitor1 = config.privateSolicitorOne;

let caseId;

Feature('Legal counsel');

async function setupScenario(I, caseViewPage, noticeOfChangePage, submitApplicationEventPage) {
  if (!solicitor1.details) {
    solicitor1.details = await apiHelper.getUser(solicitor1);
    solicitor1.details.organisation = 'Private solicitors';
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
    assertRepresentative(I, solicitor1.details, 'Private solicitors');
    caseViewPage.selectTab(caseViewPage.tabs.changeOfRepresentatives);
    assertChangeOfRepresentative(I, 1, 'Notice of change', 'Joe Bloggs', solicitor1.details.email, { addedUser: solicitor1.details });
  } else {
    await I.navigateToCaseDetailsAs(solicitor1, caseId);
  }
}

Scenario('Add legal counsellor', async ({ I, caseViewPage, noticeOfChangePage, submitApplicationEventPage, manageLegalCounsellorsEventPage }) => {
  await setupScenario(I, caseViewPage, noticeOfChangePage, submitApplicationEventPage);

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
  const LEGAL_COUNSELLOR_ORGANISATION_NAME_XPATH = `//mat-tab-body//*[@class="complex-panel" and .//*[@class="complex-panel-title" and .//*[text()="Respondents 1"]]]
    //*[@class="complex-panel" and .//*[@class="complex-panel-title" and .//*[text()="Legal Counsellor 1"]]]
    //*[@class="complex-panel" and .//*[@class="complex-panel-title" and .//*[text()="Organisation"]]]
    //*[contains(@class,"complex-panel-compound-field") and .//td/span[text()="Name:"]]`;

  caseViewPage.selectTab(caseViewPage.tabs.casePeople);
  const legalCounsellor = legalCounsellors.legalCounsellor;
  I.seeInTab(['Legal Counsellor 1', 'First name'], legalCounsellor.firstName);
  I.seeInTab(['Legal Counsellor 1', 'Last name'], legalCounsellor.lastName);
  I.seeInTab(['Legal Counsellor 1', 'Email address'], legalCounsellor.email);
  I.seeInTab(['Legal Counsellor 1', 'Phone number'], legalCounsellor.telephone);
  I.see(legalCounsellor.organisation, LEGAL_COUNSELLOR_ORGANISATION_NAME_XPATH);
}

