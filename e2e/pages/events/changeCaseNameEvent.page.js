const { I } = inject();

module.exports = {

  fields: {
    caseName: '#caseName',
  },

  async changeCaseName(caseName = 'Craigavon council v Smith') {
    I.fillField(this.fields.caseName, caseName);
    await I.runAccessibilityTest();
  },
};
