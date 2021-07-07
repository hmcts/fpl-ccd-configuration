const { I } = inject();

module.exports = {
  fields: function(index) {
    return {
      onGoingProceeding: {
        yes: '#proceeding_onGoingProceeding-Yes',
        no: '#proceeding_onGoingProceeding-No',
        dontKnow: '#proceeding_onGoingProceeding-DontKnow',
      },
      proceedingStatus: {
        previous: `#proceeding_${index}proceedingStatus-Previous`,
        ongoing: `#proceeding_${index}proceedingStatus-Ongoing`,
      },
      caseNumber: `#proceeding_${index}caseNumber`,
      started: `#proceeding_${index}started`,
      ended: `#proceeding_${index}ended`,
      ordersMade: `#proceeding_${index}ordersMade`,
      judge: `#proceeding_${index}judge`,
      children: `#proceeding_${index}children`,
      guardian: `#proceeding_${index}guardian`,
      sameGuardianNeeded: {
        yes: `#proceeding_${index}sameGuardianNeeded_Yes`,
        no: `#proceeding_${index}sameGuardianNeeded_No`,
      },
    };
  },

  selectYesForProceeding() {
    I.click(this.fields(undefined).onGoingProceeding.yes);
  },

  selectNoForProceeding() {
    I.click(this.fields(undefined).onGoingProceeding.no);
  },

  selectOngoingProceedingStatus(status = 'ongoing', elementIndex) {
    if(status === 'ongoing') {
      I.click(this.fields(elementIndex).proceedingStatus.ongoing);
    } else if (status === 'previous') {
      I.click(this.fields(elementIndex).proceedingStatus.previous);
    }
  },

  async enterProceedingInformation(otherProceedingData) {
    const elementIndex = await this.getActiveElementIndex();
    await I.runAccessibilityTest();
    this.selectOngoingProceedingStatus(otherProceedingData.proceedingStatus, elementIndex);
    I.fillField(this.fields(elementIndex).caseNumber, otherProceedingData.caseNumber);
    I.fillField(this.fields(elementIndex).started, otherProceedingData.started);
    I.fillField(this.fields(elementIndex).ended, otherProceedingData.ended);
    I.fillField(this.fields(elementIndex).ordersMade, otherProceedingData.ordersMade);
    I.fillField(this.fields(elementIndex).judge, otherProceedingData.judge);
    I.fillField(this.fields(elementIndex).children, otherProceedingData.children);
    I.fillField(this.fields(elementIndex).guardian, otherProceedingData.guardian);
    I.click(this.fields(elementIndex).sameGuardianNeeded.yes);
  },

  async getActiveElementIndex() {
    const count = await I.getActiveElementIndex();
    if (count === -1) {
      return '';
    } else {
      return `additionalProceedings_${count}_`;
    }
  },
};
