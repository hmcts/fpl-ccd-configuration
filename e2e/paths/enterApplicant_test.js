const config = require('../config.js');
const applicant = require('../fixtures/applicant.js');
const solicitor = require('../fixtures/solicitor.js');

Feature('Enter applicant');

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
  I.seeAnswerInTab(3, 'Party', 'Name of applicant', applicant.name);
  I.seeAnswerInTab(4, 'Party', 'Payment by account (PBA) number', applicant.pbaNumber);
  I.seeAnswerInTab(1, 'Address', 'Building and Street', applicant.address.buildingAndStreet.lineOne);
  I.seeAnswerInTab(2, 'Address', '', applicant.address.buildingAndStreet.lineTwo);
  I.seeAnswerInTab(3, 'Address', '', applicant.address.buildingAndStreet.lineThree);
  I.seeAnswerInTab(4, 'Address', 'Town or City', applicant.address.town);
  I.seeAnswerInTab(5, 'Address', 'Postcode/Zipcode', applicant.address.postcode);
  I.seeAnswerInTab(6, 'Address', 'Country', applicant.address.country);
  I.seeAnswerInTab(1, 'Telephone number', 'Telephone number', applicant.telephoneNumber);
  I.seeAnswerInTab(2, 'Telephone number', 'Name of person to contact', applicant.nameOfPersonToContact);
  I.seeAnswerInTab(7, 'Party', 'Job title', applicant.jobTitle);
  I.seeAnswerInTab(1, 'Mobile number', 'Mobile number', applicant.mobileNumber);
  I.seeAnswerInTab(1, 'Email', 'Email', applicant.email);
  I.seeAnswerInTab(1, 'Solicitor', 'Solicitor\'s full name', 'John Smith');
  I.seeAnswerInTab(2, 'Solicitor', 'Solicitor\'s mobile number', '7000000000');
  I.seeAnswerInTab(3, 'Solicitor', 'Solicitor\'s telephone number', '00000000000');
  I.seeAnswerInTab(4, 'Solicitor', 'Solicitor\'s email', 'solicitor@email.com');
  I.seeAnswerInTab(5, 'Solicitor', 'DX number', '160010 Kingsway 7');
  I.seeAnswerInTab(6, 'Solicitor', 'Solicitor\'s reference', 'reference');
});
