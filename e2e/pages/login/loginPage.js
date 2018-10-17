/*global process*/

const I = actor();

module.exports = {

  fields: {
    username: '#username',
    password: '#password',
  },
  submitButton: 'input[value="Sign in"]',

  signIn(username, password) {
    I.amOnPage(process.env.URL);
    I.waitForElement(this.submitButton, 5);
    I.fillField(this.fields.username, username);
    I.fillField(this.fields.password, password);
    I.click(this.submitButton);
  },
};
