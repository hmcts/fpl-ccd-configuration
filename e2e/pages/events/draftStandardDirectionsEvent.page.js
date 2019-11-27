const { I } = inject();
const judgeAndLegalAdvisor = require('../../fragments/judgeAndLegalAdvisor');
const directions = require('../../fragments/directions');

module.exports = {
  staticFields: {
    statusRadioGroup: {
      groupName: '#standardDirectionOrder_orderStatus',
      sealed: 'Yes, it can be sealed and sent to parties',
      draft: 'No, I need to make changes',
    },
  },

  async enterJudgeAndLegalAdvisor(judgeLastName, legalAdvisorName) {
    judgeAndLegalAdvisor.selectJudgeTitle();
    judgeAndLegalAdvisor.enterJudgeLastName(judgeLastName);
    judgeAndLegalAdvisor.enterLegalAdvisorName(legalAdvisorName);
    await I.retryUntilExists(() => I.click('Continue'), '#allParties');
  },

  async enterDatesForDirections(direction) {
    await directions.enterDate('allParties', direction);
    await I.retryUntilExists(() => I.click('Continue'), '#localAuthorityDirections');
    await directions.enterDate('localAuthorityDirections', direction.dueDate);
    await I.retryUntilExists(() => I.click('Continue'), '#respondentDirections');
    await directions.enterDate('respondentDirections', direction.dueDate);
    await I.retryUntilExists(() => I.click('Continue'), '#cafcassDirections');
    await directions.enterDate('cafcassDirections', direction.dueDate);
    await I.retryUntilExists(() => I.click('Continue'), '#otherPartiesDirections');
    await directions.enterDate('otherPartiesDirections', direction.dueDate);
    await I.retryUntilExists(() => I.click('Continue'), '#courtDirections');
    await directions.enterDate('courtDirections', direction.dueDate);
    await I.retryUntilExists(() => I.click('Continue'), '#standardDirectionOrder_orderStatus');
  },

  markAsDraft() {
    within(this.staticFields.statusRadioGroup.groupName, () => {
      I.click(locate('label').withText(this.staticFields.statusRadioGroup.draft));
    });
  },

  markAsFinal() {
    within(this.staticFields.statusRadioGroup.groupName, () => {
      I.click(locate('label').withText(this.staticFields.statusRadioGroup.sealed));
    });
  },
};
