const { I } = inject();

module.exports = {

  fields: {
    username: '#username',
    password: '#password',
  },
  submitButton: 'input[value="Sign in"]',
  signOut:'a[contains(text(),"Sign out")]',

  async signIn(user) {

    console.log('login page signIn');
    await I.waitForSelector(this.fields.username);
    await I.grabCurrentUrl();
    I.fillField(this.fields.username, user.email);
    I.fillField(this.fields.password, user.password);

    await I.waitForSelector(this.submitButton);
    I.click(this.submitButton);
    if (await I.waitForSelector(this.signOut, 30) == null) {
      //console.log("before refresh");
      await I.refreshPage();
      I.grabCurrentUrl();
    }

    //console.log(await I.grabCurrentUrl());
  },

};
