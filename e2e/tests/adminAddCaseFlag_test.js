const config = require('../config.js');
const mandatoryWithMultipleChildren = require('../fixtures/caseData/mandatoryWithMultipleChildren.json');

const selectors = {
  signInButton: '//button[normalize-space()="Sign in"]',
  addRemoveCaseFlagButton: '//a[normalize-space()="Add or remove case flag"]',
  continueButton: '//button[normalize-space()="Continue"]',
  saveAndContinueButton: '//button[normalize-space()="Save and continue"]',
  nextStepDropdown: '#next-step',
};

Feature('Add Case Flag  ');

async function setupScenario(I) {
  // Step 1: Submit a new case and extract the caseId
  const caseIdObject = await I.submitNewCaseWithData(
    mandatoryWithMultipleChildren,
  );
  const caseIdValue = caseIdObject.caseId;

  // Step 2: Convert caseIdValue to a JSON-formatted string and remove quotes
  const caseIdString = JSON.stringify(caseIdValue);
  const caseIdStringClean = caseIdString.replace(/"/g, '');

  I.amOnPage(config.baseUrl);
  I.waitForElement('//input[@type="text"]', 20);
  I.fillField('//input[@type="text"]', config.hmctsAdminUser.email);
  I.fillField('//input[@type="password"]', config.hmctsAdminUser.password);
  I.click('Sign in');

  return { caseIdStringClean };
}

Scenario(
  'HMCTS Admin adds case flag and then removes case flag',
  async ({ I, caseViewPage, addCaseFlagEventPage }) => {
    const { caseIdStringClean } = await setupScenario(I);
    const caseUrl = `https://manage-case.aat.platform.hmcts.net/cases/case-details/${caseIdStringClean}`;
    await I.amOnPage(caseUrl);
    await I.click('#next-step');
    await I.selectOption('#next-step', 'Add case flag');
    await I.click('exui-case-home > div button');
    I.wait(5);

    // Add case flag and upload red dot form
    addCaseFlagEventPage.addCaseFlag();

    await addCaseFlagEventPage.uploadRedDotAssessmentForm(config.testWordFile);

    addCaseFlagEventPage.addAdditionalNotes();
    await I.click(selectors.continueButton);
    await I.click(selectors.saveAndContinueButton);

    await I.wait(5);

    // Check summary tab for information
    caseViewPage.selectTab(caseViewPage.tabs.summary);
    await I.seeTagInTab('Potentially violent person');

    I.see(
      'CTSC Admin',
      '#case-viewer-field-read--caseSummaryFlagAddedByFullName',
    );
    I.see(
      'fpl-ctsc-admin@justice.gov.uk',
      '#case-viewer-field-read--caseSummaryFlagAddedByEmail',
    );
    I.see(
      'mockFile.docx',
      '#case-viewer-field-read--caseSummaryFlagAssessmentForm',
    );
    I.see(
      'Additional case flag notes',
      '#case-viewer-field-read--caseSummaryCaseFlagNotes',
    );

    // Remove case flag
    caseViewPage.selectTab(caseViewPage.tabs.summary);

    await I.click(selectors.addRemoveCaseFlagButton);
    await I.wait(5);
    addCaseFlagEventPage.removeCaseFlag();
    await I.click(selectors.continueButton);
    await I.click(selectors.saveAndContinueButton);
    await I.wait(5);
    // Check summary tab that case flag information is not present
    caseViewPage.selectTab(caseViewPage.tabs.summary);

    await I.dontSeeTagInTab('Potentially violent person');

    I.dontSeeInTab('Flag added by', 'hmcts-admin@example.com (hmcts-admin)');
    I.dontSeeInTab('Email', 'HMCTS');
    I.dontSeeInTab('Assessment Form', 'mockFile.docx');
    I.dontSeeInTab('Additional notes', 'Additional case flag notes');
  },
);
