const config = require('../config.js');

Feature('UploadDocuments');

Before((I, caseViewPage) => {
	I.logInAndCreateCase(config.localAuthorityEmail, config.localAuthorityPassword, config.eventSummary, config.eventDescription);
	caseViewPage.goToNewActions(config.applicationActions.uploadDocuments);
});

Scenario('Selecting to follow for a document produces a textarea', (I, uploadDocumentsPage) => {
	I.selectOption(uploadDocumentsPage.fields.socialWorkChronologyStatus, 'To follow');
	I.fillField(uploadDocumentsPage.fields.socialWorkChronologyReason, 'mock reason');
	I.continueAndSubmit(config.eventSummary, config.eventDescription);
	I.see(`updated with event: ${config.applicationActions.uploadDocuments}`);
});

Scenario('All documents are able to be uploaded', (I, uploadDocumentsPage) => {
	uploadDocumentsPage.uploadDocuments(config.testFile);
	I.continueAndSubmit(config.eventSummary, config.eventDescription);
	I.see(`updated with event: ${config.applicationActions.uploadDocuments}`);
});
