const I = actor();

module.exports = {

  file: 'mockFile.txt',
  tabs: {
    ordersHearing: 'Orders and hearing',
    casePeople: 'People in the case',
    legalBasis: 'Legal basis',
    documents: 'Documents',
  },
  actionsDropdown: '.ccd-dropdown',
  goButton: 'Go',

  goToNewActions(actionSelected) {
    I.waitForElement(this.actionsDropdown);
    I.selectOption(this.actionsDropdown, actionSelected);
    I.click(this.goButton);
    I.waitForElement('ccd-case-event-trigger');
  },

  selectTab(tab) {
    I.click(tab);
  },
};
