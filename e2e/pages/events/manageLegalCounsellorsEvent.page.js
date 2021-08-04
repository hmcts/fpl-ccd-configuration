const {I} = inject();
const organisationHelper = require('../../helpers/organisation_helper.js');

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

  async addLegalCounsellor(legalCounsellor) {

    const elementIndex = await this.getActiveElementIndex();

    if(legalCounsellor.firstName) {
      I.fillField(this.fields(elementIndex).legalCounsellor.firstName, legalCounsellor.firstName);
    }
    if(legalCounsellor.lastName) {
      I.fillField(this.fields(elementIndex).legalCounsellor.lastName, legalCounsellor.lastName);
    }
    if(legalCounsellor.email) {
      I.fillField(this.fields(elementIndex).legalCounsellor.email, legalCounsellor.email);
    }
    if(legalCounsellor.telephone) {
      I.fillField(this.fields(elementIndex).legalCounsellor.telephone, legalCounsellor.telephone);
    }
    if(legalCounsellor.organisation) {
      await within(this.fields(elementIndex).legalCounsellor.organisationGroup, async () => {
        organisationHelper.searchAndSelectGivenRegisteredOrganisation(I, legalCounsellor);
      });
    }
    await I.runAccessibilityTest();
  },

  async getActiveElementIndex() {
    return await I.grabNumberOfVisibleElements('//button[text()="Remove"]') - 1;
  },
};
