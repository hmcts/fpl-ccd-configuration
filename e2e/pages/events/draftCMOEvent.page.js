const { I } = inject();

module.exports = {
  fields: {
    recitals: {
      title: '#recitals_0_title',
      description: '#recitals_0_description',
    },
    cmoHearingDateList: '#cmoHearingDateList',
  },

  draftCMO(date= '1 Jan 2050') {
    I.waitForElement(this.fields.cmoHearingDateList);
    I.selectOption(this.fields.cmoHearingDateList, date);
    I.click('Continue');
  },

  enterRecital(recitals) {
    I.click('Add new');
    I.fillField(this.fields.recitals.title, recitals.title);
    I.fillField(this.fields.recitals.description, recitals.description);
  },
};
