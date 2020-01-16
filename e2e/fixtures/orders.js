module.exports = [
  {
    type: 'Blank order (C21)',
    fullType: 'Blank order (C21)',
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
    subtype: 'Interim',
    fullType: 'Interim care order',
    document: 'interim_care_order.pdf',
    judgeAndLegalAdvisor: {
      judgeTitle: 'Her Honour Judge',
      judgeLastName: 'Judy',
      legalAdvisorName: 'Fred Frederickson',
    },
  },
  {
    type: 'Care order',
    subtype: 'Final',
    fullType: 'Final care order',
    document: 'final_care_order.pdf',
    judgeAndLegalAdvisor: {
      judgeTitle: 'Her Honour Judge',
      judgeLastName: 'Judy',
      legalAdvisorName: 'Fred Frederickson',
    },
  },
  {
    type: 'Supervision order',
    fullType: 'Supervision order',
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
