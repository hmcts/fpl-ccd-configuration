const I = actor();

module.exports = {

    tabs: {
        ordersHearingTab: "Orders sought and hearing needed",
        casePeopleTab: "People in the case",
        legalOpinionTab: "Legal Opinion",
        evidenceTab: "Evidence"
    },

    actionsDropdown: ".ccd-dropdown",
    goButton: "Go",

    goToNewActions(actionSelected) {
        I.wait(2);
        I.selectOption(this.actionsDropdown, actionSelected);
        I.click(this.goButton);
        I.wait(2);
    }
};
