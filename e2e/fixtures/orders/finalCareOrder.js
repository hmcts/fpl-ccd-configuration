module.exports =
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
    closeCase: false,
    children: 'Single',
  };
