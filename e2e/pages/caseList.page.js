const { I } = inject();

module.exports = {
  changeStateFilter(desiredState) {
    I.selectOption('#wb-case-state', desiredState);
    I.click('Apply');
  },
};
