const {I} = inject();

module.exports = {
  fields: {
    currentStateLabel: '#currentStateLabel',
    confirmChangeState: {
      id: '#confirmChangeState',
      options: {
        yes: '#confirmChangeState_Yes',
        no: '#confirmChangeState_No',
      },
    },
    closedCaseOptions: {
      id: '#closedStateRadioList',
      options: {
        caseManagement: '#closedStateRadioList_PREPARE_FOR_HEARING',
        finalHearing: '#closedStateRadioList_FINAL_HEARING',
      },
    },
    endButton: 'Save and return',
  },

  changeState() {
    I.click(this.fields.confirmChangeState.options.yes);
  },

  selectCaseManagement() {
    I.click(this.fields.closedCaseOptions.options.caseManagement);
  },

  selectFinalHearing() {
    I.click(this.fields.closedCaseOptions.options.finalHearing);
  },

  async seeAsCurrentState(currentState) {
    await I.runAccessibilityTest();
    I.seeElement(locate(this.fields.currentStateLabel).withText(`Current state: ${currentState}`));
  },
};
