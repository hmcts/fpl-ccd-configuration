const config = require('../../config');

module.exports =
  {
    type: 'Upload',
    uploadedOrderType: 'Other',
    fullType: 'Order for something',
    orderName: 'Order for something',
    orderDescription: 'Some order that is not in the dropdown',
    document: 'mockFile.pdf',
    orderFile: config.testPdfFile,
    dateOfIssue: {
      day: '01',
      month: '01',
      year: '2020',
    },
    children: 'All',
    orderChecks: {
      children: 'Timothy Jones\nJohn Black\nWilliam Black\nSarah Black',
      order: 'mockFile.pdf',
      familyManCaseNumber: 'mockCaseID',
    },
  };
