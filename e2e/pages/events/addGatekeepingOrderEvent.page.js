const {I} = inject();

module.exports = {
  fields: {
    routingRadioGroup: {
      groupName: '#gatekeepingOrderRouter',
      service: '#gatekeepingOrderRouter-SERVICE',
    },
    customDirection: {
      fields(index) {
        return {
          title: `#sdoDirectionCustom_${index}_title`,
          description: `#sdoDirectionCustom_${index}_description`,
          assignee: `#sdoDirectionCustom_${index}_assignee`,
          date: `#sdoDirectionCustom_${index}_dateToBeCompletedBy`,
        };
      },
    },
    useAllocatedJudge: `#gatekeepingOrderIssuingJudge_useAllocatedJudge-Yes`,
    legalAdvisorName: `#gatekeepingOrderIssuingJudge_legalAdvisorName`,
    statusRadioGroup: {
      groupName: '#saveOrSendGatekeepingOrder_orderStatus',
      sealed: 'Yes, seal it and send to the local authority',
      draft: 'No, just save it on the system',
    },
  },

  async createGatekeepingOrderThroughService() {
    I.click(this.fields.routingRadioGroup.service);
    await I.runAccessibilityTest();
    await I.goToNextPage();
  },

  async enterIssuingJudge(legalAdvisorName) {
    I.click(this.fields.useAllocatedJudge);
    I.fillField(this.fields.legalAdvisorName, legalAdvisorName);
  },

  async enterCustomDirections(direction) {
    const elementIndex = await I.getActiveElementIndex();
    I.fillField(this.fields.customDirection.fields(elementIndex).title, direction.title);
    I.fillField(this.fields.customDirection.fields(elementIndex).description, direction.description);
    I.selectOption(this.fields.customDirection.fields(elementIndex).assignee, 'All parties');
    I.fillDate(direction.dueDate, this.fields.customDirection.fields(elementIndex).date);
  },

  markAsDraft() {
    I.click(this.fields.statusRadioGroup.draft);
  },

  async markAsFinal(issueDate) {
    await I.runAccessibilityTest();
    I.click(this.fields.statusRadioGroup.sealed);
    await I.runAccessibilityTest();
    await I.fillDate(issueDate);
  },
};
