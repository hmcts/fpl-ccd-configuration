const {I} = inject();
const judgeAndLegalAdvisor = require('../../fragments/judgeAndLegalAdvisor');
const postcodeLookup = require('../../fragments/addressPostcodeLookup');

module.exports = {

  fields: {
    hearingOptions: {
      addNewHearing: '#hearingOption-NEW_HEARING',
      editHearing: '#hearingOption-EDIT_HEARING',
      adjournHearing: '#hearingOption-ADJOURN_HEARING',
      vacateHearing: '#hearingOption-VACATE_HEARING',
      reListHearing: '#hearingOption-RE_LIST_HEARING',
    },
    presence: {
      inPerson: '#hearingPresence-IN_PERSON',
      remote: '#hearingPresence-REMOTE',
    },
    hearingDateList: '#hearingDateList',
    pastAndTodayHearingDateList: '#pastAndTodayHearingDateList',
    futureAndTodayHearingDateList: '#futureAndTodayHearingDateList',
    toReListHearingDateList: '#toReListHearingDateList',
    hearingType: {
      caseManagement: '#hearingType-CASE_MANAGEMENT',
    },
    hearingVenue: '#hearingVenue',
    usePreviousHearingVenue: '#previousHearingVenue_usePreviousVenue-Yes',
    dontUsePreviousHearingVenue: '#previousHearingVenue_usePreviousVenue-No',
    newVenue: '#previousHearingVenue_newVenue',
    newVenueCustomAddress: '#previousHearingVenue_newVenueCustomAddress_newVenueCustomAddress',
    startDate: '#hearingStartDate',
    endDate: '#hearingEndDate',
    sendNotice: '#sendNoticeOfHearing-Yes',
    dontSendNotice: '#sendNoticeOfHearing-No',
    noticeNotes: '#noticeOfHearingNotes',
    confirmHearingDate: {
      hearingDateCorrect: '#confirmHearingDate-Yes',
      hearingDateIncorrect: '#confirmHearingDate-No',
    },
    correctedStartDate: '#hearingStartDateConfirmation',
    correctedEndDate: '#hearingEndDateConfirmation',
  },

  selectAddNewHearing() {
    I.click(this.fields.hearingOptions.addNewHearing);
  },

  selectEditHearing(hearing) {
    I.click(this.fields.hearingOptions.editHearing);
    I.selectOption(this.fields.hearingDateList, hearing);
  },

  selectAdjournHearing(hearing) {
    I.click(this.fields.hearingOptions.adjournHearing);
    I.selectOption(this.fields.pastAndTodayHearingDateList, hearing);
  },

  selectVacateHearing(hearing) {
    I.click(this.fields.hearingOptions.vacateHearing);
    I.selectOption(this.fields.futureAndTodayHearingDateList, hearing);
  },

  selectReListHearing(hearing) {
    I.click(this.fields.hearingOptions.reListHearing);
    I.selectOption(this.fields.toReListHearingDateList, hearing);
  },

  selectCancellationReasonType(type) {
    I.click(type);
  },

  selectCancellationReason(reason) {
    I.selectOption('//select[not(@disabled)]', reason);
  },

  selectCancellationAction(action) {
    I.click(action);
  },

  async enterHearingDetails(hearingDetails) {
    I.click(this.fields.hearingType.caseManagement);

    if (hearingDetails.presence) {
      if (hearingDetails.presence === 'Remote') {
        this.selectRemoteHearing();
      } else {
        this.selectInPersonHearing();
      }
    }

    await I.fillDateAndTime(hearingDetails.startDate, this.fields.startDate);
    await I.fillDateAndTime(hearingDetails.endDate, this.fields.endDate);
  },

  enterVenue(hearingDetails) {
    I.selectOption(this.fields.hearingVenue, hearingDetails.venue);
  },

  selectPreviousVenue() {
    I.click(this.fields.usePreviousHearingVenue);
  },

  async enterNewVenue(hearingDetails) {
    I.click(this.fields.dontUsePreviousHearingVenue);
    I.selectOption(this.fields.newVenue, hearingDetails.venue);

    if (hearingDetails.venue === 'Other') {
      await within(this.fields.newVenueCustomAddress, () => {
        postcodeLookup.enterAddressManually(hearingDetails.venueCustomAddress);
      });
    }
  },

  selectInPersonHearing() {
    I.click(this.fields.presence.inPerson);
  },

  selectRemoteHearing() {
    I.click(this.fields.presence.remote);
  },

  enterJudgeDetails(hearingDetails) {
    // occasionally this page would take a while to load so waiting until the use allocated judge field is visible
    I.waitForVisible(`#${judgeAndLegalAdvisor.fields.useAllocatedJudge.groupName}`, 10);
    judgeAndLegalAdvisor.useAlternateJudge();
    judgeAndLegalAdvisor.selectJudgeTitle();
    I.wait(2);
    judgeAndLegalAdvisor.enterJudgeLastName(hearingDetails.judgeAndLegalAdvisor.judgeLastName);
    judgeAndLegalAdvisor.enterJudgeEmailAddress(hearingDetails.judgeAndLegalAdvisor.judgeEmail);
  },

  enterLegalAdvisorName(legalAdvisorName) {
    judgeAndLegalAdvisor.enterLegalAdvisorName(legalAdvisorName);
  },

  enterJudgeName(name) {
    judgeAndLegalAdvisor.enterJudgeLastName(name);
  },

  selectedAllocatedJudge() {
    judgeAndLegalAdvisor.useAllocatedJudge();
  },

  sendNoticeOfHearingWithNotes(notes) {
    I.click(this.fields.sendNotice);
    I.wait(2);
    I.fillField(this.fields.noticeNotes, notes);
  },

  dontSendNoticeOfHearing() {
    I.click(this.fields.dontSendNotice);
  },

  selectHearingDateIncorrect() {
    I.click(this.fields.confirmHearingDate.hearingDateIncorrect);
  },

  async enterCorrectedHearingDate(hearingDetails) {
    await I.runAccessibilityTest();
    await I.fillDateAndTime(hearingDetails.startDate, this.fields.correctedStartDate);
    await I.fillDateAndTime(hearingDetails.endDate, this.fields.correctedEndDate);
  },

};
