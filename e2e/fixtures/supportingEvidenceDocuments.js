const config = require('../config.js');

module.exports = [
  {
    name: 'Email to say evidence will be late',
    notes: 'Evidence will be late',
    date: {
      day: 1,
      month: 1,
      year: 2020,
      hour: 11,
      minute: 0,
      second: 0,
    },
    document: config.testFile,
  },
  {
    name: 'Email with evidence attached',
    notes: 'Case evidence included',
    date: {
      day: 1,
      month: 1,
      year: 2020,
      hour: 11,
      minute: 0,
      second: 0,
    },
    document: config.testFile,
  },
];
