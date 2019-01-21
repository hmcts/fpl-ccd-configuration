/*global locate*/

const I = actor();

module.exports = {

  fields: {
    postcodeLookup: '#postcodeInput',
    addressList: '#addressList',
    buildingAndStreet: {
      lineOne: '#AddressLine1',
      lineTwo: '#AddressLine2',
      lineThree: '#AddressLine3',
    },
    town: '#PostTown',
    county: '#County',
    postcode: '#PostCode',
    country: '#Country',
  },
  findAddressButton: 'Find address',
  cantEnterPostcodeLink: locate('a').withText('I can\'t enter a UK postcode'),

  lookupPostcode(address) {
    I.fillField(this.fields.postcodeLookup, address.postcode);
    I.click(this.findAddressButton);
    I.waitForElement(locate(this.fields.addressList).find('option').withText(address.lookupOption));
    I.click(this.fields.addressList);
    I.selectOption(this.fields.addressList, address.lookupOption);
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
