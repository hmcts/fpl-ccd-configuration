const config = require('../config.js');
const mandatoryWithMultipleChildren = require('../fixtures/caseData/mandatoryWithMultipleChildren.json');
const supportingEvidenceDocuments = require('../fixtures/supportingEvidenceDocuments.js');
const manageDocumentsForLAHelper = require('../helpers/manage_documents_for_LA_helper.js');

const dateFormat = require('dateformat');

let caseId;
let submittedAt;

Feature('Manage documents');

BeforeSuite(async ({I}) => {
  caseId = await I.submitNewCaseWithData(mandatoryWithMultipleChildren);
});

Scenario('HMCTS Admin and LA upload confidential and non confidential further evidence documents', async ({I, caseViewPage, manageDocumentsEventPage, manageDocumentsLAEventPage}) => {
  await I.navigateToCaseDetailsAs(config.hmctsAdminUser, caseId);
  await caseViewPage.goToNewActions(config.applicationActions.manageDocumentsLA);

  manageDocumentsEventPage.selectFurtherEvidence();
  await I.goToNextPage();
  await manageDocumentsEventPage.uploadConfidentialSupportingEvidenceDocument(supportingEvidenceDocuments[0]);
  await I.addAnotherElementToCollection();
  await manageDocumentsEventPage.uploadSupportingEvidenceDocument(supportingEvidenceDocuments[1]);
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.applicationActions.manageDocumentsLA);

  await I.navigateToCaseDetailsAs(config.swanseaLocalAuthorityUserOne, caseId);
  await caseViewPage.goToNewActions(config.applicationActions.manageDocumentsLA);

  await manageDocumentsLAEventPage.selectFurtherEvidence();
  await I.goToNextPage();
  await manageDocumentsLAEventPage.uploadConfidentialSupportingEvidenceDocument(supportingEvidenceDocuments[2]);
  await I.addAnotherElementToCollection();
  await manageDocumentsLAEventPage.uploadSupportingEvidenceDocument(supportingEvidenceDocuments[3]);
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.applicationActions.manageDocumentsLA);

  caseViewPage.selectTab(caseViewPage.tabs.documents);

  I.dontSeeInTab(['Email to say evidence will be late']);
  I.seeInTab(['HMCTS further evidence documents 1', 'Document name'], 'Email with evidence attached');
  I.seeInTab(['HMCTS further evidence documents 1', 'Notes'], 'Case evidence included');
  I.seeInTab(['HMCTS further evidence documents 1', 'Date and time uploaded'], dateFormat(submittedAt, 'd mmm yyyy'));
  I.seeInTab(['HMCTS further evidence documents 1', 'File'], 'mockFile.txt');
  I.seeTextInTab(['HMCTS further evidence documents 1', 'Uploaded by']);

  I.seeInTab(['Local authority further evidence documents 1', 'Document name'], 'Correspondence document');
  I.seeInTab(['Local authority further evidence documents 1', 'Notes'], 'Test notes');
  I.seeInTab(['Local authority further evidence documents 1', 'Date and time uploaded'], dateFormat(submittedAt, 'd mmm yyyy'));
  I.seeInTab(['Local authority further evidence documents 1', 'File'], 'mockFile.txt');
  I.seeTextInTab(['Local authority further evidence documents 1', 'Uploaded by']);
  I.seeTextInTab(['Local authority further evidence documents 1', ''], 'Confidential');
  I.seeInTab(['Local authority further evidence documents 2', 'Document name'], 'C2 supporting document');
  I.seeInTab(['Local authority further evidence documents 2', 'Notes'], 'Supports the C2 application');
  I.seeInTab(['Local authority further evidence documents 2', 'Date and time uploaded'], dateFormat(submittedAt, 'd mmm yyyy'));
  I.seeInTab(['Local authority further evidence documents 2', 'File'], 'mockFile.txt');
  I.seeTextInTab(['Local authority further evidence documents 2', 'Uploaded by']);

  await I.navigateToCaseDetailsAs(config.hmctsAdminUser, caseId);
  caseViewPage.selectTab(caseViewPage.tabs.documents);

  I.seeInTab(['HMCTS further evidence documents 1', 'Document name'], 'Email to say evidence will be late');
  I.seeInTab(['HMCTS further evidence documents 1', 'Notes'], 'Evidence will be late');
  I.seeInTab(['HMCTS further evidence documents 1', 'Date and time uploaded'], dateFormat(submittedAt, 'd mmm yyyy'));
  I.seeInTab(['HMCTS further evidence documents 1', 'File'], 'mockFile.txt');
  I.seeTextInTab(['HMCTS further evidence documents 1', 'Uploaded by']);
  I.seeTextInTab(['HMCTS further evidence documents 1', ''], 'Confidential');
  I.seeInTab(['HMCTS further evidence documents 2', 'Document name'], 'Email with evidence attached');
  I.seeInTab(['HMCTS further evidence documents 2', 'Notes'], 'Case evidence included');
  I.seeInTab(['HMCTS further evidence documents 2', 'Date and time uploaded'], dateFormat(submittedAt, 'd mmm yyyy'));
  I.seeInTab(['HMCTS further evidence documents 2', 'File'], 'mockFile.txt');
  I.seeTextInTab(['HMCTS further evidence documents 2', 'Uploaded by']);

  I.seeInTab(['Local authority further evidence documents 1', 'Document name'], 'Correspondence document');
  I.seeInTab(['Local authority further evidence documents 1', 'Notes'], 'Test notes');
  I.seeInTab(['Local authority further evidence documents 1', 'Date and time uploaded'], dateFormat(submittedAt, 'd mmm yyyy'));
  I.seeInTab(['Local authority further evidence documents 1', 'File'], 'mockFile.txt');
  I.seeTextInTab(['Local authority further evidence documents 1', 'Uploaded by']);
  I.seeTextInTab(['Local authority further evidence documents 1', ''], 'Confidential');
  I.seeInTab(['Local authority further evidence documents 2', 'Document name'], 'C2 supporting document');
  I.seeInTab(['Local authority further evidence documents 2', 'Notes'], 'Supports the C2 application');
  I.seeInTab(['Local authority further evidence documents 2', 'Date and time uploaded'], dateFormat(submittedAt, 'd mmm yyyy'));
  I.seeInTab(['Local authority further evidence documents 2', 'File'], 'mockFile.txt');
  I.seeTextInTab(['Local authority further evidence documents 2', 'Uploaded by']);
});

