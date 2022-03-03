const config = require('../config.js');
const gatekeepingWithPastHearingDetailsAndMissingVenueId = require('../fixtures/caseData/gatekeepingWithPastHearingDetailsAndMissingVenueId.json');
const gatekeepingWithPastHearingDetails = require('../fixtures/caseData/gatekeepingWithPastHearingDetails.json');
const hearingDetails = require('"../fixtures/hearingTypeDetails');
const dateFormat = require('dateformat');
const dateToString = require('../helpers/date_to_string_helper');
const api = require('../helpers/api_helper');
const moment = require('moment');

let caseId;
let submittedAt;
let hearingStartDate;
let hearingEndDate;

Feature('Hearing administration - handle unexpected missing hearingVenue');

async function setupScenario(I, missingVenueId) {
  if (missingVenueId) {
    caseId = await I.submitNewCaseWithData(gatekeepingWithPastHearingDetailsAndMissingVenueId);
  } else {
    caseId = await I.submitNewCaseWithData(gatekeepingWithPastHearingDetails);
  }
  submittedAt = new Date();
  await I.navigateToCaseDetailsAs(config.hmctsAdminUser, caseId);
}

Scenario('HMCTS admin is not able to see last court when previous hearings venue exist', async ({I, caseViewPage, manageHearingsEventPage}) => {
  await setupScenario(I, true);
  await caseViewPage.goToNewActions(config.administrationActions.manageHearings);
  manageHearingsEventPage.selectAddNewHearing();
  await I.goToNextPage();
  I.dontSee('Last Court');
});

Scenario('HMCTS admin is able to see last court when previous hearings venue exist', async ({I, caseViewPage, manageHearingsEventPage}) => {
  await setupScenario(I, false);
  await caseViewPage.goToNewActions(config.administrationActions.manageHearings);
  manageHearingsEventPage.selectAddNewHearing();
  await I.goToNextPage();
  I.see('Last Court');
});

Scenario('HMCTS admin creates subsequent hearings with previous hearing not having hearing venue', async ({I, caseViewPage, manageHearingsEventPage}) => {
  await setupScenario(I, true);
  hearingStartDate = moment().add(5,'m').toDate();
  hearingEndDate = moment(hearingStartDate).add(5,'m').toDate();

  await caseViewPage.goToNewActions(config.administrationActions.manageHearings);
  manageHearingsEventPage.selectAddNewHearing();
  await I.goToNextPage();
  I.dontSee('Last Court');

  await manageHearingsEventPage.enterHearingDetails(Object.assign({}, hearingDetails[0], {startDate: hearingStartDate}));
  manageHearingsEventPage.enterVenue(hearingDetails[0]);
  await manageHearingsEventPage.selectHearingDuration(Object.assign({}, hearingDetails[0], {endDate: hearingEndDate}));
  await I.goToNextPage();
  manageHearingsEventPage.enterJudgeDetails(hearingDetails[0]);
  manageHearingsEventPage.enterLegalAdvisorName(hearingDetails[0].judgeAndLegalAdvisor.legalAdvisorName);
  await I.goToNextPage();
  manageHearingsEventPage.sendNoticeOfHearingWithNotes(hearingDetails[0].additionalNotes);
  await I.goToNextPage();
  await manageHearingsEventPage.selectOthers(manageHearingsEventPage.fields.allOthers.options.all);
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.manageHearings);

  caseViewPage.selectTab(caseViewPage.tabs.hearings);

  I.seeInTab(['Hearing 3', 'Type of hearing'], hearingDetails[0].caseManagement);
  I.seeInTab(['Hearing 3', 'Court'], hearingDetails[0].venue);
  I.seeInTab(['Hearing 3', 'In person or remote'], hearingDetails[0].presence);
  I.seeInTab(['Hearing 3', 'Start date and time'],  formatHearingTime(hearingStartDate));
  I.seeInTab(['Hearing 3', 'End date and time'], formatHearingTime(hearingEndDate));
  I.seeInTab(['Hearing 3', 'Allocated judge or magistrate'], 'Her Honour Judge Moley');
  I.seeInTab(['Hearing 3', 'Hearing judge or magistrate'], 'Her Honour Judge Reed');
  I.seeInTab(['Hearing 3', 'Justices\' Legal Adviser\'s full name'], hearingDetails[0].judgeAndLegalAdvisor.legalAdvisorName);
  I.seeInTab(['Hearing 3', 'Additional notes'], hearingDetails[0].additionalNotes);
  I.seeInTab(['Hearing 3', 'Hearing attendance'], hearingDetails[0].attendance);
  I.seeInTab(['Hearing 3', 'Hearing attendance details'], hearingDetails[0].attendanceDetails);
  I.seeInTab(['Hearing 3', 'Pre-hearing attendance'], hearingDetails[0].preAttendanceDetails);
  I.seeInTab(['Hearing 3', 'Notice of hearing'], `Notice_of_hearing_${dateFormat(submittedAt, 'ddmmmm')}.pdf`);
  I.seeInTab(['Hearing 3', 'Others notified'], 'John Doe');

  await api.pollLastEvent(caseId, config.internalActions.updateCase);
});

const formatHearingTime = hearingDate => formatDate(hearingDate, 'd mmm yyyy, h:MM:ss TT');
const formatDate = (date, format) => dateFormat(date instanceof Date ? date : dateToString(date), format);