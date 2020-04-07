const { I } = inject();

module.exports = {

  file: 'mockFile.txt',
  tabs: {
    orders: 'Orders',
    draftOrders: 'Draft orders',
    hearings: 'Hearings',
    casePeople: 'People in the case',
    legalBasis: 'Legal basis',
    documents: 'Documents',
    documentsSentToParties: 'Documents sent to parties',
    confidential: 'Confidential',
    placement: 'Placement',
    paymentHistory: 'Payment History',
    notes: 'Notes',
  },
  actionsDropdown: '.ccd-dropdown',
  goButton: 'Go',

  async goToNewActions(actionSelected) {
    I.waitForElement(this.actionsDropdown);
    I.selectOption(this.actionsDropdown, actionSelected);
    await I.retryUntilExists(() => I.click(this.goButton), 'ccd-case-event-trigger');
  },

  checkActionsAreAvailable(actions) {
    I.waitForElement(this.actionsDropdown);
    within(this.actionsDropdown, () => {
      for (let action of actions) {
        I.seeElementInDOM(`//option[text()="${action}"]`);
      }
    });
  },

  checkActionsAreNotAvailable(actions) {
    I.waitForElement(this.actionsDropdown);
    within(this.actionsDropdown, () => {
      for (let action of actions) {
        I.dontSeeElementInDOM(`//option[text()="${action}"]`);
      }
    });
  },

  selectTab(tab) {
    I.click(tab, '.tabs .tabs-list');
  },
};
