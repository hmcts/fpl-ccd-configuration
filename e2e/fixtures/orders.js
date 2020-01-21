module.exports = [
  {
    type: 'Blank order (C21)',
    title: 'Example Order Title',
    details: 'Example order details here - Lorem ipsum dolor sit amet...',
    document: 'blank_order_c21.pdf',
    //future work: extract judgeAndLegalAdvisor to separate fixture
    judgeAndLegalAdvisor: {
      judgeTitle: 'Her Honour Judge',
      judgeLastName: 'Sotomayer',
      legalAdvisorName: 'Peter Parker',
    },
  },
  {
    type: 'Care order',
    document: 'care_order.pdf',
    judgeAndLegalAdvisor: {
      judgeTitle: 'Her Honour Judge',
      judgeLastName: 'Judy',
      legalAdvisorName: 'Fred Frederickson',
    },
  },
  {
    type: 'Emergency protection order',
    document: 'emergency_protection_order.pdf',
    judgeAndLegalAdvisor: {
      judgeTitle: 'Her Honour Judge',
      judgeLastName: 'Judy',
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
  },
  {
    type: 'Supervision order',
    document: 'supervision_order.pdf',
    months: '5',
    judgeAndLegalAdvisor: {
      judgeTitle: 'His Honour Judge',
      judgeLastName: 'Dredd',
      legalAdvisorName: 'Frank N. Stein',
    },
    directionText: 'Example direction',
  },
];
