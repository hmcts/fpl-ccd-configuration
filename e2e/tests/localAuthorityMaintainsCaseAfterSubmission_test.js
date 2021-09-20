const config = require('../config.js');
const recipients = require('../fixtures/recipients.js');
const legalRepresentatives = require('../fixtures/legalRepresentatives.js');
const placementHelper = require('../helpers/placement_helper.js');
const mandatoryWithMultipleChildren = require('../fixtures/caseData/mandatoryWithMultipleChildren.json');

let caseId;

Feature('Case maintenance after submission');

async function setupScenario(I) {
  if (!caseId) { caseId = await I.submitNewCaseWithData(mandatoryWithMultipleChildren); }
  await I.navigateToCaseDetailsAs(config.swanseaLocalAuthorityUserOne, caseId);
}

Scenario('local authority add an external barrister as a legal representative for the case', async ({I, caseViewPage, manageLegalRepresentativesEventPage}) => {
  await setupScenario(I);
  await caseViewPage.goToNewActions(config.applicationActions.manageLegalRepresentatives);
  await I.goToNextPage();
  await manageLegalRepresentativesEventPage.addLegalRepresentative(legalRepresentatives.barrister);
  await I.completeEvent('Save and continue');

  I.seeEventSubmissionConfirmation(config.applicationActions.manageLegalRepresentatives);

  caseViewPage.selectTab(caseViewPage.tabs.casePeople);
  I.seeInTab(['LA Legal representatives 1', 'Full name'], legalRepresentatives.barrister.fullName);
  I.seeInTab(['LA Legal representatives 1', 'Role'], legalRepresentatives.barrister.role);
  I.seeInTab(['LA Legal representatives 1', 'Organisation'], legalRepresentatives.barrister.organisation);
  I.seeInTab(['LA Legal representatives 1', 'Email address'], legalRepresentatives.barrister.email);
  I.seeInTab(['LA Legal representatives 1', 'Phone number'], legalRepresentatives.barrister.telephone);
});

Scenario('local authority update its details', async ({I, caseViewPage, enterLocalAuthorityEventPage}) => {
  await setupScenario(I);
  const solicitorEmail = 'solicitor@test.com';

  await caseViewPage.goToNewActions(config.applicationActions.enterLocalAuthority);
  await I.goToNextPage();
  await enterLocalAuthorityEventPage.enterColleague({email: solicitorEmail}, 0);
  await I.seeCheckAnswersAndCompleteEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.applicationActions.enterLocalAuthority);

  caseViewPage.selectTab(caseViewPage.tabs.casePeople);
  I.seeInTab(['Local authority 1', 'Colleague 1', 'Email address'], solicitorEmail);
});

