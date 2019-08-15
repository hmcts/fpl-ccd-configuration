const I = actor();

module.exports = {

  fields: {
    summary: '#field-trigger-summary',
    description: '#field-trigger-description',
  },

  submit(button) {
    I.click(button);
    I.waitForElement('.alert-success');
  },

  provideSummaryAndSubmit(button, summary, description) {
    I.fillField(this.fields.summary, summary);
    I.fillField(this.fields.description, description);
    this.submit(button);
  },
};
