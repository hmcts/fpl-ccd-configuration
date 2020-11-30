const config = require('../config.js');
const recipients = require('../fixtures/recipients.js');
const legalRepresentatives = require('../fixtures/legalRepresentatives.js');
const placementHelper = require('../helpers/placement_helper.js');
const uploadDocumentsHelper = require('../helpers/upload_case_documents_helper.js');
const mandatoryWithMultipleChildren = require('../fixtures/caseData/mandatoryWithMultipleChildren.json');
const supportingEvidenceDocuments = require('../fixtures/supportingEvidenceDocuments.js');

const dateFormat = require('dateformat');

let caseId;
let submittedAt;

Feature('Case maintenance after submission');

BeforeSuite(async ({I}) => {
  caseId = await I.submitNewCaseWithData(mandatoryWithMultipleChildren);
  await I.signIn(config.swanseaLocalAuthorityUserOne);
});

Before(async ({I}) => await I.navigateToCaseDetails(caseId));

Scenario('local authority add an external barrister as a legal representative for the case', async ({I, caseViewPage, manageLegalRepresentativesEventPage}) => {
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

Scenario('local authority adds further evidence documents and correspondence', async ({I, caseViewPage, manageDocumentsLAEventPage}) => {
  await caseViewPage.goToNewActions(config.applicationActions.manageDocumentsLA);
  await manageDocumentsLAEventPage.selectFurtherEvidence();
  await I.goToNextPage();
  await manageDocumentsLAEventPage.uploadSupportingEvidenceDocument(supportingEvidenceDocuments[0]);
  await I.addAnotherElementToCollection();
  await manageDocumentsLAEventPage.uploadSupportingEvidenceDocument(supportingEvidenceDocuments[1]);
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.applicationActions.manageDocumentsLA);

  await caseViewPage.goToNewActions(config.applicationActions.manageDocumentsLA);
  await manageDocumentsLAEventPage.selectCorrespondence();
  await I.goToNextPage();
  await manageDocumentsLAEventPage.uploadSupportingEvidenceDocument(supportingEvidenceDocuments[2]);
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.applicationActions.manageDocumentsLA);

  caseViewPage.selectTab(caseViewPage.tabs.documents);
  I.seeInTab(['Local authority further evidence documents 1', 'Document name'], 'Email to say evidence will be late');
  I.seeInTab(['Local authority further evidence documents 1', 'Notes'], 'Evidence will be late');
  I.seeInTab(['Local authority further evidence documents 1', 'Date and time received'], '1 Jan 2020, 11:00:00 AM');
  I.seeInTab(['Local authority further evidence documents 1', 'Date and time uploaded'], dateFormat(submittedAt, 'd mmm yyyy'));
  I.seeInTab(['Local authority further evidence documents 1', 'File'], 'mockFile.txt');
  I.seeTextInTab(['Local authority further evidence documents 1', 'Uploaded by']);
  I.seeInTab(['Local authority further evidence documents 2', 'Document name'], 'Email with evidence attached');
  I.seeInTab(['Local authority further evidence documents 2', 'Notes'], 'Case evidence included');
  I.seeInTab(['Local authority further evidence documents 2', 'Date and time received'], '1 Jan 2020, 11:00:00 AM');
  I.seeInTab(['Local authority further evidence documents 2', 'Date and time uploaded'], dateFormat(submittedAt, 'd mmm yyyy'));
  I.seeInTab(['Local authority further evidence documents 2', 'File'], 'mockFile.txt');
  I.seeTextInTab(['Local authority further evidence documents 2', 'Uploaded by']);

  caseViewPage.selectTab(caseViewPage.tabs.correspondence);
  I.seeInTab(['Local authority correspondence documents 1', 'Document name'], 'Correspondence document');
  I.seeInTab(['Local authority correspondence documents 1', 'Notes'], 'Test notes');
  I.seeInTab(['Local authority correspondence documents 1', 'Date and time received'], '2 Feb 2020, 11:00:00 AM');
  I.seeInTab(['Local authority correspondence documents 1', 'Date and time uploaded'], dateFormat(submittedAt, 'd mmm yyyy'));
  I.seeInTab(['Local authority correspondence documents 1', 'File'], 'mockFile.txt');
  I.seeTextInTab(['Local authority correspondence documents 1', 'Uploaded by']);
});

Scenario('local authority uploads documents when SWET not required', async ({I, caseViewPage, uploadDocumentsEventPage}) => {
  await caseViewPage.goToNewActions(config.applicationActions.uploadDocuments);
  uploadDocumentsHelper.uploadCaseDocuments(uploadDocumentsEventPage, false);
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.applicationActions.uploadDocuments);
  caseViewPage.selectTab(caseViewPage.tabs.documents);
  uploadDocumentsHelper.assertCaseDocuments(I, false);
});

Scenario('local authority uploads court bundle', async ({I, caseViewPage, uploadDocumentsEventPage}) => {
  await caseViewPage.goToNewActions(config.applicationActions.uploadDocuments);
  uploadDocumentsEventPage.uploadCourtBundle(config.testFile);
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.applicationActions.uploadDocuments);
  caseViewPage.selectTab(caseViewPage.tabs.documents);
  I.seeDocument('Court bundle', 'mockFile.txt', '', '', 'Date and time uploaded', 'Uploaded by');
});

Scenario('local authority update solicitor', async ({I, caseViewPage, enterApplicantEventPage}) => {
  const solicitorEmail = 'solicitor@test.com';
  await caseViewPage.goToNewActions(config.applicationActions.enterApplicant);
  enterApplicantEventPage.enterSolicitorDetails({email: solicitorEmail});
  await I.seeCheckAnswersAndCompleteEvent('Save and continue');

  caseViewPage.selectTab(caseViewPage.tabs.casePeople);
  I.seeInTab(['Solicitor', 'Solicitor\'s email'], solicitorEmail);
});

Scenario('local authority provides a statements of service', async ({I, caseViewPage, addStatementOfServiceEventPage}) => {
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


