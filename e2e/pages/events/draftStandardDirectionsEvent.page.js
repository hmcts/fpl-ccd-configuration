const { I } = inject();
const judgeAndLegalAdvisor = require('../../fragments/judgeAndLegalAdvisor');
const draftDirections = require('../../fragments/draftDirections');

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
    await draftDirections.enterDate('allParties', direction);
    await I.retryUntilExists(() => I.click('Continue'), '#localAuthorityDirections');
    await draftDirections.enterDate('localAuthorityDirections', direction);
    await I.retryUntilExists(() => I.click('Continue'), '#respondentDirections');
    await draftDirections.enterDate('respondentDirections', direction);
    await I.retryUntilExists(() => I.click('Continue'), '#cafcassDirections');
    await draftDirections.enterDate('cafcassDirections', direction);
    await I.retryUntilExists(() => I.click('Continue'), '#otherPartiesDirections');
    await draftDirections.enterDate('otherPartiesDirections', direction);
    await I.retryUntilExists(() => I.click('Continue'), '#courtDirections');
    await draftDirections.enterDate('courtDirections', direction);
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
