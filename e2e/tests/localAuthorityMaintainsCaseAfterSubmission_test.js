const config = require('../config.js');
const recipients = require('../fixtures/recipients.js');

let caseId;

Feature('Case maintenance after submission');

Before(async (I, caseViewPage, submitApplicationEventPage) => {
  if (!caseId) {
    await I.logInAndCreateCase(config.swanseaLocalAuthorityEmailUserOne, config.localAuthorityPassword);
    await I.enterMandatoryFields();
    caseViewPage.goToNewActions(config.applicationActions.submitCase);
    submitApplicationEventPage.giveConsent();
    I.continueAndSubmit();

    // eslint-disable-next-line require-atomic-updates
    caseId = await I.grabTextFrom('.heading-h1');
    console.log(`Case ${caseId} has been submitted`);
  } else {
    await I.navigateToCaseDetails(caseId);
  }
});

Scenario('local authority uploads documents', (I, caseViewPage, uploadDocumentsEventPage) => {
  caseViewPage.goToNewActions(config.applicationActions.uploadDocuments);
  uploadDocumentsEventPage.selectSocialWorkChronologyToFollow(config.testFile);
  uploadDocumentsEventPage.uploadSocialWorkStatement(config.testFile);
  uploadDocumentsEventPage.uploadSocialWorkAssessment(config.testFile);
  uploadDocumentsEventPage.uploadCarePlan(config.testFile);
  uploadDocumentsEventPage.uploadSWET(config.testFile);
  uploadDocumentsEventPage.uploadThresholdDocument(config.testFile);
  uploadDocumentsEventPage.uploadChecklistDocument(config.testFile);
  uploadDocumentsEventPage.uploadAdditionalDocuments(config.testFile);
  I.continueAndSave();
  I.seeEventSubmissionConfirmation(config.applicationActions.uploadDocuments);
  caseViewPage.selectTab(caseViewPage.tabs.documents);
  I.seeDocument('Social work chronology', '', 'To follow', 'mock reason');
  I.seeDocument('Social work statement and genogram', 'mockFile.txt', 'Attached');
  I.seeDocument('Social work assessment', 'mockFile.txt', 'Attached');
  I.seeDocument('Care plan', 'mockFile.txt', 'Attached');
  I.seeDocument('Social work evidence template (SWET)', 'mockFile.txt', 'Attached');
  I.seeDocument('Threshold document', 'mockFile.txt', 'Attached');
  I.seeDocument('Checklist document', 'mockFile.txt', 'Attached');
});

Scenario('local authority uploads court bundle', (I, uploadDocumentsEventPage, submitApplicationEventPage, caseViewPage) => {
  caseViewPage.goToNewActions(config.applicationActions.uploadDocuments);
  uploadDocumentsEventPage.uploadCourtBundle(config.testFile);
  I.continueAndSave();
  I.seeEventSubmissionConfirmation(config.applicationActions.uploadDocuments);
  caseViewPage.selectTab(caseViewPage.tabs.documents);
  I.seeDocument('Court bundle', 'mockFile.txt');
});

Scenario('local authority provides a statements of service', async (I, caseViewPage, loginPage, addStatementOfServiceEventPage) => {
  caseViewPage.goToNewActions(config.administrationActions.addStatementOfService);
  await addStatementOfServiceEventPage.enterRecipientDetails(recipients[0]);
  addStatementOfServiceEventPage.addRecipient();
  await addStatementOfServiceEventPage.enterRecipientDetails(recipients[1]);
  addStatementOfServiceEventPage.giveDeclaration();
  I.continueAndSave();
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
