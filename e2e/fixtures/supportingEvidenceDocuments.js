const config = require('../config.js');

module.exports = [
  {
    name: 'Email to say evidence will be late',
    notes: 'Evidence will be late',
    document: config.testFile,
    type: 'Expert reports',
  },
  {
    name: 'Email with evidence attached',
    notes: 'Case evidence included',
    document: config.testFile,
    type: 'Other reports',
  },
  {
    name: 'Correspondence document',
    notes: 'Test notes',
    document: config.testFile,
    type: 'Expert reports',
  },
  {
    name: 'C2 supporting document',
    notes: 'Supports the C2 application',
    document: config.testFile,
    type: 'Other reports',
  },
];
