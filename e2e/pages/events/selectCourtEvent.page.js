const {I} = inject();

module.exports = {
  fields: {
    courtList: '#courtsList',
  },

  async selectCourt(court) {
    await I.runAccessibilityTest();
    I.selectOption(this.fields.courtList, court);
  },
};
