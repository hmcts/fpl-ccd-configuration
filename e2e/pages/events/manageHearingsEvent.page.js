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
    attendance: '#hearingAttendance',
    attendanceDetails: '#hearingAttendanceDetails',
    preAttendanceDetails: '#preHearingAttendanceDetails',
    hearingDateList: '#hearingDateList',
    pastAndTodayHearingDateList: '#pastAndTodayHearingDateList',
    futureAndTodayHearingDateList: '#futureAndTodayHearingDateList',
    toReListHearingDateList: '#toReListHearingDateList',
    hearingType: {
      caseManagement: '#hearingType-CASE_MANAGEMENT',
    },
    hearingVenue: '#hearingVenue',
    usePreviousHearingVenue: '#previousHearingVenue_usePreviousVenue_Yes',
    dontUsePreviousHearingVenue: '#previousHearingVenue_usePreviousVenue_No',
    newVenue: '#previousHearingVenue_newVenue',
    newVenueCustomAddress: '#previousHearingVenue_newVenueCustomAddress_newVenueCustomAddress',
    startDate: '#hearingStartDate',
    endDate: '#hearingEndDate',
    sendNotice: '#sendNoticeOfHearing_Yes',
    dontSendNotice: '#sendNoticeOfHearing_No',
    noticeNotes: '#noticeOfHearingNotes',
    allOthers: {
      group: '#sendOrderToAllOthers',
      options: {
        all: 'Yes',
        select: 'No',
      },
    },
    otherSelector: {
      selector: index => `#othersSelector_option${index}-SELECTED`,
    },
    confirmHearingDate: {
      hearingDateCorrect: '#confirmHearingDate_Yes',
      hearingDateIncorrect: '#confirmHearingDate_No',
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

    if (hearingDetails.attendance) {
      hearingDetails.attendance.forEach(I.checkOption);
    }

    if (hearingDetails.attendanceDetails) {
      I.fillField(this.fields.attendanceDetails, hearingDetails.attendanceDetails);
    }

    await I.fillDateAndTime(hearingDetails.startDate, this.fields.startDate);
    await I.fillDateAndTime(hearingDetails.endDate, this.fields.endDate);

    if (hearingDetails.preAttendanceDetails) {
      I.fillField(this.fields.preAttendanceDetails, hearingDetails.preAttendanceDetails);
    }
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

  enterJudgeDetails(hearingDetails) {
    // occasionally this page would take a while to load so waiting until the use allocated judge field is visible
    I.waitForVisible(`#${judgeAndLegalAdvisor.fields.useAllocatedJudge.groupName}`, 10);
    judgeAndLegalAdvisor.useAlternateJudge();
    judgeAndLegalAdvisor.selectJudgeTitle();
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
    I.fillField(this.fields.noticeNotes, notes);
  },

  dontSendNoticeOfHearing() {
    I.click(this.fields.dontSendNotice);
  },

  selectOthers(option, indexes = []) {
    I.click(`${this.fields.allOthers.group}_${option}`);

    indexes.forEach((selectorIndex) => {
      I.checkOption(this.fields.otherSelector.selector(selectorIndex));
    });

  },

  selectHearingDateIncorrect() {
    I.click(this.fields.confirmHearingDate.hearingDateIncorrect);
  },

  async enterCorrectedHearingDate(hearingDetails) {
    await I.runAccessibilityTest();
    await I.fillDateAndTime(hearingDetails.startDate, this.fields.correctedStartDate);
    await I.fillDateAndTime(hearingDetails.endDate, this.fields.correctedEndDate);
  },

  async grabPreHearingAttendance(){
    return await I.grabValueFrom(this.fields.preAttendanceDetails);
  },

};
