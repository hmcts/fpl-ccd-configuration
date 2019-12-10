module.exports = [
  {
    type: 'Blank order (C21)',
    orderTitle: 'Example Order Title',
    orderDetails: 'Example order details here - Lorem ipsum dolor sit amet...',
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
];
