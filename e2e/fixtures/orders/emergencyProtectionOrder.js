module.exports =
  {
    type: 'Emergency protection order',
    fullType: 'Emergency protection order',
    document: 'emergency_protection_order.pdf',
    dateOfIssue: {
      day: '01',
      month: '01',
      year: '2020',
    },
    judgeAndLegalAdvisor: {
      judgeTitle: 'Her Honour Judge',
      judgeLastName: 'Judy',
      judgeEmailAddress: 'test@test.com',
      legalAdvisorName: 'Fred Frederickson',
    },
    childrenDescription: 'description',
    epoType: 'Prevent removal from an address',
    removalAddress: {
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
    directionText: 'Example direction',
    includePhrase: 'Yes',
    children: [2, 3],
  };
