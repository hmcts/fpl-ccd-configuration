const config = require('../config.js');
const applicant = require('../fixtures/applicant.js');
const solicitor = require('../fixtures/solicitor.js');

Feature('Enter applicant').retry(2);

Before((I, caseViewPage) => {
  I.logInAndCreateCase(config.swanseaLocalAuthorityEmailUserOne, config.localAuthorityPassword);
  caseViewPage.goToNewActions(config.applicationActions.enterApplicants);
});

Scenario('Filling in the information for the applicant and submitting', (I, enterApplicantPage, caseViewPage) => {
  enterApplicantPage.enterApplicantDetails(applicant);
  I.continueAndSave();
  I.seeEventSubmissionConfirmation(config.applicationActions.enterApplicants);
  caseViewPage.selectTab(caseViewPage.tabs.casePeople);
});

Scenario('Filling in the full section for enter applicants', (I, enterApplicantPage, caseViewPage) => {
  enterApplicantPage.enterApplicantDetails(applicant);
  enterApplicantPage.enterSolicitorDetails(solicitor);
  I.continueAndSave();
  I.seeEventSubmissionConfirmation(config.applicationActions.enterApplicants);
  caseViewPage.selectTab(caseViewPage.tabs.casePeople);
  I.seeAnswerInTab(1, 'Applicant', 'Name of applicant', 'John Smith');
  I.seeAnswerInTab(2, 'Applicant', 'Name of person to contact', 'Jonathon Walker');
  I.seeAnswerInTab(3, 'Applicant', 'Job title', 'Legal adviser');
  I.seeAnswerInTab(1, 'Address', 'Building and Street', 'Flat 2');
  I.seeAnswerInTab(2, 'Address', '', 'Caversham House 15-17');
  I.seeAnswerInTab(3, 'Address', '', 'Church Road');
  I.seeAnswerInTab(4, 'Address', 'Town or City', 'Reading');
  I.seeAnswerInTab(5, 'Address', 'Postcode/Zipcode', 'RG4 7AA');
  I.seeAnswerInTab(6, 'Address', 'Country', 'United Kingdom');
  I.seeAnswerInTab(5, 'Applicant', 'Mobile number', '7000000000');
  I.seeAnswerInTab(6, 'Applicant', 'Telephone number', '00000000000');
  I.seeAnswerInTab(7, 'Applicant', 'Email', 'applicant@email.com');
  I.seeAnswerInTab(1, 'Solicitor', 'Solicitor\'s full name', 'John Smith');
  I.seeAnswerInTab(2, 'Solicitor', 'Solicitor\'s mobile number', '7000000000');
  I.seeAnswerInTab(3, 'Solicitor', 'Solicitor\'s telephone number', '00000000000');
  I.seeAnswerInTab(4, 'Solicitor', 'Solicitor\'s email', 'solicitor@email.com');
  I.seeAnswerInTab(5, 'Solicitor', 'DX number', '160010 Kingsway 7');
  I.seeAnswerInTab(6, 'Solicitor', 'Solicitor\'s reference', 'reference');
});
