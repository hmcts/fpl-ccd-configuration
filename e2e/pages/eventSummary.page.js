const { I } = inject();

module.exports = {

  fields: {
    summary: '#field-trigger-summary',
    description: '#field-trigger-description',
  },

  provideSummary(summary, description) {
    I.fillField(this.fields.summary, summary);
    I.fillField(this.fields.description, description);
    I.runAccessibilityTest();
  },

  async submit(button, locator = '.alert-success') {
    await I.retryUntilExists(() => I.click(button), locator);
  },
};
