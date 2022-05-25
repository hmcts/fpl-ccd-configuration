const config = require('../config');
const hearingDetails = require('../fixtures/hearingTypeDetails.js');
const moment = require('moment');
const c2Payment = require('../fixtures/c2Payment.js');
const dateFormat = require('dateformat');
const dateToString = require('../helpers/date_to_string_helper');

let hearingStartDate;
let hearingEndDate;

const createHearing = async (I, caseViewPage, manageHearingsEventPage) => {
  hearingStartDate = moment().add(5, 'm').toDate();
  hearingEndDate = moment(hearingStartDate).add(5, 'm').toDate();

  await caseViewPage.goToNewActions(config.administrationActions.manageHearings);
  await manageHearingsEventPage.enterHearingDetails({startDate: hearingStartDate, endDate: hearingEndDate, presence: hearingDetails[0].presence});
  await manageHearingsEventPage.enterVenue(hearingDetails[0]);
  await I.goToNextPage();
  await manageHearingsEventPage.enterJudgeDetails(hearingDetails[0]);
  await manageHearingsEventPage.enterLegalAdvisorName(hearingDetails[0].judgeAndLegalAdvisor.legalAdvisorName);
  await I.goToNextPage();
  await manageHearingsEventPage.sendNoticeOfHearingWithNotes(hearingDetails[0].additionalNotes);
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.manageHearings);
};

const uploadC2 = async (I, caseViewPage, uploadAdditionalApplicationsEventPage) => {
  await caseViewPage.goToNewActions(config.administrationActions.uploadAdditionalApplications);
  uploadAdditionalApplicationsEventPage.selectAdditionalApplicationType('C2_ORDER');
  uploadAdditionalApplicationsEventPage.selectC2Type('WITH_NOTICE');
  uploadAdditionalApplicationsEventPage.selectApplicantList('Joe Bloggs, Respondent 1');
  await I.goToNextPage();
  uploadAdditionalApplicationsEventPage.uploadC2Document(config.testWordFile);
  uploadAdditionalApplicationsEventPage.selectC2AdditionalOrdersRequested('APPOINTMENT_OF_GUARDIAN');
  await I.goToNextPage();
  uploadAdditionalApplicationsEventPage.selectPeople(uploadAdditionalApplicationsEventPage.fields.allOthers.options.select, [2]);
  await I.goToNextPage();
  await uploadAdditionalApplicationsEventPage.getFeeToPay();
  uploadAdditionalApplicationsEventPage.usePbaPayment();
  uploadAdditionalApplicationsEventPage.enterPbaPaymentDetails(c2Payment);
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.uploadAdditionalApplications);
};

const uploadOtherApplications = async (I, caseViewPage, uploadAdditionalApplicationsEventPage) => {
  await caseViewPage.goToNewActions(config.administrationActions.uploadAdditionalApplications);
  uploadAdditionalApplicationsEventPage.selectAdditionalApplicationType('OTHER_ORDER');
  uploadAdditionalApplicationsEventPage.selectApplicantList('Emma Bloggs, Respondent 2');
  await I.goToNextPage();
  uploadAdditionalApplicationsEventPage.selectOtherApplication('C1 - Appointment of a guardian');
  uploadAdditionalApplicationsEventPage.uploadDocument(config.testWordFile);
  await I.goToNextPage();
  uploadAdditionalApplicationsEventPage.selectPeople(uploadAdditionalApplicationsEventPage.fields.allOthers.options.select, [2]);
  await I.goToNextPage();
  await uploadAdditionalApplicationsEventPage.getFeeToPay();
  uploadAdditionalApplicationsEventPage.usePbaPayment();
  uploadAdditionalApplicationsEventPage.enterPbaPaymentDetails(c2Payment);
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.uploadAdditionalApplications);
};

const formatHearingTime = hearingDate => formatDate(hearingDate, 'd mmm yyyy, h:MM:ss TT');
const formatHearingDate = hearingDate => formatDate(hearingDate, 'd mmmm yyyy');
const formatDate = (date, format) => dateFormat(date instanceof Date ? date : dateToString(date), format);

module.exports = {
  createHearing, uploadC2, uploadOtherApplications, formatHearingTime, formatHearingDate,
};
