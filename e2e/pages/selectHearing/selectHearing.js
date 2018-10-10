
const I = actor();

module.exports = {

  fields: {
    timeFrame: {
      dropdown: {id: 'hearing_TimeFrame'},
      sameDayField: 'Same day',
      within7Field: 'Within 7 days',
    },

    hearingType: {
      dropdown: '#hearing_Type',
      contestedICO: 'Contested interim care order'
    },

    noticeWithoutHearing: {
      yes: '#hearing_WithoutNotice-Yes'
    },

    reducedNoticeHearing: {
      no: '#hearing_ReducedNotice-No'
    },

    respondentsAware: {
      yes: '#hearing_RespondentsAware-Yes'
    }
  },

  halfFillForm() {
    I.selectOption(this.fields.timeFrame.dropdown, this.fields.timeFrame.sameDayField);
    I.selectOption(this.fields.hearingType.dropdown, this.fields.hearingType.contestedICO);
    I.click(this.fields.noticeWithoutHearing.yes);
  },

  fillForm() {
    I.selectOption(this.fields.timeFrame.dropdown, this.fields.timeFrame.within7Field);
    I.selectOption(this.fields.hearingType.dropdown, this.fields.hearingType.contestedICO);
    I.click(this.fields.noticeWithoutHearing.yes);
    I.click(this.fields.reducedNoticeHearing.no);
    I.click(this.fields.respondentsAware.yes);
  }
};
