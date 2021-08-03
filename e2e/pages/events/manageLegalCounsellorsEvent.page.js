const {I} = inject();

module.exports = {
  fields: function (index) {

    return {
      legalCounsellor: {
        firstName: `#legalCounsellors_${index}_firstName`,
        lastName: `#legalCounsellors_${index}_lastName`,
        organisationGroup: `#legalCounsellors_${index}_${index}`,
        email: `#legalCounsellors_${index}_email`,
        telephone: `#legalCounsellors_${index}_telephoneNumber`,
      },
    };
  },

  async addLegalCounsellor(legalRepresentative) {

    const elementIndex = await this.getActiveElementIndex();

    if(legalRepresentative.firstName) {
      I.fillField(this.fields(elementIndex).legalCounsellor.firstName, legalRepresentative.firstName);
    }
    if(legalRepresentative.lastName) {
      I.fillField(this.fields(elementIndex).legalCounsellor.lastName, legalRepresentative.lastName);
    }
    if(legalRepresentative.email) {
      I.fillField(this.fields(elementIndex).legalCounsellor.email, legalRepresentative.email);
    }
    if(legalRepresentative.telephone) {
      I.fillField(this.fields(elementIndex).legalCounsellor.telephone, legalRepresentative.telephone);
    }
    if(legalRepresentative.organisation) {
      await within(this.fields(elementIndex).legalCounsellor.organisationGroup, async () => {
        I.fillField('#search-org-text', legalRepresentative.organisation);
        I.click('Select');
      });
    }
    await I.runAccessibilityTest();
  },

  async getActiveElementIndex() {
    return await I.grabNumberOfVisibleElements('//button[text()="Remove"]') - 1;
  },
};
