const I = actor();

module.exports = {

  fields: {
    timeFrame: {
      dropdown: {id: 'hearing_timeFrame'},
      sameDay: 'SAME_DAY',
      reason: '#hearing_reason',
    },

    hearingType: {
      dropdown: '#hearing_type',
      contestedICO: 'CONTESTED_INTERIM_CARE_ORDER',
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
    I.selectOption(this.fields.timeFrame.dropdown, this.fields.timeFrame.sameDay);
    I.fillField(this.fields.timeFrame.reason, reason);
  },

  enterHearingType() {
    I.selectOption(this.fields.hearingType.dropdown, this.fields.hearingType.contestedICO);
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
