const {I} = inject();
const judgeAndLegalAdvisor = require('../../fragments/judgeAndLegalAdvisor');

module.exports = {

  fields: {
    hearingOptions: {
      addNewHearing: '#useExistingHearing-NEW_HEARING',
      editDraftHearing: '#useExistingHearing-EDIT_DRAFT',
    },
    hearingType: {
      final: '#hearingType-CASE_MANAGEMENT',
    },
    hearingVenue: '#hearingVenue',
    startDate: {
      second: '#hearingStartDate-second',
      minute: '#hearingStartDate-minute',
      hour: '#hearingStartDate-hour',
      day: '#hearingStartDate-day',
      month: '#hearingStartDate-month',
      year: '#hearingStartDate-year',
    },
    endDate: {
      second: '#hearingEndDate-second',
      minute: '#hearingEndDate-minute',
      hour: '#hearingEndDate-hour',
      day: '#hearingEndDate-day',
      month: '#hearingEndDate-month',
      year: '#hearingEndDate-year',
    },
    sendNoticeOfHearing: '#sendNoticeOfHearing-Yes',
    noticeOfHearingNotes: '#noticeOfHearingNotes',
  },

  // async selectAddNewHearing() {
  //   I.click(this.fields.hearingOptions.addNewHearing);
  // },
  //
  // async selectEditDraftHearing() {
  //   I.click(this.fields.hearingOptions.editDraftHearing);
  // },

  async enterHearingDetails(hearingDetails) {
    I.click(this.fields.hearingType.final);
    I.selectOption(this.fields.venue, hearingDetails.venue);

    I.fillField(this.fields.startDate.second, hearingDetails.startDate.second);
    I.fillField(this.fields.startDate.minute, hearingDetails.startDate.minute);
    I.fillField(this.fields.startDate.hour, hearingDetails.startDate.hour);
    I.fillField(this.fields.startDate.day, hearingDetails.startDate.day);
    I.fillField(this.fields.startDate.month, hearingDetails.startDate.month);
    I.fillField(this.fields.startDate.year, hearingDetails.startDate.year);
    I.fillField(this.fields.endDate.second, hearingDetails.endDate.second);
    I.fillField(this.fields.endDate.minute, hearingDetails.endDate.minute);
    I.fillField(this.fields.endDate.hour, hearingDetails.endDate.hour);
    I.fillField(this.fields.endDate.day, hearingDetails.endDate.day);
    I.fillField(this.fields.endDate.month, hearingDetails.endDate.month);
    I.fillField(this.fields.endDate.year, hearingDetails.endDate.year);
  },

  async enterJudgeAndLegalAdvisorDetails(hearingDetails) {
    judgeAndLegalAdvisor.selectJudgeTitle();
    judgeAndLegalAdvisor.enterJudgeLastName(hearingDetails.judgeAndLegalAdvisor.judgeLastName);
    judgeAndLegalAdvisor.enterLegalAdvisorName(hearingDetails.judgeAndLegalAdvisor.legalAdvisorName);
  },

  async enterNoticeOfHearingDetails() {
    I.click(this.fields.sendNoticeOfHearing);
    I.fillField(this.fields.noticeOfHearingNotes, 'Notes that will appear on the notice of hearing');
  },

};
