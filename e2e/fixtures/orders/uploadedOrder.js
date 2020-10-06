const config = require('../../config');

module.exports =
  {
    type: 'Upload',
    uploadedOrderType: 'Appointment of a solicitor - C48A',
    fullType: 'Appointment of a solicitor (C48A)',
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
