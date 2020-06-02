module.exports = [
  {
    type: 'Blank order (C21)',
    fullType: 'Blank order (C21)',
    title: 'Example Order Title',
    details: 'Example order details here - Lorem ipsum dolor sit amet...',
    document: 'blank_order_c21.pdf',
    //no date of issue to test default pre-population with today's date
    //future work: extract judgeAndLegalAdvisor to separate fixture
    judgeAndLegalAdvisor: {
      judgeTitle: 'Her Honour Judge',
      judgeLastName: 'Sotomayer',
      judgeEmailAddress: 'test@test.com',
      legalAdvisorName: 'Peter Parker',
    },
    children: 'All',
  },
  {
    type: 'Care order',
    subtype: 'Interim',
    fullType: 'Interim care order',
    document: 'interim_care_order.pdf',
    dateOfIssue: {
      day: '01',
      month: '01',
      year: '2020',
    },
    interimEndDate: {
      isNamedDate: false,
    },
    judgeAndLegalAdvisor: {
      judgeTitle: 'Her Honour Judge',
      judgeLastName: 'Judy',
      judgeEmailAddress: 'test@test.com',
      legalAdvisorName: 'Fred Frederickson',
    },
    children: 'All',
  },
  {
    type: 'Care order',
    subtype: 'Final',
    fullType: 'Final care order',
    document: 'final_care_order.pdf',
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
    children: 'All',
    closeCase: false,
  },
  {
    type: 'Supervision order',
    subtype: 'Interim',
    fullType: 'Interim supervision order',
    document: 'interim_supervision_order.pdf',
    dateOfIssue: {
      day: '01',
      month: '01',
      year: '2020',
    },
    interimEndDate: {
      isNamedDate: true,
      endDate: {
        day: '12',
        month: '03',
        year: '2120',
      },
    },
    judgeAndLegalAdvisor: {
      judgeTitle: 'His Honour Judge',
      judgeLastName: 'Dredd',
      judgeEmailAddress: 'test@test.com',
      legalAdvisorName: 'Frank N. Stein',
    },
    directionText: 'Example direction',
    children: [0],
  },
  {
    type: 'Supervision order',
    subtype: 'Final',
    fullType: 'Final supervision order',
    document: 'final_supervision_order.pdf',
    dateOfIssue: {
      day: '01',
      month: '01',
      year: '2020',
    },
    months: '5',
    judgeAndLegalAdvisor: {
      judgeTitle: 'His Honour Judge',
      judgeLastName: 'Dredd',
      judgeEmailAddress: 'test@test.com',
      legalAdvisorName: 'Frank N. Stein',
    },
    directionText: 'Example direction',
    children: [1],
    closeCase: false,
  },
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
    children: [0,1],
    closeCase: false,
  },
];
