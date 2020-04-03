const {I} = inject();

module.exports = {
  fields: {
    venue: '#expertReport_0_expertReportList',
  },

  addExpertReportLog() {
    I.click('Add new');
    I.selectOption(this.fields.venue, 'Peadiatric');
  },
};
