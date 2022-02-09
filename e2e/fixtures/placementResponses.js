const config = require('../config.js');

module.exports = [
  {
    description: 'LA response',
    document: config.testFile,
    type: 'Local authority',
  },
  {
    description: 'Cafcass response',
    document: config.testFile,
    type: 'Cafcass',
  },
  {
    description: 'Respondent 1 response',
    document: config.testFile,
    type: 'Respondent',
  },
];
