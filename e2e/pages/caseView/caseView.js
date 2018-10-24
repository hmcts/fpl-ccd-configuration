const I = actor();

module.exports = {

  file: 'mockFile.txt',
  tabs: {
    ordersHearing: 'Orders and hearing',
    casePeople: 'People in the case',
    legalOpinion: 'Legal Opinion',
    evidence: 'Evidence',
  },
  actionsDropdown: '.ccd-dropdown',
  goButton: 'Go',

  goToNewActions(actionSelected) {
    I.waitForElement(this.actionsDropdown, 10);
    I.selectOption(this.actionsDropdown, actionSelected);
    I.click(this.goButton);
    I.waitForElement('ccd-case-edit-page', 10);
  },

  selectTab(tab) {
    I.click(tab);
  },
};