Scenario('HMCTS Admin and LA upload confidential and non confidential correspondence documents', async ({I, caseViewPage, manageDocumentsEventPage, manageDocumentsLAEventPage}) => {
  await I.navigateToCaseDetailsAs(config.hmctsAdminUser, caseId);
  await caseViewPage.goToNewActions(config.applicationActions.manageDocumentsLA);

  manageDocumentsEventPage.selectCorrespondence();
  await I.goToNextPage();
  await manageDocumentsEventPage.uploadConfidentialSupportingEvidenceDocument(supportingEvidenceDocuments[0]);
  await I.addAnotherElementToCollection();
  await manageDocumentsEventPage.uploadSupportingEvidenceDocument(supportingEvidenceDocuments[1]);
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.applicationActions.manageDocumentsLA);

  await I.navigateToCaseDetailsAs(config.swanseaLocalAuthorityUserOne, caseId);
  await caseViewPage.goToNewActions(config.applicationActions.manageDocumentsLA);

  await manageDocumentsLAEventPage.selectCorrespondence();
  await I.goToNextPage();
  await manageDocumentsLAEventPage.uploadConfidentialSupportingEvidenceDocument(supportingEvidenceDocuments[2]);
  await I.addAnotherElementToCollection();
  await manageDocumentsLAEventPage.uploadSupportingEvidenceDocument(supportingEvidenceDocuments[3]);
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.applicationActions.manageDocumentsLA);

  caseViewPage.selectTab(caseViewPage.tabs.correspondence);

  I.dontSeeInTab(['Email to say evidence will be late']);
  I.seeInTab(['Correspondence uploaded by HMCTS 1', 'Document name'], 'Email with evidence attached');
  I.seeInTab(['Correspondence uploaded by HMCTS 1', 'Notes'], 'Case evidence included');
  I.seeInTab(['Correspondence uploaded by HMCTS 1', 'Date and time uploaded'], dateFormat(submittedAt, 'd mmm yyyy'));
  I.seeInTab(['Correspondence uploaded by HMCTS 1', 'File'], 'mockFile.txt');
  I.seeTextInTab(['Correspondence uploaded by HMCTS 1', 'Uploaded by']);

  I.seeInTab(['Correspondence uploaded by local authority 1', 'Document name'], 'Correspondence document');
  I.seeInTab(['Correspondence uploaded by local authority 1', 'Notes'], 'Test notes');
  I.seeInTab(['Correspondence uploaded by local authority 1', 'Date and time uploaded'], dateFormat(submittedAt, 'd mmm yyyy'));
  I.seeInTab(['Correspondence uploaded by local authority 1', 'File'], 'mockFile.txt');
  I.seeTextInTab(['Correspondence uploaded by local authority 1', 'Uploaded by']);
  I.seeTextInTab(['Correspondence uploaded by local authority 1', ''], 'Confidential');
  I.seeInTab(['Correspondence uploaded by local authority 2', 'Document name'], 'C2 supporting document');
  I.seeInTab(['Correspondence uploaded by local authority 2', 'Notes'], 'Supports the C2 application');
  I.seeInTab(['Correspondence uploaded by local authority 2', 'Date and time uploaded'], dateFormat(submittedAt, 'd mmm yyyy'));
  I.seeInTab(['Correspondence uploaded by local authority 2', 'File'], 'mockFile.txt');
  I.seeTextInTab(['Correspondence uploaded by local authority 2', 'Uploaded by']);

  await I.navigateToCaseDetailsAs(config.hmctsAdminUser, caseId);
  caseViewPage.selectTab(caseViewPage.tabs.correspondence);

  I.seeInTab(['Correspondence uploaded by HMCTS 1', 'Document name'], 'Email to say evidence will be late');
  I.seeInTab(['Correspondence uploaded by HMCTS 1', 'Notes'], 'Evidence will be late');
  I.seeInTab(['Correspondence uploaded by HMCTS 1', 'Date and time uploaded'], dateFormat(submittedAt, 'd mmm yyyy'));
  I.seeInTab(['Correspondence uploaded by HMCTS 1', 'File'], 'mockFile.txt');
  I.seeTextInTab(['Correspondence uploaded by HMCTS 1', 'Uploaded by']);
  I.seeTextInTab(['Correspondence uploaded by HMCTS 1', ''], 'Confidential');
  I.seeInTab(['Correspondence uploaded by HMCTS 2', 'Document name'], 'Email with evidence attached');
  I.seeInTab(['Correspondence uploaded by HMCTS 2', 'Notes'], 'Case evidence included');
  I.seeInTab(['Correspondence uploaded by HMCTS 2', 'Date and time uploaded'], dateFormat(submittedAt, 'd mmm yyyy'));
  I.seeInTab(['Correspondence uploaded by HMCTS 2', 'File'], 'mockFile.txt');
  I.seeTextInTab(['Correspondence uploaded by HMCTS 2', 'Uploaded by']);

  I.seeInTab(['Correspondence uploaded by local authority 1', 'Document name'], 'Correspondence document');
  I.seeInTab(['Correspondence uploaded by local authority 1', 'Notes'], 'Test notes');
  I.seeInTab(['Correspondence uploaded by local authority 1', 'Date and time uploaded'], dateFormat(submittedAt, 'd mmm yyyy'));
  I.seeInTab(['Correspondence uploaded by local authority 1', 'File'], 'mockFile.txt');
  I.seeTextInTab(['Correspondence uploaded by local authority 1', 'Uploaded by']);
  I.seeTextInTab(['Correspondence uploaded by local authority 1', ''], 'Confidential');
  I.seeInTab(['Correspondence uploaded by local authority 2', 'Document name'], 'C2 supporting document');
  I.seeInTab(['Correspondence uploaded by local authority 2', 'Notes'], 'Supports the C2 application');
  I.seeInTab(['Correspondence uploaded by local authority 2', 'Date and time uploaded'], dateFormat(submittedAt, 'd mmm yyyy'));
  I.seeInTab(['Correspondence uploaded by local authority 2', 'File'], 'mockFile.txt');
  I.seeTextInTab(['Correspondence uploaded by local authority 2', 'Uploaded by']);
});

