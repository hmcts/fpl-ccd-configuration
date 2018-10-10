const I = actor();

module.exports = {

  fields: {
    username: '#username',
    password: '#password',
  },
  submitButton: 'Sign in',

  signIn(username, password) {
    I.amOnPage('http://localhost:3451');
    I.fillField(this.fields.username, username);
    I.fillField(this.fields.password, password);
    I.click(this.submitButton);
  }
};
