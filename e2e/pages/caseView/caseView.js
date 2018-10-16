const I = actor();

module.exports = {

  file: 'mockFile.txt',
  tabs: {
    ordersHearing: 'Orders sought and hearing needed',
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
    I.click(this.tabs[tab]);
  },
};
