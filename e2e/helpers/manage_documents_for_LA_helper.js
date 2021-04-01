const config = require('../config');
const hearingDetails = require('../fixtures/hearingTypeDetails.js');
const moment = require('moment');
const c2Payment = require('../fixtures/c2Payment.js');

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

const uploadC2 = async (I, caseViewPage, uploadC2DocumentsEventPage) => {
  await caseViewPage.goToNewActions(config.administrationActions.uploadC2Documents);
  uploadC2DocumentsEventPage.selectApplicationType('WITH_NOTICE');
  await I.goToNextPage();
  uploadC2DocumentsEventPage.usePbaPayment();
  uploadC2DocumentsEventPage.enterPbaPaymentDetails(c2Payment);
  uploadC2DocumentsEventPage.uploadC2Document(config.testFile, 'Rachel Zane C2');
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.uploadC2Documents);
};

module.exports = {
  createHearing, uploadC2,
};
