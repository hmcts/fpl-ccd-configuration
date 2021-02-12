const {I} = inject();

module.exports = {
  fields: {
    currentStateLabel: '#currentStateLabel',
    confirmChangeState: {
      id: '#confirmChangeState',
      options: {
        yes: '#confirmChangeState-Yes',
        no: '#confirmChangeState-No',
      },
    },
    closedCaseOptions: {
      id: '#closedStateRadioList',
      options: {
        caseManagement: '#closedStateRadioList-PREPARE_FOR_HEARING',
        finalHearing: '#closedStateRadioList-FINAL_HEARING',
      },
    },
    endButton: 'Save and return',
  },

  changeState() {
    I.click(this.fields.confirmChangeState.options.yes);
    I.runAccessibilityTest();
  },

  selectCaseManagement() {
    I.click(this.fields.closedCaseOptions.options.caseManagement);
  },

  selectFinalHearing() {
    I.click(this.fields.closedCaseOptions.options.finalHearing);
  },

  seeAsCurrentState(currentState) {
    I.seeElement(locate(this.fields.currentStateLabel).withText(`Current state: ${currentState}`));
  },
};
