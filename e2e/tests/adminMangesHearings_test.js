const config = require('../config.js');
const hearingDetails = require('../fixtures/hearingTypeDetails.js');
const dateFormat = require('dateformat');
const dateToString = require('../helpers/date_to_string_helper');
const mandatoryWithMultipleChildren = require('../fixtures/caseData/mandatoryWithMultipleChildren.json');
const supportingEvidenceDocuments = require('../fixtures/supportingEvidenceDocuments.js');
const moment = require('moment');
const api = require('../helpers/api_helper');

let caseId;
let submittedAt;
let hearingStartDate;
let hearingEndDate;

Feature('Hearing administration');

BeforeSuite(async ({I}) => {
  caseId = await I.submitNewCaseWithData(mandatoryWithMultipleChildren);
  submittedAt = new Date();
});

Before(async ({I}) => await I.navigateToCaseDetailsAs(config.hmctsAdminUser, caseId));

Scenario('HMCTS admin creates first hearings', async ({I, caseViewPage, manageHearingsEventPage}) => {
  hearingStartDate = moment().add(5,'m').toDate();
  hearingEndDate = moment(hearingStartDate).add(5,'m').toDate();

  await caseViewPage.goToNewActions(config.administrationActions.manageHearings);
  await manageHearingsEventPage.enterHearingDetails({startDate: hearingStartDate, endDate: hearingEndDate, presence: hearingDetails[0].presence});
  manageHearingsEventPage.enterVenue(hearingDetails[0]);
  await I.goToNextPage();
  manageHearingsEventPage.enterJudgeDetails(hearingDetails[0]);
  manageHearingsEventPage.enterLegalAdvisorName(hearingDetails[0].judgeAndLegalAdvisor.legalAdvisorName);
  await I.goToNextPage();
  manageHearingsEventPage.sendNoticeOfHearingWithNotes(hearingDetails[0].additionalNotes);
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.manageHearings);

  caseViewPage.selectTab(caseViewPage.tabs.hearings);

  I.seeInTab(['Hearing 1', 'Type of hearing'], hearingDetails[0].caseManagement);
  I.seeInTab(['Hearing 1', 'Court'], hearingDetails[0].venue);
  I.seeInTab(['Hearing 1', 'In person or remote'], hearingDetails[0].presence);
  I.seeInTab(['Hearing 1', 'Start date and time'],  formatHearingTime(hearingStartDate));
  I.seeInTab(['Hearing 1', 'End date and time'], formatHearingTime(hearingEndDate));
  I.seeInTab(['Hearing 1', 'Allocated judge or magistrate'], 'Her Honour Judge Moley');
  I.seeInTab(['Hearing 1', 'Hearing judge or magistrate'], 'Her Honour Judge Reed');
  I.seeInTab(['Hearing 1', 'Justices\' Legal Adviser\'s full name'], hearingDetails[0].judgeAndLegalAdvisor.legalAdvisorName);
  I.seeInTab(['Hearing 1', 'Additional notes'], hearingDetails[0].additionalNotes);
  I.seeInTab(['Hearing 1', 'Notice of hearing'], `Notice_of_hearing_${dateFormat(submittedAt, 'ddmmmm')}.pdf`);

  await api.pollLastEvent(caseId, config.internalActions.updateCase);
});

Scenario('HMCTS admin creates subsequent hearings', async ({I, caseViewPage, manageHearingsEventPage}) => {
  await caseViewPage.goToNewActions(config.administrationActions.manageHearings);
  manageHearingsEventPage.selectAddNewHearing();
  await I.goToNextPage();
  await manageHearingsEventPage.enterHearingDetails(hearingDetails[1]);
  manageHearingsEventPage.selectPreviousVenue();
  await I.goToNextPage();
  manageHearingsEventPage.selectedAllocatedJudge();
  await I.goToNextPage();
  manageHearingsEventPage.dontSendNoticeOfHearing();
  await I.completeEvent('Save and continue');

  caseViewPage.selectTab(caseViewPage.tabs.hearings);

  I.seeInTab(['Hearing 2', 'Type of hearing'], hearingDetails[1].caseManagement);
  I.seeInTab(['Hearing 2', 'Court'], hearingDetails[0].venue);
  I.seeInTab(['Hearing 2', 'In person or remote'], hearingDetails[1].presence);
  I.seeInTab(['Hearing 2', 'Start date and time'], formatHearingTime(hearingDetails[1].startDate));
  I.seeInTab(['Hearing 2', 'End date and time'], formatHearingTime(hearingDetails[1].endDate));
  I.seeInTab(['Hearing 2', 'Allocated judge or magistrate'], 'Her Honour Judge Moley');
});

