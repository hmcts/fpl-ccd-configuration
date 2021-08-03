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
        I.waitForEnabled('#search-org-text');
        I.fillField('#search-org-text', legalCounsellor.organisation);
        I.click(locate('a').withText('Select').inside(locate('#organisation-table').withDescendant(locate('h3').withText(legalCounsellor.organisation))));
      });
    }
    await I.runAccessibilityTest();
  },

  async getActiveElementIndex() {
    return await I.grabNumberOfVisibleElements('//button[text()="Remove"]') - 1;
  },
};
