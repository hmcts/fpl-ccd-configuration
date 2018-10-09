const config = require('../config.js');

Feature('EnterApplicant');

Before((I, caseViewPage) => {
	I.logInAndCreateCase(config.localAuthorityEmail, config.localAuthorityPassword, config.eventSummary, config.eventDescription);
	caseViewPage.goToNewActions(config.applicationActions.enterApplicants);
});

Scenario('Filling in the information for the applicant and submitting', (I, enterApplicantPage) => {
	enterApplicantPage.enterApplicantDetails();
	I.continueAndSubmit(config.eventSummary, config.eventDescription);
	I.seeEventSubmissionConfirmation(config.applicationActions.enterApplicants);
});

Scenario('Filling in the full section for enter applicants', (I, enterApplicantPage) => {
	enterApplicantPage.enterApplicantDetails();
	enterApplicantPage.enterSolicitorDetails();
	I.continueAndSubmit(config.eventSummary, config.eventDescription);
	I.seeEventSubmissionConfirmation(config.applicationActions.enterApplicants);
});