Scenario('HMCTS admin edit hearings', async ({I, caseViewPage, manageHearingsEventPage}) => {
  await caseViewPage.goToNewActions(config.administrationActions.manageHearings);
  manageHearingsEventPage.selectEditHearing('Case management hearing, 1 January 2060');
  await I.goToNextPage();
  await manageHearingsEventPage.enterNewVenue(hearingDetails[1]);
  await I.goToNextPage();
  manageHearingsEventPage.selectedAllocatedJudge();
  await I.goToNextPage();
  manageHearingsEventPage.sendNoticeOfHearingWithNotes('The venue has changed');
  await I.completeEvent('Save and continue');

  caseViewPage.selectTab(caseViewPage.tabs.hearings);

  I.seeInTab(['Hearing 2', 'Type of hearing'], hearingDetails[1].caseManagement);
  I.seeInTab(['Hearing 2', 'Court'], hearingDetails[1].venue);
  I.seeInTab(['Hearing 2', 'Court address', 'Building and Street'], hearingDetails[1].venueCustomAddress.buildingAndStreet.lineOne);
  I.seeInTab(['Hearing 2', 'Court address', 'Address Line 2'], hearingDetails[1].venueCustomAddress.buildingAndStreet.lineTwo);
  I.seeInTab(['Hearing 2', 'Court address', 'Address Line 3'], hearingDetails[1].venueCustomAddress.buildingAndStreet.lineThree);
  I.seeInTab(['Hearing 2', 'Court address', 'Town or City'], hearingDetails[1].venueCustomAddress.town);
  I.seeInTab(['Hearing 2', 'Court address', 'Postcode/Zipcode'], hearingDetails[1].venueCustomAddress.postcode);
  I.seeInTab(['Hearing 2', 'Court address', 'Country'], hearingDetails[1].venueCustomAddress.country);

  I.seeInTab(['Hearing 2', 'In person or remote'], hearingDetails[1].presence);
  I.seeInTab(['Hearing 2', 'Start date and time'], formatHearingTime(hearingDetails[1].startDate));
  I.seeInTab(['Hearing 2', 'End date and time'], formatHearingTime(hearingDetails[1].endDate));
  I.seeInTab(['Hearing 2', 'Allocated judge or magistrate'], 'Her Honour Judge Moley');
  I.seeInTab(['Hearing 2', 'Additional notes'], 'The venue has changed');
  I.seeInTab(['Hearing 2', 'Notice of hearing'], `Notice_of_hearing_${dateFormat(submittedAt, 'ddmmmm')}.pdf`);

  await api.pollLastEvent(caseId, config.internalActions.updateCase);
});

