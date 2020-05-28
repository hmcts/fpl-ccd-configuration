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
    expertReports: 'Expert Reports',
    overview: 'Overview',
  },
  actionsDropdown: '.ccd-dropdown',
  goButton: 'Go',
  caseTitle: '.case-title .markdown',

  async goToNewActions(actionSelected) {
    I.waitForElement(this.actionsDropdown);
    await I.retryUntilExists(() => {
      I.selectOption(this.actionsDropdown, actionSelected);
      I.click(this.goButton);
    }, 'ccd-case-event-trigger');
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

  seeInCaseTitle(titleValue) {
    I.seeElement(locate(this.caseTitle).withText(titleValue));
  },
};
