const {I} = inject();
const moment = require('moment');
const assert = require('assert');

module.exports = {
  fields: {
    routingRadioGroup: {
      groupName: '#gatekeepingOrderRouter',
      service: '#gatekeepingOrderRouter-SERVICE',
    },
    standardDirection: directionName => `//h2[text()='${directionName}']/..`,
    customDirection: {
      fields(index) {
        return {
          title: `#customDirections_${index}_title`,
          description: `#customDirections_${index}_description`,
          assignee: `#customDirections_${index}_assignee`,
          dueDateType: `#customDirections_${index}_dueDateType-DATE`,
          date: `#customDirections_${index}_dateToBeCompletedBy`,
        };
      },
    },
    useAllocatedJudge: '#gatekeepingOrderIssuingJudge_useAllocatedJudge_Yes',
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
    I.click(this.fields.customDirection.fields(elementIndex).dueDateType);
    const dateSelector = `(//*[contains(@class, "collection-title")])[${elementIndex+1}]/parent::div//*[@id="dateToBeCompletedBy"]`;
    await I.fillDate(direction.dueDate, dateSelector);
  },

  async clickDateAndTime(directionName) {
    await within(this.fields.standardDirection(directionName), () => I.click('Date and time'));
  },

  async clickNumberOfDaysBeforeHearing(directionName) {
    await within(this.fields.standardDirection(directionName), () => I.click('Number of working days before hearing'));
  },

  async seeDate(directionName, date, format = 'YYYY-MM-DD HH:mm:ss') {
    const getDateField = async field => parseInt(await I.grabValueFrom(`//*[contains(@class, 'form-group-${field}')]/input`));

    return await within(this.fields.standardDirection(directionName), async () => {
      let day = await getDateField('day');
      let month = await getDateField('month');
      let year = await getDateField('year');
      let hour = await getDateField('hour');
      let minute = await getDateField('minute');
      let second = await getDateField('second');

      let actualDate = moment().set({
        'year': year,
        'month': month,
        'date': day,
        'hour': hour,
        'minute': minute,
        'second': second,
      })
        .subtract(1, 'month')
        .format(format);

      assert.strictEqual(actualDate, date);
    });
  },

  async seeDays(directionName, days) {
    let actualDays = parseInt(await I.grabValueFrom(`${this.fields.standardDirection(directionName)}//span[text()='Number of days']/../../input`));
    assert.strictEqual(actualDays, days);
  },

  async seeDetails(directionName, details) {
    let actualDetails = await I.grabValueFrom(`${this.fields.standardDirection(directionName)}//textarea`);
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
