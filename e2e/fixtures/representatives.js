const config = require('../config.js');
module.exports = {
  servedByDigitalService: {
    fullName: 'George Wall',
    positionInACase: 'Family Solicitor',
    telephone: '00000 000000',
    email: config.hillingdonLocalAuthorityUserOne.email,
    servingPreferences: 'Through the digital service',
    role: 'Representing respondent 1',
  },
  servedByPost:
    {
      fullName: 'Emma White',
      positionInACase: 'Family Solicitor',
      email: 'emma.white@test.com',
      address: {
        lookupOption: 'Flat 2, Caversham House 15-17, Church Road, Reading',
        buildingAndStreet: {
          lineOne: 'Flat 2',
          lineTwo: 'Caversham House 15-17',
          lineThree: 'Church Road',
        },
        town: 'Reading',
        postcode: 'RG4 7AA',
        country: 'United Kingdom',
      },
      telephone: '00000 000000',
      servingPreferences: 'By post',
      role: 'Representing respondent 1',
    },
};
