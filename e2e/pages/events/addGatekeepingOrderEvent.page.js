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
          date: `#sdoDirectionCustom_${index}_dateToBeCompletedBy`,
        };
      },
    },
    useAllocatedJudge: '#gatekeepingOrderIssuingJudge_useAllocatedJudge-Yes',
    legalAdvisorName: '#gatekeepingOrderIssuingJudge_legalAdvisorName',
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
    await I.runAccessibilityTest();
    I.click(this.fields.statusRadioGroup.sealed);
    await I.runAccessibilityTest();
    await I.fillDate(issueDate);
  },
};
