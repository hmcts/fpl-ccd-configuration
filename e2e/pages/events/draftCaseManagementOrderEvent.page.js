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

  validatePreviousSelectedHearingDate(date) {
    I.waitForElement(this.fields.cmoHearingDateList);
    I.see(date,this.fields.cmoHearingDateList);
  },

  async enterRecital(title,description) {
    I.fillField(this.fields.recitals.title, title);
    I.fillField(this.fields.recitals.description, description);
  },
};
