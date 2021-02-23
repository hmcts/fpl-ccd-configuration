const {I} = inject();

module.exports = {
  fields: function (index) {

    return {
      legalRepresentative: {
        fullName: `#legalRepresentatives_${index}_fullName`,
        roles: {
          solicitor: `#legalRepresentatives_${index}_role-EXTERNAL_LA_SOLICITOR`,
          barrister: `#legalRepresentatives_${index}_role-EXTERNAL_LA_BARRISTER`,
        },
        organisation: `#legalRepresentatives_${index}_organisation`,
        email: `#legalRepresentatives_${index}_email`,
        telephone: `[id="legalRepresentatives_${index}_telephoneNumber"]`,
      },
    };
  },

  async addLegalRepresentative(legalRepresentative) {

    const elementIndex = await this.getActiveElementIndex();

    if(legalRepresentative.fullName) {
      I.fillField(this.fields(elementIndex).legalRepresentative.fullName, legalRepresentative.fullName);
    }
    if(legalRepresentative.role) {
      await this.setRolePreferences(legalRepresentative.role);
    }
    if(legalRepresentative.organisation) {
      I.fillField(this.fields(elementIndex).legalRepresentative.organisation, legalRepresentative.organisation);
    }
    if(legalRepresentative.email) {
      I.fillField(this.fields(elementIndex).legalRepresentative.email, legalRepresentative.email);
    }
    if(legalRepresentative.telephone) {
      I.fillField(this.fields(elementIndex).legalRepresentative.telephone, legalRepresentative.telephone);
    }
    await I.runAccessibilityTest();
  },

  async setRolePreferences(rolePreferences) {
    const elementIndex = await this.getActiveElementIndex();

    switch (rolePreferences) {
      case 'Barrister':
        I.checkOption(this.fields(elementIndex).legalRepresentative.roles.barrister);
        break;
      case 'Solicitor':
        I.checkOption(this.fields(elementIndex).legalRepresentative.roles.solicitor);
        break;
      default:
        throw new Error(`Unsupported representative serving preferences ${rolePreferences}`);
    }
  },

  async getActiveElementIndex() {
    return await I.grabNumberOfVisibleElements('//button[text()="Remove"]') - 1;
  },
};
