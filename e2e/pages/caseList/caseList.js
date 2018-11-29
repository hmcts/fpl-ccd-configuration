/*global locate*/
const config = require('../../config');

const I = actor();

module.exports = {
  openExistingCase(caseId) {
    const href = `/case/${config.definition.jurisdiction}/${config.definition.caseType}/${caseId.replace(/\D/g, '')}`;
    const caseLink = locate('a').withAttr({href: href});
    I.click(caseLink);
  },

  changeStateFilter(desiredState) {
    I.selectOption('#wb-case-state', desiredState);
    I.click('Apply');
  },
};
