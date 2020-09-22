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
    endButton: 'Save and return',
  },

  changeState() {
    I.click(this.fields.confirmChangeState.options.yes);
  },

  seeAsCurrentState(currentState) {
    I.seeElement(locate(this.fields.currentStateLabel).withText(`Current state: ${currentState}`));
  },
};
