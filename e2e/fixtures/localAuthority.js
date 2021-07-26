module.exports = {
  name: 'Swansea Local Authority',
  email: 'applicant@email.com',
  legalTeamManager: 'Alex Brown',
  pbaNumber: 'PBA0082848',
  customerReference: 'Example reference',
  clientCode: '8888',
  phone: '07000000000',
  address: {
    lookupOption: 'Flat 2, Caversham House 15-17, Church Road, Reading',
    buildingAndStreet: {
      lineOne: 'Flat 1, Swansea Apartments',
      lineTwo: 'Swansea Central Square 11',
      lineThree: '40 Fleet street',
    },
    townCity: 'Swansea',
    county: 'Swansea',
    country: 'United Kingdom',
    postcode: 'CR0 2GE',
  },
  colleagues:[
    {
      role: 'Solicitor',
      fullName: 'Emma White',
      email: 'emma@test.com',
      dx: '123',
      reference: 'AB1234',
      phone: '07654321',
      notificationRecipient: 'Yes',
    },
    {
      role: 'Other colleague',
      fullName: 'Gregory Wish',
      email: 'gregory@test.com',
      title: 'Legal adviser',
      notificationRecipient: 'No',
    },
  ],
};
