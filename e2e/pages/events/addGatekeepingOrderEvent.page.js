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
    useAllocatedJudge: '#gatekeepingOrderIssuingJudge_useAllocatedJudge-Yes',
    issuingJudgeTitle: '#gatekeepingOrderIssuingJudge_judgeTitle-HER_HONOUR_JUDGE',
    issuingJudgeName: '#gatekeepingOrderIssuingJudge_judgeLastName',
    issuingJudgeEmail: '#gatekeepingOrderIssuingJudge_judgeEmailAddress',
    legalAdvisorName: '#gatekeepingOrderIssuingJudge_legalAdvisorName',
    statusRadioGroup: {
      sealed: '#gatekeepingOrderSealDecision_orderStatus-SEALED',
      draft: '#gatekeepingOrderSealDecision_orderStatus-DRAFT',
    },
  },

  createGatekeepingOrderThroughService() {
    I.click(this.fields.routingRadioGroup.service);
  },

  enterIssuingJudge(judgeName, legalAdvisorName) {
    I.click(this.fields.issuingJudgeTitle);
    I.fillField(this.fields.issuingJudgeName, judgeName);
    I.fillField(this.fields.issuingJudgeEmail, 'test@mail.com');
    I.fillField(this.fields.legalAdvisorName, legalAdvisorName);
  },

  selectAllocatedJudge(legalAdvisorName) {
    I.click(this.fields.useAllocatedJudge);
    I.fillField(this.fields.legalAdvisorName, legalAdvisorName);
  },

  verifyNextStepsLabel() {
    I.see('Next steps');
    I.see('Your order will be saved as a draft in \'Draft orders\'');
    I.see('You cannot seal and send the order until adding');
    I.see('the allocated judge');
  },

  async enterCustomDirections(direction) {
    const elementIndex = await I.getActiveElementIndex();
    I.fillField(this.fields.customDirection.fields(elementIndex).title, direction.title);
    I.fillField(this.fields.customDirection.fields(elementIndex).description, direction.description);
    I.selectOption(this.fields.customDirection.fields(elementIndex).assignee, direction.assignee);
    await I.fillDate(direction.dueDate, this.fields.customDirection.fields(elementIndex).date);
  },

  markAsDraft() {
    I.click(this.fields.statusRadioGroup.draft);
  },

  async markAsFinal(issueDate) {
    I.click(this.fields.statusRadioGroup.sealed);
    await I.fillDate(issueDate);
  },
};