Scenario('local authority provides a statements of service', async ({I, caseViewPage, addStatementOfServiceEventPage}) => {
  await setupScenario(I);
  await caseViewPage.goToNewActions(config.administrationActions.addStatementOfService);
  await addStatementOfServiceEventPage.enterRecipientDetails(recipients[0]);
  await I.addAnotherElementToCollection();
  await addStatementOfServiceEventPage.enterRecipientDetails(recipients[1]);
  addStatementOfServiceEventPage.giveDeclaration();
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.addStatementOfService);
  caseViewPage.selectTab(caseViewPage.tabs.legalBasis);
  I.seeInTab(['Recipients 1', 'Name of recipient'], recipients[0].name);
  I.seeInTab(['Recipients 1', 'Do you have the recipient\'s address?'], recipients[0].addressCheck);
  I.seeInTab(['Recipient\'s address', 'Building and Street'], recipients[0].address.buildingAndStreet.lineOne);
  I.seeInTab(['Recipient\'s address', 'Address Line 2'], recipients[0].address.buildingAndStreet.lineTwo);
  I.seeInTab(['Recipient\'s address', 'Address Line 3'], recipients[0].address.buildingAndStreet.lineThree);
  I.seeInTab(['Recipient\'s address', 'Town or City'], recipients[0].address.town);
  I.seeInTab(['Recipient\'s address', 'Postcode/Zipcode'], recipients[0].address.postcode);
  I.seeInTab(['Recipient\'s address', 'Country'], recipients[0].address.country);
  I.seeInTab(['Recipients 1', 'Documents'], recipients[0].documents);
  I.seeInTab(['Recipients 1', 'Date sent'], '1 Jan 2050');
  I.seeInTab(['Recipients 1', 'Time sent'], recipients[0].timeSent);
  I.seeInTab(['Recipients 1', 'How were they sent?'], recipients[0].sentBy);
  I.seeInTab(['Recipients 1', 'Recipient\'s email address'], recipients[0].email);

  I.seeInTab(['Recipients 2', 'Name of recipient'], recipients[1].name);
  I.seeInTab(['Recipients 2', 'Do you have the recipient\'s address?'], recipients[1].addressCheck);
  I.seeInTab(['Recipient\'s address', 'Building and Street'], recipients[1].address.buildingAndStreet.lineOne);
  I.seeInTab(['Recipient\'s address', 'Address Line 2'], recipients[1].address.buildingAndStreet.lineTwo);
  I.seeInTab(['Recipient\'s address', 'Address Line 3'], recipients[1].address.buildingAndStreet.lineThree);
  I.seeInTab(['Recipient\'s address', 'Town or City'], recipients[1].address.town);
  I.seeInTab(['Recipient\'s address', 'Postcode/Zipcode'], recipients[1].address.postcode);
  I.seeInTab(['Recipient\'s address', 'Country'], recipients[1].address.country);
  I.seeInTab(['Recipients 2', 'Documents'], recipients[1].documents);
  I.seeInTab(['Recipients 2', 'Date sent'], '1 Jan 2050');
  I.seeInTab(['Recipients 2', 'Time sent'], recipients[1].timeSent);
  I.seeInTab(['Recipients 2', 'How were they sent?'], recipients[1].sentBy);
  I.seeInTab(['Recipients 2', 'Recipient\'s email address'], recipients[1].email);
});

Scenario('local authority upload placement application', async ({I, caseViewPage, placementEventPage}) => {
  await setupScenario(I);
  await caseViewPage.goToNewActions(config.administrationActions.placement);
  await placementEventPage.selectChild('Timothy Jones');
  await placementEventPage.addApplication(config.testFile);
  await placementEventPage.addSupportingDocument(0, 'Statement of facts', config.testFile);
  await placementEventPage.addConfidentialDocument(0, 'Annex B', config.testFile);
  await placementEventPage.addOrderOrNotice(0, 'Placement order', config.testPdfFile, 'test note');
  await I.completeEvent('Save and continue');

  await caseViewPage.goToNewActions(config.administrationActions.placement);
  await placementEventPage.selectChild('John Black');
  await placementEventPage.addApplication(config.testFile);
  await placementEventPage.addSupportingDocument(0, 'Other final orders', config.testFile);
  await placementEventPage.addConfidentialDocument(0, 'Other confidential documents', config.testFile);
  await I.completeEvent('Save and continue');

  caseViewPage.selectTab(caseViewPage.tabs.placement);

  I.seeInTab(['Child 1', 'Name'], 'Timothy Jones');
  I.seeInTab(['Child 1', 'Application document'], 'mockFile.txt');
  I.seeInTab(['Child 1', 'Supporting document 1', 'Document type'], 'Statement of facts');
  I.seeInTab(['Child 1', 'Supporting document 1', 'Document'], 'mockFile.txt');
  I.seeInTab(['Child 1', 'Confidential document 1', 'Document type'], 'Annex B');
  I.seeInTab(['Child 1', 'Confidential document 1', 'Document'], 'mockFile.txt');
  I.seeInTab(['Child 1', 'Order and notices 1', 'Document type'], 'Placement order');
  I.seeInTab(['Child 1', 'Order and notices 1', 'Document'], 'mockFile.pdf');
  I.seeInTab(['Child 1', 'Order and notices 1', 'Description'], 'test note');

  I.seeInTab(['Child 2', 'Name'], 'John Black');
  I.seeInTab(['Child 2', 'Application document'], 'mockFile.txt');
  I.seeInTab(['Child 2', 'Supporting document 1', 'Document type'], 'Other final orders');
  I.seeInTab(['Child 2', 'Supporting document 1', 'Document'], 'mockFile.txt');
  I.seeInTab(['Child 2', 'Confidential document 1', 'Document type'], 'Other confidential documents');
  I.seeInTab(['Child 2', 'Confidential document 1', 'Document'], 'mockFile.txt');

  await placementHelper.assertCafcassCannotSeePlacementOrder(I, caseViewPage, caseId);
});