Scenario('HMCTS Admin and LA upload confidential C2 supporting documents', async ({I, caseViewPage, manageDocumentsEventPage, manageDocumentsLAEventPage, uploadC2DocumentsEventPage}) => {
  await I.navigateToCaseDetailsAs(config.hmctsAdminUser, caseId);
  await manageDocumentsForLAHelper.uploadC2(I, caseViewPage, uploadC2DocumentsEventPage);
  await caseViewPage.goToNewActions(config.applicationActions.manageDocumentsLA);

  manageDocumentsEventPage.selectC2SupportingDocuments();
  await manageDocumentsEventPage.selectC2FromDropdown();
  await I.goToNextPage();
  await manageDocumentsEventPage.uploadConfidentialSupportingEvidenceDocument(supportingEvidenceDocuments[0]);
  await I.addAnotherElementToCollection();
  await manageDocumentsEventPage.uploadSupportingEvidenceDocument(supportingEvidenceDocuments[1]);
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.applicationActions.manageDocumentsLA);

  caseViewPage.selectTab(caseViewPage.tabs.c2);

  I.seeInTab(['C2 Application 1', 'C2 supporting documents 1', 'Document name'], 'Email to say evidence will be late');
  I.seeInTab(['C2 Application 1', 'C2 supporting documents 1', 'Notes'], 'Evidence will be late');
  I.seeInTab(['C2 Application 1', 'C2 supporting documents 1', 'File'], 'mockFile.txt');
  I.seeInTab(['C2 Application 1', 'C2 supporting documents 1', 'Date and time uploaded'], dateFormat(submittedAt, 'd mmm yyyy'));
  I.seeTextInTab(['C2 Application 1', 'C2 supporting documents 1', ''], 'Confidential');
  I.seeTextInTab(['C2 Application 1', 'C2 supporting documents 1', 'Uploaded by']);

  I.seeInTab(['C2 Application 1', 'C2 supporting documents 2', 'Document name'], 'Email with evidence attached');
  I.seeInTab(['C2 Application 1', 'C2 supporting documents 2', 'Notes'], 'Case evidence included');
  I.seeInTab(['C2 Application 1', 'C2 supporting documents 2', 'File'], 'mockFile.txt');
  I.seeInTab(['C2 Application 1', 'C2 supporting documents 2', 'Date and time uploaded'], dateFormat(submittedAt, 'd mmm yyyy'));
  I.seeTextInTab(['C2 Application 1', 'C2 supporting documents 2', 'Uploaded by']);

  await I.navigateToCaseDetailsAs(config.swanseaLocalAuthorityUserOne, caseId);
  caseViewPage.selectTab(caseViewPage.tabs.c2);

  I.dontSeeInTab(['Email to say evidence will be late']);
  I.seeInTab(['C2 Application 1', 'C2 supporting documents 1', 'Document name'], 'Email with evidence attached');
  I.seeInTab(['C2 Application 1', 'C2 supporting documents 1', 'Notes'], 'Case evidence included');
  I.seeInTab(['C2 Application 1', 'C2 supporting documents 1', 'File'], 'mockFile.txt');
  I.seeInTab(['C2 Application 1', 'C2 supporting documents 1', 'Date and time uploaded'], dateFormat(submittedAt, 'd mmm yyyy'));
  I.seeTextInTab(['C2 Application 1', 'C2 supporting documents 1', 'Uploaded by']);


  await caseViewPage.goToNewActions(config.applicationActions.manageDocumentsLA);

  await manageDocumentsLAEventPage.selectC2SupportingDocuments();
  await manageDocumentsLAEventPage.selectC2();
  await I.goToNextPage();
  await manageDocumentsLAEventPage.uploadConfidentialSupportingEvidenceDocument(supportingEvidenceDocuments[2]);
  await I.addAnotherElementToCollection();
  await manageDocumentsLAEventPage.uploadSupportingEvidenceDocument(supportingEvidenceDocuments[3]);
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.applicationActions.manageDocumentsLA);

  caseViewPage.selectTab(caseViewPage.tabs.c2);

  I.seeInTab(['C2 Application 1', 'C2 supporting documents 1', 'Document name'], 'Correspondence document');
  I.seeInTab(['C2 Application 1', 'C2 supporting documents 1', 'Notes'], 'Test notes');
  I.seeInTab(['C2 Application 1', 'C2 supporting documents 1', 'File'], 'mockFile.txt');
  I.seeInTab(['C2 Application 1', 'C2 supporting documents 1', 'Date and time uploaded'], dateFormat(submittedAt, 'd mmm yyyy'));
  I.seeTextInTab(['C2 Application 1', 'C2 supporting documents 1', ''], 'Confidential');
  I.seeTextInTab(['C2 Application 1', 'C2 supporting documents 1', 'Uploaded by']);

  I.seeInTab(['C2 Application 1', 'C2 supporting documents 2', 'Document name'], 'C2 supporting document');
  I.seeInTab(['C2 Application 1', 'C2 supporting documents 2', 'Notes'], 'Supports the C2 application');
  I.seeInTab(['C2 Application 1', 'C2 supporting documents 2', 'File'], 'mockFile.txt');
  I.seeInTab(['C2 Application 1', 'C2 supporting documents 2', 'Date and time uploaded'], dateFormat(submittedAt, 'd mmm yyyy'));
  I.seeTextInTab(['C2 Application 1', 'C2 supporting documents 2', 'Uploaded by']);


  await I.navigateToCaseDetailsAs(config.hmctsAdminUser, caseId);
  caseViewPage.selectTab(caseViewPage.tabs.c2);

  I.seeInTab(['C2 Application 1', 'C2 supporting documents 1', 'Document name'], 'Correspondence document');
  I.seeInTab(['C2 Application 1', 'C2 supporting documents 1', 'Notes'], 'Test notes');
  I.seeInTab(['C2 Application 1', 'C2 supporting documents 1', 'File'], 'mockFile.txt');
  I.seeTextInTab(['C2 Application 1', 'C2 supporting documents 1', ''], 'Confidential');
  I.seeInTab(['C2 Application 1', 'C2 supporting documents 1', 'Date and time uploaded'], dateFormat(submittedAt, 'd mmm yyyy'));
  I.seeTextInTab(['C2 Application 1', 'C2 supporting documents 1', 'Uploaded by']);


  I.seeInTab(['C2 Application 1', 'C2 supporting documents 2', 'Document name'], 'C2 supporting document');
  I.seeInTab(['C2 Application 1', 'C2 supporting documents 2', 'Notes'], 'Supports the C2 application');
  I.seeInTab(['C2 Application 1', 'C2 supporting documents 2', 'File'], 'mockFile.txt');
  I.seeInTab(['C2 Application 1', 'C2 supporting documents 2', 'Date and time uploaded'], dateFormat(submittedAt, 'd mmm yyyy'));
  I.seeTextInTab(['C2 Application 1', 'C2 supporting documents 2', 'Uploaded by']);
});
