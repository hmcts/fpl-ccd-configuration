module.exports =
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
  };
