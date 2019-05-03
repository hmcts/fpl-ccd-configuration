/*global locate*/

const I = actor();

module.exports = {

  fields: {
    postcodeLookup: '#postcodeInput',
    addressList: '#addressList',
    buildingAndStreet: {
      lineOne: '#applicant_address__AddressLine1',
      lineTwo: '#applicant_address__AddressLine2',
      lineThree: '#applicant_address__AddressLine3',
    },
    town: '#applicant_address__PostTown',
    county: '#applicant_address__County',
    postcode: '#applicant_address__PostCode',
    country: '#applicant_address__Country',
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
