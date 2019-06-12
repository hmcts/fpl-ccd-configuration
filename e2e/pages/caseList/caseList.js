/* global locate*/
const I = actor();

module.exports = {
  changeStateFilter(desiredState) {
    I.selectOption('#wb-case-state', desiredState);
    I.click('Apply');
  },

  findCase(caseId) {
    return locate('.//tr').withChild(`.//td/a[text()='${caseId.slice(1)}']`);
  },
};
