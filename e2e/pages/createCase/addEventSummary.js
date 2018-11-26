const I = actor();

module.exports = {

  submitButton: 'Submit',

  submitCase() {
    I.click(this.submitButton);
    I.waitForElement('.tabs');
  },
};
