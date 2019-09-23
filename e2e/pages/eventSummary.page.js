const { I } = inject();

module.exports = {

  fields: {
    summary: '#field-trigger-summary',
    description: '#field-trigger-description',
  },

  provideSummary(summary, description) {
    I.fillField(this.fields.summary, summary);
    I.fillField(this.fields.description, description);
  },

  submit(button) {
    I.retryUntilExists(() => I.click(button), '.alert-success');
  },
};
