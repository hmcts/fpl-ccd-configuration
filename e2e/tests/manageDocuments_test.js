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
  await manageDocumentsEventPage.uploadConfidentialSupportingEvidenceDocument(supportingEvidenceDocuments[0], true);
  await I.addAnotherElementToCollection();
  await manageDocumentsEventPage.uploadSupportingEvidenceDocument(supportingEvidenceDocuments[1], true);
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.applicationActions.manageDocumentsLA);

  await I.navigateToCaseDetailsAs(config.swanseaLocalAuthorityUserOne, caseId);
  await caseViewPage.goToNewActions(config.applicationActions.manageDocumentsLA);

  await manageDocumentsLAEventPage.selectFurtherEvidence();
  await I.goToNextPage();
  await manageDocumentsLAEventPage.selectAnyOtherDocument();
  await I.goToNextPage();
  await manageDocumentsLAEventPage.uploadConfidentialSupportingEvidenceDocument(supportingEvidenceDocuments[2], true);
  await I.addAnotherElementToCollection();
  await manageDocumentsLAEventPage.uploadSupportingEvidenceDocument(supportingEvidenceDocuments[3], true);
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.applicationActions.manageDocumentsLA);

  caseViewPage.selectTab(caseViewPage.tabs.documents);

  I.dontSeeInTab(['Email to say evidence will be late']);
  assertFurtherEvidence(I, 'HMCTS', 1, 'Email with evidence attached', 'Case evidence included');

  assertConfidentialFurtherEvidence(I, 'Local authority', 1, 'Correspondence document', 'Test notes');
  assertFurtherEvidence(I, 'Local authority', 2, 'C2 supporting document', 'Supports the C2 application');

  await I.navigateToCaseDetailsAs(config.hmctsAdminUser, caseId);
  caseViewPage.selectTab(caseViewPage.tabs.documents);

  assertConfidentialFurtherEvidence(I, 'HMCTS', 1, 'Email to say evidence will be late', 'Evidence will be late');
  assertFurtherEvidence(I, 'HMCTS', 2, 'Email with evidence attached', 'Case evidence included');

  assertConfidentialFurtherEvidence(I, 'Local authority', 1, 'Correspondence document', 'Test notes');
  assertFurtherEvidence(I, 'Local authority', 2, 'C2 supporting document', 'Supports the C2 application');
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
  assertCorrespondence(I, 'HMCTS', 1, 'Email with evidence attached', 'Case evidence included');

  assertConfidentialCorrespondence(I, 'local authority', 1, 'Correspondence document', 'Test notes');
  assertCorrespondence(I, 'local authority', 2, 'C2 supporting document', 'Supports the C2 application');

  await I.navigateToCaseDetailsAs(config.hmctsAdminUser, caseId);
  caseViewPage.selectTab(caseViewPage.tabs.correspondence);

  assertConfidentialCorrespondence(I, 'HMCTS', 1, 'Email to say evidence will be late', 'Evidence will be late');
  assertCorrespondence(I, 'HMCTS', 2, 'Email with evidence attached', 'Case evidence included');

  assertConfidentialCorrespondence(I, 'local authority', 1, 'Correspondence document', 'Test notes');
  assertCorrespondence(I, 'local authority', 2, 'C2 supporting document', 'Supports the C2 application');
});

