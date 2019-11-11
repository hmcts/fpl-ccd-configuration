const {I} = inject();
const judgeAndLegalAdvisor = require('../../fragments/judgeAndLegalAdvisor');

module.exports = {
  fields: {
    orderTitle: '#c21Order_orderTitle',
    orderDetails: '#c21Order_orderDetails',
  },

  enterOrder() {
    I.fillField(this.fields.orderTitle, 'Example Title');
    I.fillField(this.fields.orderDetails, 'Example order details here - Lorem ipsum dolor sit amet...');
  },

  async enterJudgeAndLegalAdvisor(judgeLastName, legalAdvisorName) {
    judgeAndLegalAdvisor.selectJudgeTitle();
    judgeAndLegalAdvisor.enterJudgeLastName(judgeLastName);
    judgeAndLegalAdvisor.enterLegalAdvisorName(legalAdvisorName);
  },
};