Scenario('HMCTS admin uploads further hearing evidence documents', async ({I, caseViewPage, manageDocumentsEventPage}) => {
  await caseViewPage.goToNewActions(config.administrationActions.manageDocuments);
  manageDocumentsEventPage.selectFurtherEvidence();
  await I.goToNextPage();
  manageDocumentsEventPage.selectAnyOtherDocument();
  manageDocumentsEventPage.selectFurtherEvidenceIsRelatedToHearing();
  manageDocumentsEventPage.selectHearing(formatHearingDate(hearingStartDate));
  await I.goToNextPage();
  await manageDocumentsEventPage.uploadSupportingEvidenceDocument(supportingEvidenceDocuments[0], true);
  await I.addAnotherElementToCollection();
  await manageDocumentsEventPage.uploadSupportingEvidenceDocument(supportingEvidenceDocuments[1], true);
  await I.completeEvent('Save and continue', {summary: 'Summary', description: 'Description'});
  I.seeEventSubmissionConfirmation(config.administrationActions.manageDocuments);

  await caseViewPage.goToNewActions(config.administrationActions.manageDocuments);
  manageDocumentsEventPage.selectFurtherEvidence();
  await I.goToNextPage();
  manageDocumentsEventPage.selectAnyOtherDocument();
  manageDocumentsEventPage.selectFurtherEvidenceIsRelatedToHearing();
  manageDocumentsEventPage.selectHearing('1 January 2060');
  await I.goToNextPage();
  await manageDocumentsEventPage.uploadSupportingEvidenceDocument(supportingEvidenceDocuments[0], true);
  await I.completeEvent('Save and continue', {summary: 'Summary', description: 'Description'});
  I.seeEventSubmissionConfirmation(config.administrationActions.manageDocuments);

  caseViewPage.selectTab(caseViewPage.tabs.documents);
  I.seeInTab(['Further evidence documents for hearings 1', 'Hearing'], `Case management hearing, ${formatHearingDate(hearingStartDate)}`);
  I.seeInTab(['Further evidence documents for hearings 1', 'Documents 1', 'Document name'], 'Email to say evidence will be late');
  I.seeInTab(['Further evidence documents for hearings 1', 'Documents 1', 'Notes'], 'Evidence will be late');
  I.seeInTab(['Further evidence documents for hearings 1', 'Documents 1', 'Date and time uploaded'], dateFormat(submittedAt, 'd mmm yyyy'));
  I.seeInTab(['Further evidence documents for hearings 1', 'Documents 1', 'File'], 'mockFile.txt');
  I.seeTextInTab(['Further evidence documents for hearings 1', 'Documents 1', 'Uploaded by']);
  I.seeInTab(['Further evidence documents for hearings 1', 'Documents 2', 'Document name'], 'Email with evidence attached');
  I.seeInTab(['Further evidence documents for hearings 1', 'Documents 2', 'Notes'], 'Case evidence included');
  I.seeInTab(['Further evidence documents for hearings 1', 'Documents 2', 'Date and time uploaded'], dateFormat(submittedAt, 'd mmm yyyy'));
  I.seeInTab(['Further evidence documents for hearings 1', 'Documents 2', 'File'], 'mockFile.txt');
  I.seeTextInTab(['Further evidence documents for hearings 1', 'Documents 2', 'Uploaded by']);

  I.seeInTab(['Further evidence documents for hearings 2', 'Hearing'], 'Case management hearing, 1 January 2060');
  I.seeInTab(['Further evidence documents for hearings 2', 'Documents 1', 'Document name'], 'Email to say evidence will be late');
  I.seeInTab(['Further evidence documents for hearings 2', 'Documents 1', 'Notes'], 'Evidence will be late');
  I.seeInTab(['Further evidence documents for hearings 2', 'Documents 1', 'Date and time uploaded'], dateFormat(submittedAt, 'd mmm yyyy'));
  I.seeInTab(['Further evidence documents for hearings 2', 'Documents 1', 'File'], 'mockFile.txt');
  I.seeTextInTab(['Further evidence documents for hearings 2', 'Documents 1', 'Uploaded by']);
});


Scenario('HMCTS admin adjourns and re-lists a hearing', async ({I, caseViewPage, manageHearingsEventPage}) => {
  const reListedHearingJudgeName = 'Brown';

  await caseViewPage.goToNewActions(config.administrationActions.manageHearings);
  manageHearingsEventPage.selectAdjournHearing(`Case management hearing, ${formatHearingDate(hearingStartDate)}`);
  await I.goToNextPage();
  manageHearingsEventPage.selectCancellationReasonType('Other lawyers');
  manageHearingsEventPage.selectCancellationReason('No key issue analysis');
  await I.goToNextPage();
  manageHearingsEventPage.selectCancellationAction('Yes - and I can add the new date now');
  await I.goToNextPage();
  await manageHearingsEventPage.enterHearingDetails(hearingDetails[0]);
  await I.goToNextPage();
  manageHearingsEventPage.enterJudgeName(reListedHearingJudgeName);
  await I.goToNextPage();
  manageHearingsEventPage.dontSendNoticeOfHearing();
  await I.completeEvent('Save and continue');

  caseViewPage.selectTab(caseViewPage.tabs.hearings);

  I.seeInTab(['Hearing 2', 'Type of hearing'], hearingDetails[0].caseManagement);
  I.seeInTab(['Hearing 2', 'Court'], hearingDetails[0].venue);
  I.seeInTab(['Hearing 2', 'In person or remote'], hearingDetails[0].presence);
  I.seeInTab(['Hearing 2', 'Start date and time'], formatHearingTime(hearingDetails[0].startDate));
  I.seeInTab(['Hearing 2', 'End date and time'], formatHearingTime(hearingDetails[0].endDate));
  I.seeInTab(['Hearing 2', 'Hearing judge or magistrate'], 'Her Honour Judge Brown');
  I.seeInTab(['Hearing 2', 'Justices\' Legal Adviser\'s full name'], hearingDetails[0].judgeAndLegalAdvisor.legalAdvisorName);

  I.seeInTab(['Adjourned or vacated hearing 1', 'Type of hearing'], hearingDetails[0].caseManagement);
  I.seeInTab(['Adjourned or vacated hearing 1', 'Start date and time'], formatHearingTime(hearingStartDate));
  I.seeInTab(['Adjourned or vacated hearing 1', 'Status'], 'Adjourned');

  caseViewPage.selectTab(caseViewPage.tabs.documents);

  I.seeInTab(['Further evidence documents for hearings 2', 'Hearing'], 'Case management hearing, 1 January 2050');
});

