const {I} = inject();
const judgeAndLegalAdvisor = require('../../fragments/judgeAndLegalAdvisor');
const postcodeLookup = require('../../fragments/addressPostcodeLookup');

module.exports = {

  fields: {
    hearingOptions: {
      addNewHearing: '#hearingOption-NEW_HEARING',
      editHearing: '#hearingOption-EDIT_HEARING',
      adjournHearing: '#hearingOption-ADJOURN_HEARING',
      vacatedHearing: '#hearingOption-VACATE_HEARING',
    },
    hearingDateList: '#hearingDateList',
    pastAndTodayHearingDateList: '#pastAndTodayHearingDateList',
    futureAndTodayHearingDateList: '#futureAndTodayHearingDateList',
    hearingType: {
      final: '#hearingType-CASE_MANAGEMENT',
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
  },

  async selectAddNewHearing() {
    I.click(this.fields.hearingOptions.addNewHearing);
  },

  async selectEditHearing(hearing) {
    I.click(this.fields.hearingOptions.editHearing);
    I.selectOption(this.fields.hearingDateList, hearing);
  },

  selectAdjournHearing(hearing) {
    I.click(this.fields.hearingOptions.adjournHearing);
    I.selectOption(this.fields.pastAndTodayHearingDateList, hearing);
  },

  selectVacateHearing(hearing) {
    I.click(this.fields.hearingOptions.vacatedHearing);
    I.selectOption(this.fields.futureAndTodayHearingDateList, hearing);
  },

  selectCancellationReasonType(type){
    I.click(type);
  },

  selectCancellationReason(reason){
    I.selectOption('//select[not(@disabled)]', reason);
  },

  selectCancellationAction(action){
    I.click(action);
  },

  async enterHearingDetails(hearingDetails) {
    I.click(this.fields.hearingType.final);

    I.fillDateAndTime(hearingDetails.startDate, this.fields.startDate);
    I.fillDateAndTime(hearingDetails.endDate, this.fields.endDate);
  },

  async enterVenue(hearingDetails) {
    I.selectOption(this.fields.hearingVenue, hearingDetails.venue);
  },

  async selectPreviousVenue() {
    I.click(this.fields.usePreviousHearingVenue);
  },

  async enterNewVenue(hearingDetails) {
    I.click(this.fields.dontUsePreviousHearingVenue);
    I.selectOption(this.fields.newVenue, hearingDetails.venue);

    if (hearingDetails.venue === 'Other') {
      within(this.fields.newVenueCustomAddress, () => {
        postcodeLookup.enterAddressManually(hearingDetails.venueCustomAddress);
      });
    }
  },

  async enterJudgeDetails(hearingDetails) {
    judgeAndLegalAdvisor.useAlternateJudge();
    judgeAndLegalAdvisor.selectJudgeTitle();
    judgeAndLegalAdvisor.enterJudgeLastName(hearingDetails.judgeAndLegalAdvisor.judgeLastName);
    judgeAndLegalAdvisor.enterJudgeEmailAddress(hearingDetails.judgeAndLegalAdvisor.judgeEmail);
  },

  async enterLegalAdvisorName(legalAdvisorName) {
    judgeAndLegalAdvisor.enterLegalAdvisorName(legalAdvisorName);
  },

  async enterJudgeName(name) {
    judgeAndLegalAdvisor.enterJudgeLastName(name);
  },

  async selectedAllocatedJudge() {
    judgeAndLegalAdvisor.useAllocatedJudge();
  },

  async sendNoticeOfHearingWithNotes(notes) {
    I.click(this.fields.sendNotice);
    I.fillField(this.fields.noticeNotes, notes);
  },

  async dontSendNoticeOfHearing() {
    I.click(this.fields.dontSendNotice);
  },

};
