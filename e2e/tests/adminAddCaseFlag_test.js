const config = require('../config.js');
const mandatoryWithMultipleChildren = require('../fixtures/caseData/mandatoryWithMultipleChildren.json');
const supportingEvidenceDocuments = require('../fixtures/supportingEvidenceDocuments.js');
const manageDocumentsForLAHelper = require('../helpers/manage_documents_for_LA_helper.js');
const api = require('../helpers/api_helper');

let caseId;

Feature('Add Case Flag');

async function setupScenario(I) {
  if (!caseId) {
    caseId = await I.submitNewCaseWithData(mandatoryWithMultipleChildren);
  }
  await I.navigateToCaseDetailsAs(config.hmctsAdminUser, caseId);
}

Scenario('HMCTS Admin adds case flag and then removes case flag', async ({I, caseViewPage, addCaseFlagEventPage}) => {

  await setupScenario(I);
  await caseViewPage.goToNewActions(config.administrationActions.addCaseFlag);

  // Add case flag and upload red dot form
  addCaseFlagEventPage.addCaseFlag();
  await addCaseFlagEventPage.uploadRedDotAssessmentForm(config.testWordFile);
  addCaseFlagEventPage.addAdditionalNotes();
  await I.seeCheckAnswersAndCompleteEvent('Save and continue');

  // Check summary tab for information
  caseViewPage.selectTab(caseViewPage.tabs.summary);
  I.seeTagInTab('Potentially violent person');
  I.seeInTab('Flag added by', 'hmcts-admin@example.com (hmcts-admin)');
  I.seeInTab('Email', 'HMCTS');
  I.seeInTab('Assessment Form', 'mockFile.docx');
  I.seeInTab('Additional notes', 'Additional case flag notes');

  // Remove case flag
  await caseViewPage.goToNewActions(config.administrationActions.addCaseFlag);
  addCaseFlagEventPage.removeCaseFlag();
  await I.seeCheckAnswersAndCompleteEvent('Save and continue');

  // Check summary tab that case flag information is not present
  caseViewPage.selectTab(caseViewPage.tabs.summary);
  I.dontSeeTagInTab('Potentially violent person');
  I.dontSeeInTab('Flag added by', 'hmcts-admin@example.com (hmcts-admin)');
  I.dontSeeInTab('Email', 'HMCTS');
  I.dontSeeInTab('Assessment Form', 'mockFile.docx');
  I.dontSeeInTab('Additional notes', 'Additional case flag notes');
});
