module.exports =
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
  };
