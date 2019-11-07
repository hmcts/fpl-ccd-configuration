const { I } = inject();

module.exports = {
  fields: {
    cmoHearingDateList: '#cmoHearingDateList',
  },
  draftCMO(date= '1 Jan 2050') {
    I.waitForElement(this.fields.cmoHearingDateList);
    I.selectOption(this.fields.cmoHearingDateList, date);
  },
};
