const { I } = inject();

module.exports = {
  fields: {
    cmoHearingDateList: '#cmoHearingDateList',
  },

  associateHearingDate(date) {
    I.waitForElement(this.fields.cmoHearingDateList);
    I.selectOption(this.fields.cmoHearingDateList, date);
  },
};
