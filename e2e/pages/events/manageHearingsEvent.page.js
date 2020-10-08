const {I} = inject();
const judgeAndLegalAdvisor = require('../../fragments/judgeAndLegalAdvisor');
const postcodeLookup = require('../../fragments/addressPostcodeLookup');

module.exports = {

  fields: {
    hearingOptions: {
      addNewHearing: '#useExistingHearing-NEW_HEARING',
      editDraftHearing: '#useExistingHearing-EDIT_DRAFT',
    },
    hearingDateList: '#hearingDateList',
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
    I.click(this.fields.hearingOptions.editDraftHearing);
    I.selectOption(this.fields.hearingDateList, hearing);
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

  async enterJudgeAndLegalAdvisorDetails(hearingDetails) {
    judgeAndLegalAdvisor.useAlternateJudge();
    judgeAndLegalAdvisor.selectJudgeTitle();
    judgeAndLegalAdvisor.enterJudgeLastName(hearingDetails.judgeAndLegalAdvisor.judgeLastName);
    judgeAndLegalAdvisor.enterLegalAdvisorName(hearingDetails.judgeAndLegalAdvisor.legalAdvisorName);
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
