const { I } = inject();

module.exports = {

  fields: {
    summary: '#field-trigger-summary',
    description: '#field-trigger-description',
  },

  async submit(button) {
    await I.retryUntilExists(() => I.click(button), '.alert-success');
  },

  provideSummaryAndSubmit(button, summary, description) {
    I.fillField(this.fields.summary, summary);
    I.fillField(this.fields.description, description);
    this.submit(button);
  },
};
