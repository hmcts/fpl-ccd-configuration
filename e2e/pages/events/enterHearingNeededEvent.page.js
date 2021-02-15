const {I} = inject();

module.exports = {

  fields: {
    timeFrame: {
      radioGroup: '#hearing_timeFrame',
      sameDay: '#hearing_timeFrame-Same\\ day',
      reason: '#hearing_reason',
    },

    hearingType: {
      radioGroup: '#hearing_type',
      contestedICO: '#hearing_type-Contested\\ interim\\ care\\ order',
      reason: '#hearing_type_GiveReason',
    },

    noticeWithoutHearing: {
      no: '#hearing_withoutNotice-No',
    },

    reducedNoticeHearing: {
      no: '#hearing_reducedNotice-No',
    },

    respondentsAware: {
      no: '#hearing_respondentsAware-No',
    },
  },

  async enterTimeFrame(reason = 'test reason') {
    //await I.runAccessibilityTest();
    I.click(this.fields.timeFrame.sameDay);
    I.fillField(this.fields.timeFrame.reason, reason);
  },

  enterHearingType(reason = 'test reason') {
    I.click(this.fields.hearingType.contestedICO);
    I.fillField(this.fields.hearingType.reason, reason);
  },

  enterWithoutNoticeHearing() {
    I.click(this.fields.noticeWithoutHearing.no);
  },

  enterReducedHearing() {
    I.click(this.fields.reducedNoticeHearing.no);
  },

  enterRespondentsAware() {
    I.click(this.fields.respondentsAware.no);
  },
};
