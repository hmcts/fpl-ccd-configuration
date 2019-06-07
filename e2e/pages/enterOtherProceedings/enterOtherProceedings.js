const I = actor();

module.exports = {
  state: {
    contextPrefix: '',
  },

  fields: function() {
    const prefix = this.state.contextPrefix;

    return {
      onGoingProceeding: {
        yes: '#proceeding_onGoingProceeding-Yes',
        no: '#proceeding_onGoingProceeding-No',
        dontKnow: '#proceeding_onGoingProceeding-DontKnow',
      },
      proceedingStatus: {
        previous: `#proceeding_${prefix}proceedingStatus-Previous`,
        ongoing: `#proceeding_${prefix}proceedingStatus-Ongoing`,
      },
      caseNumber: `#proceeding_${prefix}caseNumber`,
      started: `#proceeding_${prefix}started`,
      ended: `#proceeding_${prefix}ended`,
      ordersMade: `#proceeding_${prefix}ordersMade`,
      judge: `#proceeding_${prefix}judge`,
      children: `#proceeding_${prefix}children`,
      guardian: `#proceeding_${prefix}guardian`,
      sameGuardianNeeded: {
        yes: `#proceeding_${prefix}sameGuardianNeeded-Yes`,
        no: `#proceeding_${prefix}sameGuardianNeeded-No`,
      },
    };
  },

  addProceedingButton: 'Add new',

  addNewProceeding() {
    if (this.state.contextPrefix === 'additionalProceedings_0_') {
      throw new Error('Adding more proceedings is not supported in the test');
    }

    I.click(this.addProceedingButton);
    this.state.contextPrefix = 'additionalProceedings_0_';
  },

  selectYesForProceeding() {
    I.click(this.fields().onGoingProceeding.yes);
  },

  selectNoForProceeding() {
    I.click(this.fields().onGoingProceeding.no);
  },

  selectOngoingProceedingStatus(status = 'ongoing') {
    if(status == 'ongoing') {
      I.click(this.fields().proceedingStatus.ongoing);
    } else if (status == 'previous') {
      I.click(this.fields().proceedingStatus.previous);
    }
  },

  enterProceedingInformation(otherProceedingData) {
    this.selectOngoingProceedingStatus(otherProceedingData.proceedingStatus);
    I.fillField(this.fields().caseNumber, otherProceedingData.caseNumber);
    I.fillField(this.fields().started, otherProceedingData.started);
    I.fillField(this.fields().ended, otherProceedingData.ended);
    I.fillField(this.fields().ordersMade, otherProceedingData.ordersMade);
    I.fillField(this.fields().judge, otherProceedingData.judge);
    I.fillField(this.fields().children, otherProceedingData.children);
    I.fillField(this.fields().guardian, otherProceedingData.guardian);
    I.click(this.fields().sameGuardianNeeded.yes);
  },
};