Scenario('HMCTS admin vacates and re-lists a hearing', async ({I, caseViewPage, manageHearingsEventPage}) => {
  await caseViewPage.goToNewActions(config.administrationActions.manageHearings);
  manageHearingsEventPage.selectVacateHearing('Case management hearing, 1 January 2060');
  await I.goToNextPage();
  manageHearingsEventPage.selectCancellationAction('Yes - and I can add the new date now');
  await I.goToNextPage();
  manageHearingsEventPage.selectCancellationReasonType('Other lawyers');
  manageHearingsEventPage.selectCancellationReason('No key issue analysis');
  await I.goToNextPage();
  await manageHearingsEventPage.enterHearingDetails(hearingDetails[1]);
  await I.goToNextPage();
  manageHearingsEventPage.enterJudgeName(hearingDetails[1].judgeAndLegalAdvisor.judgeLastName);
  await I.goToNextPage();
  manageHearingsEventPage.dontSendNoticeOfHearing();
  await I.completeEvent('Save and continue');

  caseViewPage.selectTab(caseViewPage.tabs.hearings);

  I.seeInTab(['Hearing 2', 'Type of hearing'], hearingDetails[1].caseManagement);
  I.seeInTab(['Hearing 2', 'Court'], hearingDetails[1].venue);
  I.seeInTab(['Hearing 2', 'In person or remote'], hearingDetails[1].presence);
  I.seeInTab(['Hearing 2', 'Start date and time'], formatHearingTime(hearingDetails[1].startDate));
  I.seeInTab(['Hearing 2', 'End date and time'], formatHearingTime(hearingDetails[1].endDate));
  I.seeInTab(['Hearing 2', 'Allocated judge or magistrate'], 'Her Honour Judge Moley');

  I.seeInTab(['Adjourned or vacated hearing 2', 'Type of hearing'], hearingDetails[1].caseManagement);
  I.seeInTab(['Adjourned or vacated hearing 2', 'Start date and time'], '1 Jan 2060, 11:00:00 AM');
  I.seeInTab(['Adjourned or vacated hearing 2', 'Status'], 'Vacated');

  caseViewPage.selectTab(caseViewPage.tabs.documents);

  I.seeInTab(['Further evidence documents for hearings 2', 'Hearing'], 'Case management hearing, 1 January 2060');
});

Scenario('HMCTS admin cancels and re-lists hearing', async ({I, caseViewPage, manageHearingsEventPage}) => {
  await caseViewPage.goToNewActions(config.administrationActions.manageHearings);
  manageHearingsEventPage.selectVacateHearing('Case management hearing, 1 January 2060');
  await I.goToNextPage();
  manageHearingsEventPage.selectCancellationAction('Yes - but I do not have the new date yet');
  await I.goToNextPage();
  manageHearingsEventPage.selectCancellationReasonType('Other lawyers');
  manageHearingsEventPage.selectCancellationReason('No key issue analysis');
  await I.completeEvent('Save and continue');

  caseViewPage.selectTab(caseViewPage.tabs.hearings);
  I.seeInTab(['Adjourned or vacated hearing 3', 'Status'], 'Vacated - to be re-listed');

  caseViewPage.selectTab(caseViewPage.tabs.documents);
  I.seeInTab(['Further evidence documents for hearings 2', 'Hearing'], 'Case management hearing, 1 January 2060 - vacated');

  await caseViewPage.goToNewActions(config.administrationActions.manageHearings);
  manageHearingsEventPage.selectReListHearing('Case management hearing, 1 January 2060 - vacated');
  await I.goToNextPage();
  await manageHearingsEventPage.enterHearingDetails(hearingDetails[2]);
  await I.goToNextPage();
  await I.goToNextPage();
  manageHearingsEventPage.dontSendNoticeOfHearing();
  await I.completeEvent('Save and continue');

  caseViewPage.selectTab(caseViewPage.tabs.hearings);
  I.seeInTab(['Hearing 2', 'Type of hearing'], hearingDetails[1].caseManagement);
  I.seeInTab(['Hearing 2', 'Court'], hearingDetails[1].venue);
  I.seeInTab(['Hearing 2', 'Court address', 'Building and Street'], hearingDetails[1].venueCustomAddress.buildingAndStreet.lineOne);
  I.seeInTab(['Hearing 2', 'Court address', 'Address Line 2'], hearingDetails[1].venueCustomAddress.buildingAndStreet.lineTwo);
  I.seeInTab(['Hearing 2', 'Court address', 'Address Line 3'], hearingDetails[1].venueCustomAddress.buildingAndStreet.lineThree);
  I.seeInTab(['Hearing 2', 'Court address', 'Town or City'], hearingDetails[1].venueCustomAddress.town);
  I.seeInTab(['Hearing 2', 'Court address', 'Postcode/Zipcode'], hearingDetails[1].venueCustomAddress.postcode);
  I.seeInTab(['Hearing 2', 'Court address', 'Country'], hearingDetails[1].venueCustomAddress.country);
  I.seeInTab(['Hearing 2', 'In person or remote'], hearingDetails[1].presence);
  I.seeInTab(['Hearing 2', 'Start date and time'], formatHearingTime(hearingDetails[2].startDate));
  I.seeInTab(['Hearing 2', 'End date and time'], formatHearingTime(hearingDetails[2].endDate));
  I.seeInTab(['Hearing 2', 'Allocated judge or magistrate'], 'Her Honour Judge Moley');

  I.seeInTab(['Adjourned or vacated hearing 2', 'Status'], 'Vacated');

  caseViewPage.selectTab(caseViewPage.tabs.documents);
  I.seeInTab(['Further evidence documents for hearings 2', 'Hearing'], 'Case management hearing, 11 January 2060');
});

