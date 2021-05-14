const config = require('../config.js');
const mandatoryWithMultipleChildren = require('../fixtures/caseData/mandatoryWithMultipleChildren.json');
const supportingEvidenceDocuments = require('../fixtures/supportingEvidenceDocuments.js');
const manageDocumentsForLAHelper = require('../helpers/manage_documents_for_LA_helper.js');
const api = require('../helpers/api_helper');

const dateFormat = require('dateformat');
const respondent = 'Joe Bloggs';
const respondent1StatementsSection = 'Respondent 1 statements';
const expertReportsSection = 'Expert reports';
const otherReportsSection = 'Other reports';

let caseId;
let submittedAt;

Feature('Manage documents');

BeforeSuite(async ({I}) => {
  caseId = await I.submitNewCaseWithData(mandatoryWithMultipleChildren);
  await api.grantCaseAccess(caseId, config.hillingdonLocalAuthorityUserOne, '[SOLICITOR]');
});

Scenario('HMCTS Admin and LA upload confidential and non confidential further evidence documents', async ({I, caseViewPage, manageDocumentsEventPage, manageDocumentsLAEventPage}) => {
  await I.navigateToCaseDetailsAs(config.hmctsAdminUser, caseId);
  await caseViewPage.goToNewActions(config.applicationActions.manageDocumentsLA);

  manageDocumentsEventPage.selectFurtherEvidence();
  await I.goToNextPage();
  manageDocumentsEventPage.selectAnyOtherDocument();
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

  caseViewPage.selectTab(caseViewPage.tabs.furtherEvidence);

  I.dontSeeDocumentSection(otherReportsSection, supportingEvidenceDocuments[0].name);
  I.expandDocumentSection(otherReportsSection, supportingEvidenceDocuments[1].name);
  I.seeInExpandedDocument(supportingEvidenceDocuments[1].name, 'HMCTS', dateFormat(submittedAt, 'd mmm yyyy'));

  I.expandDocument(otherReportsSection, supportingEvidenceDocuments[3].name);
  I.seeInExpandedDocument(supportingEvidenceDocuments[3].name, config.swanseaLocalAuthorityUserOne.email, dateFormat(submittedAt, 'd mmm yyyy'));
  I.dontSeeConfidentialInExpandedDocument(otherReportsSection, supportingEvidenceDocuments[3].name);

  I.expandDocumentSection(expertReportsSection, supportingEvidenceDocuments[2].name);
  I.seeInExpandedConfidentialDocument(supportingEvidenceDocuments[2].name, config.swanseaLocalAuthorityUserOne.email, dateFormat(submittedAt, 'd mmm yyyy'));

  await I.navigateToCaseDetailsAs(config.hmctsAdminUser, caseId);
  caseViewPage.selectTab(caseViewPage.tabs.furtherEvidence);

  I.expandDocumentSection(expertReportsSection, supportingEvidenceDocuments[0].name);
  I.seeInExpandedConfidentialDocument(supportingEvidenceDocuments[0].name, 'HMCTS', dateFormat(submittedAt, 'd mmm yyyy'));

  I.expandDocument(expertReportsSection, supportingEvidenceDocuments[2].name);
  I.seeInExpandedConfidentialDocument(supportingEvidenceDocuments[2].name, config.swanseaLocalAuthorityUserOne.email, dateFormat(submittedAt, 'd mmm yyyy'));

  I.expandDocumentSection(otherReportsSection, supportingEvidenceDocuments[1].name);
  I.seeInExpandedDocument(supportingEvidenceDocuments[1].name, 'HMCTS', dateFormat(submittedAt, 'd mmm yyyy'));

  I.expandDocument(otherReportsSection, supportingEvidenceDocuments[3].name);
  I.seeInExpandedDocument(supportingEvidenceDocuments[3].name, config.swanseaLocalAuthorityUserOne.email, dateFormat(submittedAt, 'd mmm yyyy'));
});

