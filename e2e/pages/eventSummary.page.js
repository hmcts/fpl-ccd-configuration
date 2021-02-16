const { I } = inject();

module.exports = {

  fields: {
    summary: '#field-trigger-summary',
    description: '#field-trigger-description',
  },

  async provideSummary(summary, description) {
    //await I.runAccessibilityTest();
    I.fillField(this.fields.summary, summary);
    I.fillField(this.fields.description, description);
  },

  async submit(button, locator = '.alert-success') {
    await I.retryUntilExists(() => I.click(button), locator);
  },
};
