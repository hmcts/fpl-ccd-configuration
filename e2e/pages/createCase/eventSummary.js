const I = actor();

module.exports = {

  submit(button) {
    I.click(button);
    I.waitForNavigation({ waitUntil: 'networkidle0' });
    I.waitForElement('.alert-success');
  },
};
