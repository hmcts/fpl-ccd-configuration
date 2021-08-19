const {I} = inject();

module.exports = {
  fields: {
    localAuthoritiesList: '#localAuthoritiesToShare',
    localAuthorityEmail: '#localAuthorityEmail',
  },

  selectAddLocalAuthority() {
    I.click('Give case access to another local authority');
  },

  selectRemoveLocalAuthority() {
    I.click('Remove case access from local authority');
  },

  async selectLocalAuthority(name) {
    I.selectOption(this.fields.localAuthoritiesList, name);
    await I.runAccessibilityTest();
  },

  async setEmailAddress(email) {
    I.fillField(this.fields.localAuthorityEmail, email);
    await I.runAccessibilityTest();
  },

  async getEmailAddress() {
    return await I.grabValueFrom(this.fields.localAuthorityEmail);
  },

};
