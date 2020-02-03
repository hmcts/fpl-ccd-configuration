const config = require('../config.js');
const recipients = require('../fixtures/recipients.js');
const uploadDocs = require('../fragments/caseDocuments');

let caseId;

Feature('Case maintenance after submission');

Before(async (I, caseViewPage, submitApplicationEventPage) => {
  if (!caseId) {
    await I.logInAndCreateCase(config.swanseaLocalAuthorityEmailUserOne, config.localAuthorityPassword);
    await I.enterMandatoryFields({multipleChildren: true});
    await caseViewPage.goToNewActions(config.applicationActions.submitCase);
    submitApplicationEventPage.giveConsent();
    await I.completeEvent('Submit');

    // eslint-disable-next-line require-atomic-updates
    caseId = await I.grabTextFrom('.heading-h1');
    console.log(`Case ${caseId} has been submitted`);
  } else {
    await I.navigateToCaseDetails(caseId);
  }
});

Scenario('local authority uploads documents', uploadDocs.uploadDocuments());

Scenario('local authority uploads court bundle', uploadDocs.uploadCourtBundle());

Scenario('local authority provides a statements of service', async (I, caseViewPage, loginPage, addStatementOfServiceEventPage) => {
  await caseViewPage.goToNewActions(config.administrationActions.addStatementOfService);
  await addStatementOfServiceEventPage.enterRecipientDetails(recipients[0]);
  await I.addAnotherElementToCollection();
  await addStatementOfServiceEventPage.enterRecipientDetails(recipients[1]);
  addStatementOfServiceEventPage.giveDeclaration();
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.addStatementOfService);
  caseViewPage.selectTab(caseViewPage.tabs.legalBasis);
  I.seeAnswerInTab(1, 'Recipients 1', 'Name of recipient', recipients[0].name);
  I.seeAnswerInTab(2, 'Recipients 1', 'Do you have the recipient\'s address?', recipients[0].addressCheck);
  I.seeAnswerInTab(1, 'Recipient\'s address', 'Building and Street', recipients[0].address.buildingAndStreet.lineOne);
  I.seeAnswerInTab(2, 'Recipient\'s address', 'Address Line 2', recipients[0].address.buildingAndStreet.lineTwo);
  I.seeAnswerInTab(3, 'Recipient\'s address', 'Address Line 3', recipients[0].address.buildingAndStreet.lineThree);
  I.seeAnswerInTab(4, 'Recipient\'s address', 'Town or City', recipients[0].address.town);
  I.seeAnswerInTab(5, 'Recipient\'s address', 'Postcode/Zipcode', recipients[0].address.postcode);
  I.seeAnswerInTab(6, 'Recipient\'s address', 'Country', recipients[0].address.country);
  I.seeAnswerInTab(4, 'Recipients 1', 'Documents', recipients[0].documents);
  I.seeAnswerInTab(5, 'Recipients 1', 'Date sent', '1 Jan 2050');
  I.seeAnswerInTab(6, 'Recipients 1', 'Time sent', recipients[0].timeSent);
  I.seeAnswerInTab(7, 'Recipients 1', 'How were they sent?', recipients[0].sentBy);
  I.seeAnswerInTab(8, 'Recipients 1', 'Recipient\'s email address', recipients[0].email);

  I.seeAnswerInTab(1, 'Recipients 2', 'Name of recipient', recipients[1].name);
  I.seeAnswerInTab(2, 'Recipients 2', 'Do you have the recipient\'s address?', recipients[1].addressCheck);
  I.seeAnswerInTab(1, 'Recipient\'s address', 'Building and Street', recipients[1].address.buildingAndStreet.lineOne);
  I.seeAnswerInTab(2, 'Recipient\'s address', 'Address Line 2', recipients[1].address.buildingAndStreet.lineTwo);
  I.seeAnswerInTab(3, 'Recipient\'s address', 'Address Line 3', recipients[1].address.buildingAndStreet.lineThree);
  I.seeAnswerInTab(4, 'Recipient\'s address', 'Town or City', recipients[1].address.town);
  I.seeAnswerInTab(5, 'Recipient\'s address', 'Postcode/Zipcode', recipients[1].address.postcode);
  I.seeAnswerInTab(6, 'Recipient\'s address', 'Country', recipients[1].address.country);
  I.seeAnswerInTab(4, 'Recipients 2', 'Documents', recipients[1].documents);
  I.seeAnswerInTab(5, 'Recipients 2', 'Date sent', '1 Jan 2050');
  I.seeAnswerInTab(6, 'Recipients 2', 'Time sent', recipients[1].timeSent);
  I.seeAnswerInTab(7, 'Recipients 2', 'How were they sent?', recipients[1].sentBy);
  I.seeAnswerInTab(8, 'Recipients 2', 'Recipient\'s email address', recipients[1].email);
});

Scenario('local authority upload placement application', async (I, caseViewPage, placementEventPage) => {
  await I.navigateToCaseDetails(caseId);

  await caseViewPage.goToNewActions(config.administrationActions.placement);
  await placementEventPage.selectChild('Timothy Jones');
  await placementEventPage.addApplication(config.testFile);
  await placementEventPage.addSupportingDocument(0, 'Statement of facts', config.testFile);
  await placementEventPage.addOrderOrNotice(0, 'Notice of hearing', config.testFile, 'test note');
  await I.completeEvent('Save and continue');

  await caseViewPage.goToNewActions(config.administrationActions.placement);
  await placementEventPage.selectChild('John Black');
  await placementEventPage.addApplication(config.testFile);
  await placementEventPage.addSupportingDocument(0, 'Other final orders', config.testFile);
  await I.completeEvent('Save and continue');

  caseViewPage.selectTab(caseViewPage.tabs.placement);

  I.seeAnswerInTab(2, 'Child 1', 'Name', 'Timothy Jones');
  I.seeAnswerInTab(3, 'Child 1', 'Application document', 'mockFile.txt');
  I.seeNestedAnswerInTab(1, 'Child 1', 'Supporting document 1', 'Document type', 'Statement of facts');
  I.seeNestedAnswerInTab(2, 'Child 1', 'Supporting document 1', 'Document', 'mockFile.txt');
  I.seeNestedAnswerInTab(1, 'Child 1', 'Order and notices 1', 'Document type', 'Notice of hearing');
  I.seeNestedAnswerInTab(2, 'Child 1', 'Order and notices 1', 'Document', 'mockFile.txt');
  I.seeNestedAnswerInTab(3, 'Child 1', 'Order and notices 1', 'Description', 'test note');

  I.seeAnswerInTab(2, 'Child 2', 'Name', 'John Black');
  I.seeAnswerInTab(3, 'Child 2', 'Application document', 'mockFile.txt');
  I.seeNestedAnswerInTab(1, 'Child 2', 'Supporting document 1', 'Document type', 'Other final orders');
  I.seeNestedAnswerInTab(2, 'Child 2', 'Supporting document 1', 'Document', 'mockFile.txt');
});
