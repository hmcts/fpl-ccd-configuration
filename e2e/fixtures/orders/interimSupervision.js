module.exports =
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
  };
