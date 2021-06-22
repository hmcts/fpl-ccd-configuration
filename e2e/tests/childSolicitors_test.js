const config = require('../config.js');
const mandatoryWithMaxChildren = require('../fixtures/caseData/mandatoryWithMaxChildren.json');
const apiHelper = require('../helpers/api_helper.js');
const moment = require('moment');

const solicitor1 = config.privateSolicitorOne;

let caseId;

Feature('Child solicitors');

BeforeSuite(async ({I}) => {
  caseId = await I.submitNewCaseWithData(mandatoryWithMaxChildren);
  solicitor1.details = await apiHelper.getUser(solicitor1);
  solicitor1.details.organisation = 'Private solicitors';
});

Before(async ({I}) => await I.navigateToCaseDetailsAs(config.hmctsAdminUser, caseId));

Scenario('HMCTS Confirm that a main solicitor in not assigned for all the children yet', async ({I, caseViewPage, enterChildrenEventPage}) => {
  await caseViewPage.goToNewActions(config.administrationActions.amendChildren);
  await I.goToNextPage();
  await enterChildrenEventPage.selectAnyChildHasLegalRepresentation(enterChildrenEventPage.fields().mainSolicitor.childrenHaveLegalRepresentation.options.no);
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.amendChildren);
});

Scenario('HMCTS assign a main solicitor for all the children', async ({I, caseViewPage, enterChildrenEventPage}) => {
  await caseViewPage.goToNewActions(config.administrationActions.amendChildren);
  await I.goToNextPage();
  await enterChildrenEventPage.selectAnyChildHasLegalRepresentation(enterChildrenEventPage.fields().mainSolicitor.childrenHaveLegalRepresentation.options.yes);
  await enterChildrenEventPage.enterChildrenMainRepresentation(solicitor1.details);
  await enterChildrenEventPage.enterRegisteredOrganisation(solicitor1.details);
  await I.goToNextPage();
  await enterChildrenEventPage.selectChildrenHaveSameRepresentation(enterChildrenEventPage.fields().mainSolicitor.childrenHaveSameRepresentation.options.yes);
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.amendChildren);
  caseViewPage.selectTab(caseViewPage.tabs.casePeople);
  mandatoryWithMaxChildren.caseData.children1.forEach((element, index) => assertChild(I, index+1, element.value, solicitor1.details));
});

function assertChild(I, idx, child, solicitor) {
  const childElement = `Child ${idx}`;

  I.seeInTab([childElement, 'Party', 'First name'], child.party.firstName);
  I.seeInTab([childElement, 'Party', 'Last name'], child.party.lastName);
  I.seeInTab([childElement, 'Party', 'Date of birth'], moment(child.party.dateOfBirth, 'YYYY-MM-DD').format('D MMM YYYY'));
  I.seeInTab([childElement, 'Party', 'Gender'], child.party.gender);

  if(solicitor) {
    I.seeInTab([childElement, 'Representative', 'Representative\'s first name'], solicitor.forename);
    I.seeInTab([childElement, 'Representative', 'Representative\'s last name'], solicitor.surname);
    I.seeInTab([childElement, 'Representative', 'Email address'], solicitor.email);
    I.waitForText(solicitor.organisation, 40);
    I.seeOrganisationInTab([childElement, 'Representative', 'Name'], solicitor.organisation);
  }
}



