const I = actor();

module.exports = {

  fields: {
    timeFrame: {
      radioGroup: '#hearing_timeFrame',
      sameDay: 'Same day',
      reason: '#hearing_reason',
    },

    hearingType: {
      radioGroup: '#hearing_type',
      contestedICO: 'Contested interim care order',
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
    within(this.fields.timeFrame.radioGroup, () => {
      I.click(locate('label').withText(this.fields.timeFrame.sameDay));
    });
    I.fillField(this.fields.timeFrame.reason, reason);
  },

  enterHearingType() {
    within(this.fields.hearingType.radioGroup, () => {
      I.click(locate('label').withText(this.fields.hearingType.contestedICO));
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
