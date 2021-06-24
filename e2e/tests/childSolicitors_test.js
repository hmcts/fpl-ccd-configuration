const config = require('../config.js');
const mandatoryWithMaxChildren = require('../fixtures/caseData/mandatoryWithMaxChildren.json');
const apiHelper = require('../helpers/api_helper.js');
const moment = require('moment');

const solicitor1 = config.privateSolicitorOne;
const solicitor2 = config.hillingdonLocalAuthorityUserOne;

let caseId;

Feature('Child solicitors');

BeforeSuite(async ({I}) => {
  caseId = await I.submitNewCaseWithData(mandatoryWithMaxChildren);
  solicitor1.details = await apiHelper.getUser(solicitor1);
  solicitor1.details.organisation = 'Private solicitors';
  solicitor2.details = await apiHelper.getUser(solicitor2);
  solicitor2.details.organisation = 'Hillingdon'; // org search on aat does not like London Borough Hillingdon
});

Before(async ({I}) => await I.navigateToCaseDetailsAs(config.hmctsAdminUser, caseId));

Scenario('HMCTS Confirm that a main solicitor in not assigned for all the children yet', async ({I, caseViewPage, enterChildrenEventPage}) => {
  await caseViewPage.goToNewActions(config.administrationActions.amendChildren);
  await I.goToNextPage();
  enterChildrenEventPage.selectAnyChildHasLegalRepresentation(enterChildrenEventPage.fields().mainSolicitor.childrenHaveLegalRepresentation.options.no);
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.amendChildren);
});

Scenario('HMCTS assign a main solicitor for all the children', async ({I, caseViewPage, enterChildrenEventPage}) => {
  await caseViewPage.goToNewActions(config.administrationActions.amendChildren);
  await I.goToNextPage();
  enterChildrenEventPage.selectAnyChildHasLegalRepresentation(enterChildrenEventPage.fields().mainSolicitor.childrenHaveLegalRepresentation.options.yes);
  enterChildrenEventPage.enterChildrenMainRepresentation(solicitor1.details);
  await enterChildrenEventPage.enterRegisteredOrganisation(solicitor1.details);
  await I.goToNextPage();
  enterChildrenEventPage.selectChildrenHaveSameRepresentation(enterChildrenEventPage.fields().mainSolicitor.childrenHaveSameRepresentation.options.yes);
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.amendChildren);
  caseViewPage.selectTab(caseViewPage.tabs.casePeople);
  mandatoryWithMaxChildren.caseData.children1.forEach((element, index) => assertChild(I, index + 1, element.value, solicitor1.details));
});

Scenario('HMCTS assign a different solicitor for some of the children', async ({I, caseViewPage, enterChildrenEventPage}) => {
  let children = mandatoryWithMaxChildren.caseData.children1;

  await caseViewPage.goToNewActions(config.administrationActions.amendChildren);
  await I.goToNextPage();
  enterChildrenEventPage.selectAnyChildHasLegalRepresentation(enterChildrenEventPage.fields().mainSolicitor.childrenHaveLegalRepresentation.options.yes);
  enterChildrenEventPage.enterChildrenMainRepresentation(solicitor1.details);
  await enterChildrenEventPage.enterRegisteredOrganisation(solicitor1.details);
  await I.goToNextPage();
  enterChildrenEventPage.selectChildrenHaveSameRepresentation(enterChildrenEventPage.fields().mainSolicitor.childrenHaveSameRepresentation.options.no);

  for (const [index, child] of children.entries()) {
    await enterChildrenEventPage.selectChildUseMainRepresentation(enterChildrenEventPage.fields(index).childSolicitor.useMainSolicitor.options.yes, index, child.value.party);
  }

  let childWithDifferentSolicitorIdx = 2;
  await setSpecificRepresentative(enterChildrenEventPage, childWithDifferentSolicitorIdx, children[childWithDifferentSolicitorIdx].value.party, solicitor2.details);
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.amendChildren);
  caseViewPage.selectTab(caseViewPage.tabs.casePeople);

  for (const [index, child] of children.entries()) {
    assertChild(I, index + 1, child.value, index === childWithDifferentSolicitorIdx ? solicitor2.details : solicitor1.details);
  }
});

async function setSpecificRepresentative(enterChildrenEventPage, idx, child, solicitor) {
  await enterChildrenEventPage.selectChildUseMainRepresentation(enterChildrenEventPage.fields(idx).childSolicitor.useMainSolicitor.options.no, idx, child);
  enterChildrenEventPage.enterChildrenSpecificRepresentation(idx, solicitor);
  await enterChildrenEventPage.enterSpecificRegisteredOrganisation(idx, solicitor);
}

function assertChild(I, idx, child, solicitor) {
  const childElement = `Child ${idx}`;

  I.seeInTab([childElement, 'Party', 'First name'], child.party.firstName);
  I.seeInTab([childElement, 'Party', 'Last name'], child.party.lastName);
  I.seeInTab([childElement, 'Party', 'Date of birth'], moment(child.party.dateOfBirth, 'YYYY-MM-DD').format('D MMM YYYY'));
  I.seeInTab([childElement, 'Party', 'Gender'], child.party.gender);

  if (solicitor) {
    I.seeInTab([childElement, 'Representative', 'Representative\'s first name'], solicitor.forename);
    I.seeInTab([childElement, 'Representative', 'Representative\'s last name'], solicitor.surname);
    I.seeInTab([childElement, 'Representative', 'Email address'], solicitor.email);
    I.waitForText(solicitor.organisation, 40);
    I.seeOrganisationInTab([childElement, 'Representative', 'Name'], solicitor.organisation);
  }
}
