/*global locate*/

const I = actor();

module.exports = {

  fields: {
    postcodeLookup: '#postcodeInput',
    addressList: '#addressList',
    buildingAndStreet: {
      lineOne: '#applicant_address_AddressLine1',
      lineTwo: '#applicant_address_AddressLine2',
      lineThree: '#applicant_address_AddressLine3',
    },
    town: '#applicant_address_PostTown',
    county: '#applicant_address_County',
    postcode: '#applicant_address_PostCode',
    country: '#applicant_address_Country',
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
