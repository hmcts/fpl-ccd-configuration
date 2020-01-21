const {I} = inject();
const postcodeLookup = require('../../fragments/addressPostcodeLookup');

module.exports = {
  fields: function (index) {

    return {
      representative: {
        fullName: `#representatives_${index}_fullName`,
        positionInACase: `#representatives_${index}_positionInACase`,
        email: `#representatives_${index}_email`,
        address: `#representatives_${index}_address_address`,
        telephone: `[id="representatives_${index}_telephoneNumber"]`,
        servingPreferences: {
          email: `#representatives_${index}_servingPreferences-EMAIL`,
          post: `#representatives_${index}_servingPreferences-POST`,
          digitalService: `#representatives_${index}_servingPreferences-DIGITAL_SERVICE`,
        },
        role: `#representatives_${index}_role`,
      },
    };
  },

  async enterRepresentative(representative) {
    const elementIndex = await this.getActiveElementIndex();

    I.fillField(this.fields(elementIndex).representative.fullName, representative.fullName);
    I.fillField(this.fields(elementIndex).representative.positionInACase, representative.positionInACase);
    I.fillField(this.fields(elementIndex).representative.email, representative.email);
    I.fillField(this.fields(elementIndex).representative.telephone, representative.telephone);
    within(this.fields(elementIndex).representative.address, () => {
      postcodeLookup.enterAddressManually(representative.address);
    });

    this.setServingPreferences(representative.servingPreferences.toLowerCase());

    I.selectOption(this.fields(elementIndex).representative.role, representative.role);
  },

  async setServingPreferences(servingPreferences) {
    const elementIndex = await this.getActiveElementIndex();

    switch (servingPreferences) {
      case 'email':
        I.checkOption(this.fields(elementIndex).representative.servingPreferences.email);
        break;
      case 'by post':
        I.checkOption(this.fields(elementIndex).representative.servingPreferences.post);
        break;
      case 'through the digital service':
        I.checkOption(this.fields(elementIndex).representative.servingPreferences.digitalService);
        break;
      default:
        throw new Error(`Unsupported representative serving preferences ${servingPreferences}`);
    }
  },

  async getActiveElementIndex() {
    return await I.grabNumberOfVisibleElements('//button[text()="Remove"]') - 1;
  },
};
