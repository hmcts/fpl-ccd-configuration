/* global locate */

const I = actor();

module.exports = {

  fields: {
    timeFrame: {
      sameDay: locate('input').withAttr({id: 'hearing_timeFrame-Same day'}),
      reason: '#hearing_reason',
    },

    hearingType: {
      contestedICO: locate('input').withAttr({id: 'hearing_type-Contested interim care order'}),
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
    I.checkOption(this.fields.timeFrame.sameDay);
    I.fillField(this.fields.timeFrame.reason, reason);
  },

  enterHearingType() {
    I.checkOption(this.fields.hearingType.contestedICO);
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
