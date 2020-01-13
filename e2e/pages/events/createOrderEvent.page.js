const {I} = inject();
const judgeAndLegalAdvisor = require('../../fragments/judgeAndLegalAdvisor');
const orders = require('../../fixtures/orders.js');

module.exports = {
  fields: {
    title: '#order_title',
    details: '#order_details',
    orderTypeList: '#orderTypeAndDocument_type',
    directionsNeeded: {
      id: '#orderFurtherDirections_directionsNeeded',
      options: {
        yes: '#orderFurtherDirections_directionsNeeded-Yes',
        no: '#orderFurtherDirections_directionsNeeded-No',
      },
    },
    directions: '#orderFurtherDirections_directions',
    months: '#orderMonths',
  },

  selectType(type) {
    within(this.fields.orderTypeList, () => {
      I.click(locate('label').withText(type));
    });
  },

  enterC21OrderDetails() {
    I.fillField(this.fields.title, orders[0].title);
    I.fillField(this.fields.details, orders[0].details);
  },

  async enterJudgeAndLegalAdvisor(judgeLastName, legalAdvisorName, judgeTitle = judgeAndLegalAdvisor.fields.judgeTitleRadioGroup.herHonourJudge) {
    judgeAndLegalAdvisor.selectJudgeTitle('', judgeTitle);
    judgeAndLegalAdvisor.enterJudgeLastName(judgeLastName);
    judgeAndLegalAdvisor.enterLegalAdvisorName(legalAdvisorName);
  },

  enterDirections(directions) {
    I.click(this.fields.directionsNeeded.options.yes);
    I.fillField(this.fields.directions, directions);
  },

  enterNumberOfMonths(numOfMonths) {
    I.fillField(this.fields.months, numOfMonths);
  },
};