Scenario('HMCTS Admin and LA upload confidential and non confidential respondent statement', async ({I, caseViewPage, manageDocumentsEventPage, manageDocumentsLAEventPage}) => {
  await I.navigateToCaseDetailsAs(config.hmctsAdminUser, caseId);
  await caseViewPage.goToNewActions(config.administrationActions.manageDocuments);

  manageDocumentsEventPage.selectFurtherEvidence();
  await I.goToNextPage();
  manageDocumentsEventPage.selectRespondentStatement();
  manageDocumentsEventPage.selectRespondent(respondent);
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
  await manageDocumentsLAEventPage.selectRespondentStatement();
  await manageDocumentsLAEventPage.selectRespondent(respondent);
  await I.goToNextPage();
  await I.addAnotherElementToCollection();
  await manageDocumentsLAEventPage.uploadConfidentialSupportingEvidenceDocument(supportingEvidenceDocuments[2]);
  await I.addAnotherElementToCollection();
  await manageDocumentsLAEventPage.uploadSupportingEvidenceDocument(supportingEvidenceDocuments[3]);
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.applicationActions.manageDocumentsLA);

  caseViewPage.selectTab(caseViewPage.tabs.furtherEvidence);
  I.expandDocumentSection(respondent1StatementsSection, supportingEvidenceDocuments[2].name);
  I.seeInExpandedConfidentialDocument(supportingEvidenceDocuments[2].name, config.swanseaLocalAuthorityUserOne.email, dateFormat(submittedAt, 'd mmm yyyy'));

  I.expandDocument(respondent1StatementsSection, supportingEvidenceDocuments[3].name);
  I.seeInExpandedDocument(supportingEvidenceDocuments[3].name, config.swanseaLocalAuthorityUserOne.email, dateFormat(submittedAt, 'd mmm yyyy'));
  I.dontSeeConfidentialInExpandedDocument(respondent1StatementsSection, supportingEvidenceDocuments[3].name);

  I.expandDocument(respondent1StatementsSection, supportingEvidenceDocuments[1].name);
  I.seeInExpandedDocument(supportingEvidenceDocuments[1].name, 'HMCTS', dateFormat(submittedAt, 'd mmm yyyy'));
  I.dontSeeConfidentialInExpandedDocument(respondent1StatementsSection, supportingEvidenceDocuments[2].name);

  I.dontSeeDocumentSection(respondent1StatementsSection, supportingEvidenceDocuments[0].name);

  await I.navigateToCaseDetailsAs(config.hmctsAdminUser, caseId);
  caseViewPage.selectTab(caseViewPage.tabs.furtherEvidence);
  I.expandDocumentSection(respondent1StatementsSection, supportingEvidenceDocuments[2].name);
  I.seeInExpandedConfidentialDocument(supportingEvidenceDocuments[2].name, config.swanseaLocalAuthorityUserOne.email, dateFormat(submittedAt, 'd mmm yyyy'));

  I.expandDocument(respondent1StatementsSection, supportingEvidenceDocuments[3].name);
  I.seeInExpandedDocument(supportingEvidenceDocuments[3].name, config.swanseaLocalAuthorityUserOne.email, dateFormat(submittedAt, 'd mmm yyyy'));
  I.dontSeeConfidentialInExpandedDocument(respondent1StatementsSection, supportingEvidenceDocuments[3].name);

  I.expandDocument(respondent1StatementsSection, supportingEvidenceDocuments[0].name);
  I.seeInExpandedConfidentialDocument(supportingEvidenceDocuments[0].name, 'HMCTS', dateFormat(submittedAt, 'd mmm yyyy'));

  I.expandDocument(respondent1StatementsSection, supportingEvidenceDocuments[1].name);
  I.seeInExpandedDocument(supportingEvidenceDocuments[1].name, 'HMCTS', dateFormat(submittedAt, 'd mmm yyyy'));
  I.dontSeeConfidentialInExpandedDocument(respondent1StatementsSection, supportingEvidenceDocuments[1].name);

  await I.navigateToCaseDetailsAs(config.hillingdonLocalAuthorityUserOne, caseId);
  caseViewPage.selectTab(caseViewPage.tabs.furtherEvidence);

  I.expandDocumentSection(respondent1StatementsSection, supportingEvidenceDocuments[3].name);
  I.seeInExpandedDocument(supportingEvidenceDocuments[3].name, config.swanseaLocalAuthorityUserOne.email, dateFormat(submittedAt, 'd mmm yyyy'));
  I.dontSeeConfidentialInExpandedDocument(respondent1StatementsSection, supportingEvidenceDocuments[3].name);

  I.expandDocument(respondent1StatementsSection, supportingEvidenceDocuments[1].name);
  I.seeInExpandedDocument(supportingEvidenceDocuments[1].name, 'HMCTS', dateFormat(submittedAt, 'd mmm yyyy'));
  I.dontSeeConfidentialInExpandedDocument(respondent1StatementsSection, supportingEvidenceDocuments[2].name);
  I.dontSeeDocumentSection(respondent1StatementsSection, supportingEvidenceDocuments[2].name);
  I.dontSeeDocumentSection(respondent1StatementsSection, supportingEvidenceDocuments[0].name);
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

Scenario('HMCTS Admin and LA upload confidential C2 supporting documents', async ({I, caseViewPage, manageDocumentsEventPage, manageDocumentsLAEventPage, uploadAdditionalApplicationsEventPage}) => {
  await I.navigateToCaseDetailsAs(config.hmctsAdminUser, caseId);
  await manageDocumentsForLAHelper.uploadC2(I, caseViewPage, uploadAdditionalApplicationsEventPage);
  await caseViewPage.goToNewActions(config.applicationActions.manageDocumentsLA);

  await manageDocumentsEventPage.selectAdditionalApplicationsSupportingDocuments();
  await manageDocumentsEventPage.selectApplicationBundleFromDropdown(2);
  await I.goToNextPage();
  await manageDocumentsEventPage.uploadConfidentialSupportingEvidenceDocument(supportingEvidenceDocuments[0]);
  await I.addAnotherElementToCollection();
  await manageDocumentsEventPage.uploadSupportingEvidenceDocument(supportingEvidenceDocuments[1]);
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.applicationActions.manageDocumentsLA);

  caseViewPage.selectTab(caseViewPage.tabs.otherApplications);

  assertConfidentialC2SupportingDocuments(I, 'C2 application', 1, 'Email to say evidence will be late', 'Evidence will be late');
  assertC2SupportingDocuments(I, 'C2 application', 2, 'Email with evidence attached', 'Case evidence included');

  await I.navigateToCaseDetailsAs(config.swanseaLocalAuthorityUserOne, caseId);
  caseViewPage.selectTab(caseViewPage.tabs.otherApplications);

  I.dontSeeInTab(['Email to say evidence will be late']);
  assertC2SupportingDocuments(I, 'C2 application', 1, 'Email with evidence attached', 'Case evidence included');

  await caseViewPage.goToNewActions(config.applicationActions.manageDocumentsLA);

  await manageDocumentsLAEventPage.selectAdditionalApplicationsSupportingDocuments();
  await manageDocumentsLAEventPage.selectApplicationBundleFromDropdown(2);
  await I.goToNextPage();
  await manageDocumentsLAEventPage.uploadConfidentialSupportingEvidenceDocument(supportingEvidenceDocuments[2]);
  await I.addAnotherElementToCollection();
  await manageDocumentsLAEventPage.uploadSupportingEvidenceDocument(supportingEvidenceDocuments[3]);
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.applicationActions.manageDocumentsLA);

  caseViewPage.selectTab(caseViewPage.tabs.otherApplications);

  assertC2SupportingDocuments(I, 'C2 application', 1, 'Email with evidence attached', 'Case evidence included');
  assertConfidentialC2SupportingDocuments(I, 'C2 application', 2, 'Correspondence document', 'Test notes');
  assertC2SupportingDocuments(I, 'C2 application', 3, 'C2 supporting document', 'Supports the C2 application');

  await I.navigateToCaseDetailsAs(config.hmctsAdminUser, caseId);
  caseViewPage.selectTab(caseViewPage.tabs.otherApplications);

  assertConfidentialC2SupportingDocuments(I, 'C2 application', 1, 'Email to say evidence will be late', 'Evidence will be late');
  assertC2SupportingDocuments(I, 'C2 application', 2, 'Email with evidence attached', 'Case evidence included');
  assertConfidentialC2SupportingDocuments(I, 'C2 application', 3, 'Correspondence document', 'Test notes');
  assertC2SupportingDocuments(I, 'C2 application', 4, 'C2 supporting document', 'Supports the C2 application');
});

