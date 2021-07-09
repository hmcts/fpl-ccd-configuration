const {I} = inject();

module.exports = {
  fields: {
    statusRadioGroup: {
      groupName: '#standardDirectionOrder_orderStatus',
      sealed: 'Yes, seal it and send to the local authority',
      draft: 'No, just save it on the system',
    },
    file: {
      preparedSDO: '#preparedSDO',
      replacementSDO: '#replacementSDO',
    },
    allocationDecision: {
      judgeLevelConfirmation: {
        yes: '#urgentHearingAllocation_judgeLevelRadio_Yes',
        no: '#urgentHearingAllocation_judgeLevelRadio_No',
      },
      allocationLevel: {
        // ids have spaces in so don't work
        circuit: 'Circuit Judge',
        section9Circuit: 'Circuit Judge (Section 9)',
        district: 'District Judge',
        magistrate: 'Magistrate',
        highCourt: 'High Court Judge',
      },
      reason: '#urgentHearingAllocation_proposalReason',
    },
    urgentHearingOrder: '#urgentHearingOrderDocument',
  },

  async uploadPreparedSDO(file) {
    await I.runAccessibilityTest();
    I.attachFile(this.fields.file.preparedSDO, file);
    await I.goToNextPage();
  },

  async uploadReplacementSDO(file) {
    await I.runAccessibilityTest();
    I.attachFile(this.fields.file.replacementSDO, file);
    await I.goToNextPage();
  },

  markAsDraft() {
    I.click(this.fields.statusRadioGroup.draft);
  },

  async markAsFinal() {
    await I.runAccessibilityTest();
    I.click(this.fields.statusRadioGroup.sealed);
  },

  async makeAllocationDecision(agreement, level, reason) {
    I.click(agreement);
    if (agreement === this.fields.allocationDecision.judgeLevelConfirmation.no) {
      I.click(level);
      I.fillField(this.fields.allocationDecision.reason, reason);
    }
    await I.runAccessibilityTest();
  },

  async uploadUrgentHearingOrder(order) {
    I.attachFile(this.fields.urgentHearingOrder, order);
    await I.runAccessibilityTest();
  },
};
