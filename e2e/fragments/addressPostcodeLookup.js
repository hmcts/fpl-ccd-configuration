/*global locate*/

const I = actor();

module.exports = {

  fields: {
    postcodeLookup: '#postcodeInput',
    addressList: '#addressList',
    buildingAndStreet: {
      addressLineOne: '#AddressLine1',
      addressLineTwo: '#AddressLine2',
      addressLineThree: '#AddressLine3',
    },
    town: '#PostTown',
    county: '#County',
    postcodeZipcode: '#PostCode',
    country: '#Country',
  },
  findAddressButton: 'Find address',
  cantEnterPostcodeLink: locate('span').withText('I can\'t enter a UK postcode'),
  
  lookupPostcode(address) {
    I.fillField(this.fields.postcodeLookup, address.postcode);
    I.click(this.findAddressButton);
    I.selectOption(this.fields.addressList, address.lookupOption);
  },

  enterAddressManually(address) {
    I.click(this.cantEnterPostcodeLink);
    I.fillField(this.fields.buildingAndStreet.addressLineOne, address.buildingAndStreet.addressLineOne);
    I.fillField(this.fields.buildingAndStreet.addressLineTwo, address.buildingAndStreet.addressLineTwo);
    I.fillField(this.fields.buildingAndStreet.addressLineThree, address.buildingAndStreet.addressLineThree);
    I.fillField(this.fields.town, address.town);
    I.fillField(this.fields.postcodeZipcode, address.postcode);
    I.fillField(this.fields.country, address.country);
  },
};