Scenario('HMCTS Admin and LA upload confidential Other applications supporting documents', async ({I, caseViewPage, manageDocumentsEventPage, manageDocumentsLAEventPage, uploadAdditionalApplicationsEventPage}) => {
  await I.navigateToCaseDetailsAs(config.hmctsAdminUser, caseId);
  await manageDocumentsForLAHelper.uploadOtherApplications(I, caseViewPage, uploadAdditionalApplicationsEventPage);
  await caseViewPage.goToNewActions(config.applicationActions.manageDocumentsLA);

  await manageDocumentsEventPage.selectAdditionalApplicationsSupportingDocuments();
  await manageDocumentsEventPage.selectApplicationBundleFromDropdown(2);
  await I.goToNextPage();
  await manageDocumentsEventPage.uploadConfidentialSupportingEvidenceDocument(supportingEvidenceDocuments[0]);
  await I.addAnotherElementToCollection();
  await manageDocumentsEventPage.uploadSupportingEvidenceDocument(supportingEvidenceDocuments[1]);
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.applicationActions.manageDocumentsLA);

  caseViewPage.selectTab(caseViewPage.tabs.otherApplications);

  assertConfidentialC2SupportingDocuments(I, 'Other applications', 1, 'Email to say evidence will be late', 'Evidence will be late');
  assertC2SupportingDocuments(I, 'Other applications', 2, 'Email with evidence attached', 'Case evidence included');

  await I.navigateToCaseDetailsAs(config.swanseaLocalAuthorityUserOne, caseId);
  caseViewPage.selectTab(caseViewPage.tabs.otherApplications);

  I.dontSeeInTab(['Email to say evidence will be late']);
  assertC2SupportingDocuments(I, 'Other applications', 1, 'Email with evidence attached', 'Case evidence included');

  await caseViewPage.goToNewActions(config.applicationActions.manageDocumentsLA);

  await manageDocumentsLAEventPage.selectAdditionalApplicationsSupportingDocuments();
  await manageDocumentsLAEventPage.selectApplicationBundleFromDropdown(2);
  await I.goToNextPage();
  await manageDocumentsLAEventPage.uploadConfidentialSupportingEvidenceDocument(supportingEvidenceDocuments[2]);
  await I.addAnotherElementToCollection();
  await manageDocumentsLAEventPage.uploadSupportingEvidenceDocument(supportingEvidenceDocuments[3]);
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.applicationActions.manageDocumentsLA);

  caseViewPage.selectTab(caseViewPage.tabs.otherApplications);

  assertC2SupportingDocuments(I, 'Other applications', 1, 'Email with evidence attached', 'Case evidence included');
  assertConfidentialC2SupportingDocuments(I, 'Other applications', 2, 'Correspondence document', 'Test notes');
  assertC2SupportingDocuments(I, 'Other applications', 3, 'C2 supporting document', 'Supports the C2 application');

  await I.navigateToCaseDetailsAs(config.hmctsAdminUser, caseId);
  caseViewPage.selectTab(caseViewPage.tabs.otherApplications);

  assertConfidentialC2SupportingDocuments(I, 'Other applications', 1, 'Email to say evidence will be late', 'Evidence will be late');
  assertC2SupportingDocuments(I, 'Other applications', 2, 'Email with evidence attached', 'Case evidence included');
  assertConfidentialC2SupportingDocuments(I, 'Other applications', 3, 'Correspondence document', 'Test notes');
  assertC2SupportingDocuments(I, 'Other applications', 4, 'C2 supporting document', 'Supports the C2 application');
});

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
};

const assertConfidentialC2SupportingDocuments = (I, application, index, docName, notes) => {
  assertC2SupportingDocuments(I, application, index, docName, notes, true);
};

const assertC2SupportingDocuments = (I, application, index, docName, notes, confidential = false) => {
  I.seeInTab(['Additional applications 1', application, `Supporting documents ${index}`, 'Document name'], docName);
  I.seeInTab(['Additional applications 1', application, `Supporting documents ${index}`, 'Notes'], notes);
  I.seeInTab(['Additional applications 1', application, `Supporting documents ${index}`, 'Date and time uploaded'], dateFormat(submittedAt, 'd mmm yyyy'));
  I.seeInTab(['Additional applications 1', application, `Supporting documents ${index}`, 'File'], 'mockFile.txt');

  I.seeTextInTab(['Additional applications 1', application, `Supporting documents ${index}`, 'Uploaded by']);

  if (confidential) {
    I.seeInTab(['Additional applications 1', application, `Supporting documents ${index}`, ''], 'Confidential');
  }
};
