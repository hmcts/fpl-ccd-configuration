const I = actor();

module.exports = {

  submitButton: 'Save and continue',

  submitCase() {
    I.click(this.submitButton);
    I.waitForElement('.tabs');
  },
};
