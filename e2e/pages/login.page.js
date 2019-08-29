const I = actor();

module.exports = {

  fields: {
    username: '#username',
    password: '#password',
  },
  submitButton: 'input[value="Sign in"]',

  signIn(username, password) {
    I.waitForElement(this.submitButton);
    I.fillField(this.fields.username, username);
    I.fillField(this.fields.password, password);
    I.click(this.submitButton);
    I.waitForText('Sign Out');
  },
};
