const basic = require('./base.js');
const tabs = require('./tabs.js');

'use strict';

module.exports = function () {
  return actor(Object.assign(basic, tabs));
};
