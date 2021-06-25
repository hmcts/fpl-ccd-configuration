const config = require('../config.js');
const mandatoryWithMaxChildren = require('../fixtures/caseData/mandatoryWithMaxChildren.json');
const apiHelper = require('../helpers/api_helper.js');
const moment = require('moment');

const unregisteredSolicitor = {
  forename: 'Rupert',
  surname: 'Bear',
  email: 'rupert@bear.com',
  unregisteredOrganisation: {
    name: 'Swansea Managing Office',
    address: {
      lookupOption: 'The Tower, Trawler Rd, Maritime Quarter, Swansea',
      buildingAndStreet: {
        lineOne: 'The Tower',
        lineTwo: 'Trawlery Rd',
        lineThree: 'Maritime Quarter',
      },
      town: 'Swansea',
      postcode: 'SA1 1JW',
      country: 'United Kingdom',
    },
  },
};

let mainSolicitor;
let alternativeSolicitor;
let caseId;

Feature('Child solicitors');

async function setupScenario(I, caseViewPage) {
  if (!caseId) {
    caseId = await I.submitNewCaseWithData(mandatoryWithMaxChildren);
    mainSolicitor = await apiHelper.getUser(config.privateSolicitorOne);
    mainSolicitor.organisation = 'Private solicitors';
    alternativeSolicitor = await apiHelper.getUser(config.hillingdonLocalAuthorityUserOne);
    alternativeSolicitor.organisation = 'Hillingdon'; // org search on aat does not like London Borough Hillingdon
  }
  await I.navigateToCaseDetailsAs(config.hmctsAdminUser, caseId);
  await caseViewPage.goToNewActions(config.administrationActions.amendChildren);
}

Scenario('HMCTS Confirm that a main solicitor in not assigned for all the children yet', async ({I, caseViewPage, enterChildrenEventPage}) => {
  await setupScenario(I, caseViewPage);

  await I.goToNextPage();
  enterChildrenEventPage.selectAnyChildHasLegalRepresentation(enterChildrenEventPage.fields().mainSolicitor.childrenHaveLegalRepresentation.options.no);
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.amendChildren);
});

Scenario('HMCTS assign a main solicitor for all the children', async ({I, caseViewPage, enterChildrenEventPage}) => {
  await setupScenario(I, caseViewPage);

  await I.goToNextPage();
  enterChildrenEventPage.selectAnyChildHasLegalRepresentation(enterChildrenEventPage.fields().mainSolicitor.childrenHaveLegalRepresentation.options.yes);
  enterChildrenEventPage.enterChildrenMainRepresentation(mainSolicitor);
  await enterChildrenEventPage.enterRegisteredOrganisation(mainSolicitor);
  await I.goToNextPage();
  enterChildrenEventPage.selectChildrenHaveSameRepresentation(enterChildrenEventPage.fields().mainSolicitor.childrenHaveSameRepresentation.options.yes);
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.amendChildren);
  caseViewPage.selectTab(caseViewPage.tabs.casePeople);
  mandatoryWithMaxChildren.caseData.children1.forEach((element, index) => assertChild(I, index + 1, element.value, mainSolicitor));
});

Scenario('HMCTS assign a different solicitor for some of the children', async ({I, caseViewPage, enterChildrenEventPage}) => {
  let children = mandatoryWithMaxChildren.caseData.children1;
  await setupScenario(I, caseViewPage);

  await I.goToNextPage();
  enterChildrenEventPage.selectAnyChildHasLegalRepresentation(enterChildrenEventPage.fields().mainSolicitor.childrenHaveLegalRepresentation.options.yes);
  enterChildrenEventPage.enterChildrenMainRepresentation(mainSolicitor);
  await enterChildrenEventPage.enterRegisteredOrganisation(mainSolicitor);
  await I.runAccessibilityTest();
  await I.goToNextPage();

  enterChildrenEventPage.selectChildrenHaveSameRepresentation(enterChildrenEventPage.fields().mainSolicitor.childrenHaveSameRepresentation.options.no);
  for (const [index, child] of children.entries()) {
    await enterChildrenEventPage.selectChildUseMainRepresentation(enterChildrenEventPage.fields(index).childSolicitor.useMainSolicitor.options.yes, index, child.value.party);
  }

  const childWithDifferentRegisteredSolicitorIdx = 2;
  const childWithUnregisteredSolicitorIdx = 3;
  await setSpecificRepresentative(enterChildrenEventPage, childWithDifferentRegisteredSolicitorIdx, children[childWithDifferentRegisteredSolicitorIdx].value.party, alternativeSolicitor);
  await setSpecificRepresentative(enterChildrenEventPage, childWithUnregisteredSolicitorIdx, children[childWithUnregisteredSolicitorIdx].value.party, unregisteredSolicitor);
  await I.runAccessibilityTest();
  await I.completeEvent('Save and continue');

  I.seeEventSubmissionConfirmation(config.administrationActions.amendChildren);
  caseViewPage.selectTab(caseViewPage.tabs.casePeople);

  for (const [index, child] of children.entries()) {
    const solicitor = index === childWithDifferentRegisteredSolicitorIdx ? alternativeSolicitor : (index === childWithUnregisteredSolicitorIdx ? unregisteredSolicitor : mainSolicitor);
    assertChild(I, index + 1, child.value, solicitor);
  }
});

async function setSpecificRepresentative(enterChildrenEventPage, idx, child, solicitor) {
  await enterChildrenEventPage.selectChildUseMainRepresentation(enterChildrenEventPage.fields(idx).childSolicitor.useMainSolicitor.options.no, idx, child);
  enterChildrenEventPage.enterChildrenSpecificRepresentation(idx, solicitor);
  if (solicitor.unregisteredOrganisation) {
    await enterChildrenEventPage.enterSpecificUnregisteredOrganisation(idx, solicitor);
  } else {
    await enterChildrenEventPage.enterSpecificRegisteredOrganisation(idx, solicitor);
  }
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
    if (solicitor.unregisteredOrganisation) {
      I.seeInTab([childElement, 'Representative', 'Organisation (unregistered)', 'Organisation name'], solicitor.unregisteredOrganisation.name);
      I.seeInTab([childElement, 'Representative', 'Organisation (unregistered)', 'Organisation address', 'Building and Street'], solicitor.unregisteredOrganisation.address.buildingAndStreet.lineOne);
      I.seeInTab([childElement, 'Representative', 'Organisation (unregistered)', 'Organisation address', 'Address Line 2'], solicitor.unregisteredOrganisation.address.buildingAndStreet.lineTwo);
      I.seeInTab([childElement, 'Representative', 'Organisation (unregistered)', 'Organisation address', 'Address Line 3'], solicitor.unregisteredOrganisation.address.buildingAndStreet.lineThree);
      I.seeInTab([childElement, 'Representative', 'Organisation (unregistered)', 'Organisation address', 'Town or City'], solicitor.unregisteredOrganisation.address.town);
      I.seeInTab([childElement, 'Representative', 'Organisation (unregistered)', 'Organisation address', 'Postcode/Zipcode'], solicitor.unregisteredOrganisation.address.postcode);
      I.seeInTab([childElement, 'Representative', 'Organisation (unregistered)', 'Organisation address', 'Country'], solicitor.unregisteredOrganisation.address.country);
    } else {
      I.waitForText(solicitor.organisation, 40);
      I.seeOrganisationInTab([childElement, 'Representative', 'Name'], solicitor.organisation);
    }
  }
}
