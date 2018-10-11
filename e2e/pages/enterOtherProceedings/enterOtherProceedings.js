const I = actor();

module.exports = {
  fields: {
    onGoingProceeding: {
      yes: '#proceeding_onGoingProceeding-Yes',
      no: '#proceeding_onGoingProceeding-No',
    },
    proceedingStatus: '#proceeding_proceedingStatus',
    caseNumber: '#proceeding_caseNumber',
    started: '#proceeding_started',
    ended: '#proceeding_ended',
    ordersMade: '#proceeding_ordersMade',
    judge: '#proceeding_judge',
    children: '#proceeding_children',
    guardian: '#proceeding_guardian',
    sameGuardianNeeded: {
      yes: '#proceeding_sameGuardianNeeded-Yes',
      no: '#proceeding_sameGuardianNeeded-No',
    },
  },

  selectYesForProceeding() {
    I.click(this.fields.onGoingProceeding.yes);
  },

  selectNoForProceeding() {
    I.click(this.fields.onGoingProceeding.no);
  },

  enterProceedingInformation(otherProceedingData) {
    I.selectOption(this.fields.proceedingStatus, otherProceedingData.proceedingStatus);
    I.fillField(this.fields.caseNumber, otherProceedingData.caseNumber);
    I.fillField(this.fields.started, otherProceedingData.started);
    I.fillField(this.fields.ended, otherProceedingData.ended);
    I.fillField(this.fields.ordersMade, otherProceedingData.ordersMade);
    I.fillField(this.fields.judge, otherProceedingData.judge);
    I.fillField(this.fields.children, otherProceedingData.children);
    I.fillField(this.fields.guardian, otherProceedingData.guardian);
    I.click(this.fields.sameGuardianNeeded.yes);
  },
};
