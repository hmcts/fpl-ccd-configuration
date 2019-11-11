const { I } = inject();

module.exports = {
  fields: {
    recitals: {
      title: '#recitals_0_title',
      description: '#recitals_0_description',
    },
    cmoHearingDateList: '#cmoHearingDateList',
  },

  associateHearingDate(date) {
    I.waitForElement(this.fields.cmoHearingDateList);
    I.selectOption(this.fields.cmoHearingDateList, date);
  },

  enterRecital(recitals) {
    I.click('Add new');
    I.fillField(this.fields.recitals.title, recitals.title);
    I.fillField(this.fields.recitals.description, recitals.description);
  },
};
