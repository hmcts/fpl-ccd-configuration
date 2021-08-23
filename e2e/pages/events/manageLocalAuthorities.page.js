const {I} = inject();

module.exports = {
  fields: {
    localAuthoritiesList: '#localAuthoritiesToShare',
    localAuthorityEmail: '#localAuthorityEmail',
    transfer: {
      localAuthoritiesList: '#localAuthoritiesToTransfer',
      toSharedLocalAuthority: '#transferToSharedLocalAuthority_Yes',
      solicitor: {
        name: '#localAuthorityToTransferSolicitor_fullName',
        email: '#localAuthorityToTransferSolicitor_email',
      },
      court: {
        change: '#transferToCourt_Yes',
        same: '#transferToCourt_No',
        list: '#courtsToTransfer',
      },
    },
  },

  selectAddLocalAuthority() {
    I.click('Give case access to another local authority');
  },

  selectRemoveLocalAuthority() {
    I.click('Remove case access from local authority');
  },

  selectTransferLocalAuthority() {
    I.click('Transfer the case to another local authority');
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

  selectSharedLocalAuthorityToTransfer() {
    I.click(this.fields.transfer.toSharedLocalAuthority);
  },

  selectLocalAuthorityToTransfer(name) {
    I.selectOption(this.fields.transfer.localAuthoritiesList, name);
  },

  fillSolicitorDetails(name, email) {
    I.fillField(this.fields.transfer.solicitor.name, name);
    I.fillField(this.fields.transfer.solicitor.email, email);
  },

  selectChangeCourt(){
    I.click(this.fields.transfer.court.change);
  },

  selectSameCourt(){
    I.click(this.fields.transfer.court.same);
  },

  selectCourt(court){
    I.selectOption(this.fields.transfer.court.list, court);
  },

};
