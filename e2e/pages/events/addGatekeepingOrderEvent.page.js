const {I} = inject();
const moment = require('moment');
const assert = require('assert');

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
          dueDateType: `#sdoDirectionCustom_${index}_dueDateType-DATE`,
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

  async createGatekeepingOrderThroughService() {
    I.click(this.fields.routingRadioGroup.service);
  },

  async enterIssuingJudge(judgeName, legalAdvisorName) {
    I.click(this.fields.issuingJudgeTitle);
    I.fillField(this.fields.issuingJudgeName, judgeName);
    I.fillField(this.fields.issuingJudgeEmail, 'test@mail.com');
    I.fillField(this.fields.legalAdvisorName, legalAdvisorName);
  },

  async selectAllocatedJudge(legalAdvisorName) {
    I.click(this.fields.useAllocatedJudge);
    I.fillField(this.fields.legalAdvisorName, legalAdvisorName);
  },

  async verifyNextStepsLabel() {
    I.see('the allocated judge');
  },

  async enterCustomDirections(direction) {
    const elementIndex = await I.getActiveElementIndex();
    I.fillField(this.fields.customDirection.fields(elementIndex).title, direction.title);
    I.fillField(this.fields.customDirection.fields(elementIndex).description, direction.description);
    I.selectOption(this.fields.customDirection.fields(elementIndex).assignee, direction.assignee);
    I.click(this.fields.customDirection.fields(elementIndex).dueDateType);
    await I.fillDate(direction.dueDate, this.fields.customDirection.fields(elementIndex).date);
  },

  async clickDateAndTime(directionName) {
    await within(`//h2[text()='${directionName}']/..`, () => {
      I.click('Date and time');
    });
  },

  async clickNumberOfDaysBeforeHearing(directionName) {
    await within(`//h2[text()='${directionName}']/..`, () => {
      I.click('Number of days before hearing');
    });
  },

  async seeDate(directionName, date, format='YYYY-MM-DD HH:mm:ss') {
    return await within(`//h2[text()='${directionName}']/..`, async () => {
      let day = parseInt(await I.grabValueFrom('//*[contains(@class, \'form-group-day\')]/input'));
      let month = parseInt(await I.grabValueFrom('//*[contains(@class, \'form-group-month\')]/input'));
      let year = parseInt(await I.grabValueFrom('//*[contains(@class, \'form-group-year\')]/input'));
      let hour = parseInt(await I.grabValueFrom('//*[contains(@class, \'form-group-hour\')]/input'));
      let minute = parseInt(await I.grabValueFrom('//*[contains(@class, \'form-group-minute\')]/input'));
      let second = parseInt(await I.grabValueFrom('//*[contains(@class, \'form-group-second\')]/input'));


      let actualDate =moment().set({'year': year, 'month': month, 'date': day, 'hour': hour, 'minute': minute, 'second': second})
        .subtract(1, 'month')
        .format(format);

      assert.strictEqual(actualDate, date);

    });
  },

  async seeDays(directionName, days) {
    let actualDays =parseInt(await I.grabValueFrom(`//h2[text()='${directionName}']/..//span[text()='Enter number of days']/../../input`));
    assert.strictEqual(actualDays, days);
  },

  async seeDetails(directionName, details) {
    let actualDetails =await I.grabValueFrom(`//h2[text()='${directionName}']/..//textarea`);
    assert.strictEqual(actualDetails, details);
  },

  markAsDraft() {
    I.click(this.fields.statusRadioGroup.draft);
  },

  async markAsFinal(issueDate) {
    I.click(this.fields.statusRadioGroup.sealed);
    await I.fillDate(issueDate);
  },
};
