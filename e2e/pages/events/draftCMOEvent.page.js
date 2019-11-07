const { I } = inject();

module.exports = {
  fields: {
    cmoHearingDateList: '#cmoHearingDateList',
  },
  async draftCMO(date= '1 Jan 2050') {
    await I.waitForElement(this.fields.cmoHearingDateList);
    I.selectOption(this.fields.cmoHearingDateList, date);
  },
};