Scenario('HMCTS admin adds past hearing', async ({I, caseViewPage, manageHearingsEventPage}) => {
  hearingStartDate = moment().subtract(10,'m').toDate();
  hearingEndDate = moment(hearingStartDate).add(5,'m').toDate();

  const correctedHearingStartDate = moment().subtract(10,'m').toDate();
  const correctedHearingEndDate = moment(correctedHearingStartDate).add(5,'m').toDate();

  await caseViewPage.goToNewActions(config.administrationActions.manageHearings);
  manageHearingsEventPage.selectAddNewHearing();
  await I.goToNextPage();

  await manageHearingsEventPage.enterHearingDetails({startDate: hearingStartDate, endDate: hearingEndDate, presence: hearingDetails[0].presence});
  manageHearingsEventPage.selectPreviousVenue();
  await I.goToNextPage();

  manageHearingsEventPage.selectHearingDateIncorrect();
  await manageHearingsEventPage.enterCorrectedHearingDate({startDate: correctedHearingStartDate, endDate: correctedHearingEndDate});

  await I.goToNextPage();
  manageHearingsEventPage.enterJudgeDetails(hearingDetails[0]);
  manageHearingsEventPage.enterLegalAdvisorName(hearingDetails[0].judgeAndLegalAdvisor.legalAdvisorName);
  await I.goToNextPage();
  manageHearingsEventPage.sendNoticeOfHearingWithNotes(hearingDetails[0].additionalNotes);
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.manageHearings);

  caseViewPage.selectTab(caseViewPage.tabs.hearings);

  I.seeInTab(['Hearing 3', 'Type of hearing'], hearingDetails[0].caseManagement);
  I.seeInTab(['Hearing 3', 'Court'], hearingDetails[0].venue);
  I.seeInTab(['Hearing 2', 'In person or remote'], hearingDetails[0].presence);
  I.seeInTab(['Hearing 3', 'Start date and time'],  formatHearingTime(correctedHearingStartDate));
  I.seeInTab(['Hearing 3', 'End date and time'], formatHearingTime(correctedHearingEndDate));
  I.seeInTab(['Hearing 3', 'Allocated judge or magistrate'], 'Her Honour Judge Moley');
  I.seeInTab(['Hearing 3', 'Hearing judge or magistrate'], 'Her Honour Judge Reed');
  I.seeInTab(['Hearing 3', 'Justices\' Legal Adviser\'s full name'], hearingDetails[0].judgeAndLegalAdvisor.legalAdvisorName);
  I.seeInTab(['Hearing 3', 'Additional notes'], hearingDetails[0].additionalNotes);
  I.seeInTab(['Hearing 3', 'Notice of hearing'], `Notice_of_hearing_${dateFormat(submittedAt, 'ddmmmm')}.pdf`);
});

const formatHearingTime = hearingDate => formatDate(hearingDate, 'd mmm yyyy, h:MM:ss TT');
const formatHearingDate = hearingDate => formatDate(hearingDate, 'd mmmm yyyy');
const formatDate = (date, format) => dateFormat(date instanceof Date ? date : dateToString(date), format);
