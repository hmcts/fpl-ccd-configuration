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

  selectLocalAuthority(name) {
    I.selectOption(this.fields.localAuthoritiesList, name);
  },

  setEmailAddress(email) {
    I.fillField(this.fields.localAuthorityEmail, email);
  },

  async getEmailAddress() {
    return await I.grabValueFrom(this.fields.localAuthorityEmail);
  },

};