xScenario('HMCTS Admin and LA upload confidential C2 supporting documents', async ({I, caseViewPage, manageDocumentsEventPage, manageDocumentsLAEventPage, uploadC2DocumentsEventPage}) => {
  await I.navigateToCaseDetailsAs(config.hmctsAdminUser, caseId);
  await manageDocumentsForLAHelper.uploadC2(I, caseViewPage, uploadC2DocumentsEventPage);
  await caseViewPage.goToNewActions(config.applicationActions.manageDocumentsLA);

  await manageDocumentsEventPage.selectC2SupportingDocuments();
  await manageDocumentsEventPage.selectC2FromDropdown();
  await I.goToNextPage();
  await manageDocumentsEventPage.uploadConfidentialSupportingEvidenceDocument(supportingEvidenceDocuments[0]);
  await I.addAnotherElementToCollection();
  await manageDocumentsEventPage.uploadSupportingEvidenceDocument(supportingEvidenceDocuments[1]);
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.applicationActions.manageDocumentsLA);

  caseViewPage.selectTab(caseViewPage.tabs.c2);

  assertConfidentialC2SupportingDocuments(I, 1, 'Email to say evidence will be late', 'Evidence will be late');
  assertC2SupportingDocuments(I, 2, 'Email with evidence attached', 'Case evidence included');

  await I.navigateToCaseDetailsAs(config.swanseaLocalAuthorityUserOne, caseId);
  caseViewPage.selectTab(caseViewPage.tabs.c2);

  I.dontSeeInTab(['Email to say evidence will be late']);
  assertC2SupportingDocuments(I, 1, 'Email with evidence attached', 'Case evidence included');


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

  assertC2SupportingDocuments(I, 1, 'Email with evidence attached', 'Case evidence included');
  assertConfidentialC2SupportingDocuments(I, 2, 'Correspondence document', 'Test notes');
  assertC2SupportingDocuments(I, 3, 'C2 supporting document', 'Supports the C2 application');

  await I.navigateToCaseDetailsAs(config.hmctsAdminUser, caseId);
  caseViewPage.selectTab(caseViewPage.tabs.c2);

  assertConfidentialC2SupportingDocuments(I, 1, 'Email to say evidence will be late', 'Evidence will be late');
  assertC2SupportingDocuments(I, 2, 'Email with evidence attached', 'Case evidence included');
  assertConfidentialC2SupportingDocuments(I, 3, 'Correspondence document', 'Test notes');
  assertC2SupportingDocuments(I, 4, 'C2 supporting document', 'Supports the C2 application');
});

const assertConfidentialFurtherEvidence = (I, prefix, index, docName, notes) => {
  assertSupportingEvidence(I, `${prefix} further evidence documents ${index}`, docName, notes, true);
};

const assertFurtherEvidence = (I, prefix, index, docName, notes) => {
  assertSupportingEvidence(I, `${prefix} further evidence documents ${index}`, docName, notes, false);
};

const assertConfidentialCorrespondence = (I, suffix, index, docName, notes) => {
  assertSupportingEvidence(I, `Correspondence uploaded by ${suffix} ${index}`, docName, notes, true);
};

const assertCorrespondence = (I, suffix, index, docName, notes) => {
  assertSupportingEvidence(I, `Correspondence uploaded by ${suffix} ${index}`, docName, notes, false);
};

const assertSupportingEvidence = (I, supportingEvidenceName, docName, notes, confidential) => {
  I.seeInTab([supportingEvidenceName, 'Document name'], docName);
  I.seeInTab([supportingEvidenceName, 'Notes'], notes);
  I.seeInTab([supportingEvidenceName, 'File'], 'mockFile.txt');
  I.seeInTab([supportingEvidenceName, 'Date and time uploaded'], dateFormat(submittedAt, 'd mmm yyyy'));

  if (confidential) {
    I.seeInTab([supportingEvidenceName, ''], 'Confidential');
  }

  I.seeTextInTab([supportingEvidenceName, 'Uploaded by']);
};

const assertConfidentialC2SupportingDocuments = (I, index, docName, notes) => {
  assertC2SupportingDocuments(I, index, docName, notes, true);
};

const assertC2SupportingDocuments = (I, index, docName, notes, confidential = false) => {
  I.seeInTab(['C2 Application 1', `C2 supporting documents ${index}`, 'Document name'], docName);
  I.seeInTab(['C2 Application 1', `C2 supporting documents ${index}`, 'Notes'], notes);
  I.seeInTab(['C2 Application 1', `C2 supporting documents ${index}`, 'File'], 'mockFile.txt');
  I.seeInTab(['C2 Application 1', `C2 supporting documents ${index}`, 'Date and time uploaded'], dateFormat(submittedAt, 'd mmm yyyy'));

  if (confidential) {
    I.seeInTab(['C2 Application 1', `C2 supporting documents ${index}`, ''], 'Confidential');
  }

  I.seeTextInTab(['C2 Application 1', `C2 supporting documents ${index}`, 'Uploaded by']);
};
