const { I } = inject();

module.exports = {

  fields: {
    username: '#username',
    password: '#password',
  },
  submitButton: 'input[value="Sign in"]',

  async signIn(user) {
    I.fillField(this.fields.username, user.email);
    I.fillField(this.fields.password, user.password);
    I.click(this.submitButton);
  },

};
