const I = actor();

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
    I.waitForElement(locate(this.fields.addressList).find('option').withText(address.lookupOption));
    I.wait(1);
    I.selectOption(this.fields.addressList, address.lookupOption);
    I.wait(1);
  },

  enterAddressManually(address) {
    I.click(this.cantEnterPostcodeLink);
    I.fillField(this.fields.buildingAndStreet.lineOne, address.buildingAndStreet.lineOne);
    I.fillField(this.fields.buildingAndStreet.lineTwo, address.buildingAndStreet.lineTwo);
    I.fillField(this.fields.buildingAndStreet.lineThree, address.buildingAndStreet.lineThree);
    I.fillField(this.fields.town, address.town);
    I.fillField(this.fields.postcode, address.postcode);
    I.fillField(this.fields.country, address.country);
  },
};
