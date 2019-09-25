const { I } = inject();

module.exports = {

  file: 'mockFile.txt',
  tabs: {
    orders: 'Orders',
    hearings: 'Hearings',
    casePeople: 'People in the case',
    legalBasis: 'Legal basis',
    documents: 'Documents',
  },
  actionsDropdown: '.ccd-dropdown',
  goButton: 'Go',

  async goToNewActions(actionSelected) {
    I.waitForElement(this.actionsDropdown);
    I.selectOption(this.actionsDropdown, actionSelected);
    await I.retryUntilExists(() => I.click(this.goButton), 'ccd-case-event-trigger');
  },

  selectTab(tab) {
    I.click(tab, '.tabs .tabs-list');
  },
};
