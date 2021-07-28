const { I } = inject();

module.exports = {

  fields: {
    postcodeLookup: 'input[id$="postcodeInput"]',
    addressList: 'select[id$="addressList"]',
    buildingAndStreet: {
      lineOne: 'input[id$="AddressLine1"]',
      lineTwo: 'input[id$="AddressLine2"]',
      lineThree: 'input[id$="AddressLine3"]',
    },
    town: 'input[id$="PostTown"]',
    county: 'input[id$="County"]',
    postcode: 'input[id$="PostCode"]',
    country: 'input[id$="Country"]',
  },
  findAddressButton: 'Find address',
  cantEnterPostcodeLink: locate('a').withText('I can\'t enter a UK postcode'),

  lookupPostcode(address) {
    I.fillField(this.fields.postcodeLookup, address.postcode);
    I.click(this.findAddressButton);
    I.waitForText('addresses found');
    I.waitForElement(locate(this.fields.addressList).find('option').withText(address.lookupOption));
    I.selectOption(this.fields.addressList, address.lookupOption);
    I.waitForValue(this.fields.buildingAndStreet.lineOne, address.buildingAndStreet.lineOne);
    I.waitForValue(this.fields.buildingAndStreet.lineTwo, address.buildingAndStreet.lineTwo);
    I.waitForValue(this.fields.buildingAndStreet.lineThree, address.buildingAndStreet.lineThree);
    I.waitForValue(this.fields.town, address.town);
    I.waitForValue(this.fields.postcode, address.postcode);
    I.waitForValue(this.fields.country, address.country);
  },

  async enterAddressManually(address) {
    I.waitForElement(this.fields.postcodeLookup);
    if (await I.grabNumberOfVisibleElements(this.cantEnterPostcodeLink)) {
      I.click(this.cantEnterPostcodeLink);
    }
    if (address.buildingAndStreet.lineOne) {
      I.fillField(this.fields.buildingAndStreet.lineOne, address.buildingAndStreet.lineOne);
    }
    if (address.buildingAndStreet.lineTwo) {
      I.fillField(this.fields.buildingAndStreet.lineTwo, address.buildingAndStreet.lineTwo);
    }
    if (address.buildingAndStreet.lineThree) {
      I.fillField(this.fields.buildingAndStreet.lineThree, address.buildingAndStreet.lineThree);
    }
    if (address.town) {
      I.fillField(this.fields.town, address.town);
    }
    if (address.postcode) {
      I.fillField(this.fields.postcode, address.postcode);
    }
    if (address.country) {
      I.fillField(this.fields.country, address.country);
    }
  },

  async enterAddressIfNotPresent(address) {
    if(await I.canSee(this.cantEnterPostcodeLink)){
      await this.enterAddressManually(address);
    }
  },

};
