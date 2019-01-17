/* global locate */

const I = actor();

module.exports = {

  fields: {
    timeFrame: {
      dropdown: {id: 'hearing_timeFrame'},
      sameDay: 'Same day',
      reason: '#hearing_reason',
    },

    hearingType: {
      standardCaseManagement: locate('input').withAttr({id: 'hearing_type-STANDARD_CASE_HEARING'}),
      urgentPreliminary: locate('input').withAttr({id: 'hearing_type-URGENT_PRELIMINARY_HEARING'}),
      contestedInterimCareOrder: locate('input').withAttr({id: 'hearing_type-CONTESTED_INTERIM_HEARING'}),
      EmergencyProtectionOrder: locate('input').withAttr({id: 'hearing_type-EMERGENCY_PROTECTION_HEARING'}),
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
    I.checkOption(this.fields.hearingType.contestedInterimCareOrder);
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
