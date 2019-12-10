const {I} = inject();
const judgeAndLegalAdvisor = require('../../fragments/judgeAndLegalAdvisor');
const orders = require('../../fixtures/orders.js');

module.exports = {
  fields: {
    orderTitle: '#order_orderTitle',
    orderDetails: '#order_orderDetails',
  },

  enterC21OrderDetails() {
    I.fillField(this.fields.orderTitle, orders[0].orderTitle);
    I.fillField(this.fields.orderDetails, orders[0].orderDetails);
  },

  async enterJudgeAndLegalAdvisor(judgeLastName, legalAdvisorName) {
    judgeAndLegalAdvisor.selectJudgeTitle();
    judgeAndLegalAdvisor.enterJudgeLastName(judgeLastName);
    judgeAndLegalAdvisor.enterLegalAdvisorName(legalAdvisorName);
  },
};
