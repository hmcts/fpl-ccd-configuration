const I = actor();

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
        yes: `#proceeding_${index}sameGuardianNeeded-Yes`,
        no: `#proceeding_${index}sameGuardianNeeded-No`,
      },
    };
  },

  addProceedingButton: 'Add new',

  addNewProceeding() {
    I.click(this.addProceedingButton);
  },

  selectYesForProceeding() {
    I.click(this.fields(undefined).onGoingProceeding.yes);
  },

  selectNoForProceeding() {
    I.click(this.fields(undefined).onGoingProceeding.no);
  },

  async selectOngoingProceedingStatus(status = 'ongoing') {
    const elementIndex = await this.getActiveElementIndex();

    if(status === 'ongoing') {
      I.click(this.fields(elementIndex).proceedingStatus.ongoing);
    } else if (status === 'previous') {
      I.click(this.fields(elementIndex).proceedingStatus.previous);
    }
  },

  async enterProceedingInformation(otherProceedingData) {
    const elementIndex = await this.getActiveElementIndex();

    await this.selectOngoingProceedingStatus(otherProceedingData.proceedingStatus);
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
    const count = await I.grabNumberOfVisibleElements('//button[text()="Remove"]');
    if (count === 0) {
      return '';
    } else {
      return `additionalProceedings_${count - 1}_`;
    }
  },
};
