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
    },

    noticeWithoutHearing: {
      yes: '#hearing_withoutNotice-Yes',
    },

    reducedNoticeHearing: {
      no: '#hearing_reducedNotice-No',
    },

    respondentsAware: {
      yes: '#hearing_respondentsAware-Yes',
    },
  },

  enterTimeFrame(reason = 'test reason') {
    I.click(this.fields.timeFrame.sameDay);
    // I.runAccessibilityTest();
    I.fillField(this.fields.timeFrame.reason, reason);
  },

  enterHearingType() {
    I.click(this.fields.hearingType.contestedICO);
  },

  enterWithoutNoticeHearing() {
    I.click(this.fields.noticeWithoutHearing.yes);
  },

  enterReducedHearing() {
    I.click(this.fields.reducedNoticeHearing.no);
  },

  enterRespondentsAware() {
    I.click(this.fields.respondentsAware.yes);
  },
};
