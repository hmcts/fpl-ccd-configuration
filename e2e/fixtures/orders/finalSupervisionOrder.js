module.exports =
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
  };
