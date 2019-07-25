const I = actor();

module.exports = {

  submit(button) {
    I.click(button);
    I.waitForElement('.alert-success');
  },
};
