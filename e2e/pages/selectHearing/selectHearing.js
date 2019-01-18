const I = actor();

module.exports = {

  fields: {
    timeFrame: {
      dropdown: {id: 'hearing_timeFrame'},
      sameDay: 'Same day',
      reason: '#hearing_reason',
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

  checkHearingTypes(...hearingTypes) {
    hearingTypes.forEach(type => {
      I.checkOption(type);
    });
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
