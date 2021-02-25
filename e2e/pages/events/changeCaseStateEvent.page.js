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

  async changeState() {
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
