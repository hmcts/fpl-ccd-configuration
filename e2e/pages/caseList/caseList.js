/* global locate*/
const I = actor();

module.exports = {
  changeStateFilter(desiredState) {
    I.selectOption('#wb-case-state', desiredState);
    I.click('Apply');
  },
};
