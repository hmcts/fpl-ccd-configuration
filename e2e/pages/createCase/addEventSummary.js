const I = actor();

module.exports = {

  fields: {
    summary: '#field-trigger-summary',
    description: '#field-trigger-description',
  },
  submitButton: 'Submit',

  submitCase(summary, description) {
    I.fillField(this.fields.summary, summary);
    I.fillField(this.fields.description, description);
    I.click(this.submitButton);
  },
};
