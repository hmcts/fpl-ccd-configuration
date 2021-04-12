const {I} = inject();
const postcodeLookup = require('../../fragments/addressPostcodeLookup');

module.exports = {
  fields: index => ({
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
  }),

  async enterRepresentative(representative) {
    const elementIndex = await I.getActiveElementIndex();

    await I.runAccessibilityTest();

    if (representative.fullName) {
      I.fillField(this.fields(elementIndex).representative.fullName, representative.fullName);
    }
    if (representative.positionInACase) {
      I.fillField(this.fields(elementIndex).representative.positionInACase, representative.positionInACase);
    }
    if (representative.email) {
      I.fillField(this.fields(elementIndex).representative.email, representative.email);
    }
    if (representative.telephone) {
      I.fillField(this.fields(elementIndex).representative.telephone, representative.telephone);
    }
    if (representative.address) {
      await within(this.fields(elementIndex).representative.address, () => {
        postcodeLookup.enterAddressManually(representative.address);
      });
    }
    if (representative.servingPreferences) {
      await this.setServingPreferences(representative.servingPreferences.toLowerCase());
    }
    if (representative.role) {
      I.selectOption(this.fields(elementIndex).representative.role, representative.role);
    }
  },

  async setServingPreferences(servingPreferences) {
    const elementIndex = await I.getActiveElementIndex();

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

  async setRepresentativeEmail(elementIndex, email) {
    I.fillField(this.fields(elementIndex).representative.email, email);
  },

  async setDigitalServingPreferences(elementIndex) {
    I.checkOption(this.fields(elementIndex).representative.servingPreferences.digitalService);
  },

  async setRepresentativeRole(elementIndex, role) {
    I.selectOption(this.fields(elementIndex).representative.role, role);
  },
};
