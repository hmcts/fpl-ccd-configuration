const config = require('../config.js');

module.exports = [
  {
    name: 'Document 1',
    notes: 'Evidence will be late',
    document: config.testFile,
    type: 'Expert reports',
  },
  {
    name: 'Document 2',
    notes: 'Case evidence included',
    document: config.testFile,
    type: 'Other reports',
  },
  {
    name: 'Document 3',
    notes: 'Test notes',
    document: config.testFile,
    type: 'Expert reports',
  },
  {
    name: 'Document 4',
    notes: 'Supports the C2 application',
    document: config.testFile,
    type: 'Other reports',
  },
];
