const {I} = inject();
const judgeAndLegalAdvisor = require('../../fragments/judgeAndLegalAdvisor');

module.exports = {
  fields: function (party, index) {
    return {
      direction: {
        title: `#${party}_${index}_directionType`,
        description: `#${party}_${index}_directionText`,
        dueDate: {
          day: `#${party}_${index}_dateToBeCompletedBy-day`,
          month: `#${party}_${index}_dateToBeCompletedBy-month`,
          year: `#${party}_${index}_dateToBeCompletedBy-year`,
          hour: `#${party}_${index}_dateToBeCompletedBy-hour`,
          minute: `#${party}_${index}_dateToBeCompletedBy-minute`,
          second: `#${party}_${index}_dateToBeCompletedBy-second`,
        },
      },
    };
  },

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

  async enterDate(party, direction, index = 0) {
    I.fillField(this.fields(party, index).direction.dueDate.day, direction.dueDate.day);
    I.fillField(this.fields(party, index).direction.dueDate.month, direction.dueDate.month);
    I.fillField(this.fields(party, index).direction.dueDate.year, direction.dueDate.year);
    I.fillField(this.fields(party, index).direction.dueDate.hour, direction.dueDate.hour);
    I.fillField(this.fields(party, index).direction.dueDate.minute, direction.dueDate.minute);
    I.fillField(this.fields(party, index).direction.dueDate.second, direction.dueDate.second);
  },

  async enterDatesForDirections(direction) {
    await this.enterDate('allParties', direction);
    await I.retryUntilExists(() => I.click('Continue'), '#localAuthorityDirections');
    await this.enterDate('localAuthorityDirections', direction);
    await I.retryUntilExists(() => I.click('Continue'), '#respondentDirections');
    await this.enterDate('respondentDirections', direction);
    await I.retryUntilExists(() => I.click('Continue'), '#cafcassDirections');
    await this.enterDate('cafcassDirections', direction);
    await I.retryUntilExists(() => I.click('Continue'), '#otherPartiesDirections');
    await this.enterDate('otherPartiesDirections', direction);
    await I.retryUntilExists(() => I.click('Continue'), '#courtDirections');
    await this.enterDate('courtDirections', direction);
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
