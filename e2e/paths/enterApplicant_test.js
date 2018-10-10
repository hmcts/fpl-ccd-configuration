const config = require('../config.js');
const applicant = require('../fixtures/applicant.js');
const solicitor = require('../fixtures/solicitor.js');

Feature('Enter applicant');

Before((I, caseViewPage) => {
	I.logInAndCreateCase(config.localAuthorityEmail, config.localAuthorityPassword, config.eventSummary, config.eventDescription);
	caseViewPage.goToNewActions(config.applicationActions.enterApplicants);
});

Scenario('Filling in the information for the applicant and submitting', (I, enterApplicantPage) => {
	enterApplicantPage.enterApplicantDetails(applicant);
	I.continueAndSubmit(config.eventSummary, config.eventDescription);
	I.seeEventSubmissionConfirmation(config.applicationActions.enterApplicants);
});

Scenario('Filling in the full section for enter applicants', (I, enterApplicantPage) => {
	enterApplicantPage.enterApplicantDetails(applicant);
	enterApplicantPage.enterSolicitorDetails(solicitor);
	I.continueAndSubmit(config.eventSummary, config.eventDescription);
	I.seeEventSubmissionConfirmation(config.applicationActions.enterApplicants);
});
