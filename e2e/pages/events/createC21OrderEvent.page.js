const { I } = inject();
const judgeAndLegalAdvisor = require('../../fragments/judgeAndLegalAdvisor');

module.exports = {
  fields: {
    orderTitle: '#temporaryC21Order_orderTitle',
    orderDetails: '#temporaryC21Order_orderDetails',
  },

  async enterOrder(){
    await I.fillField(this.fields.orderTitle, 'Example Title');
    await I.fillField(this.fields.orderDetails, 'Example order details here - Lorem ipsum dolor sit amet...');
  },

  async enterJudgeAndLegalAdvisor(judgeLastName, legalAdvisorName) {
    judgeAndLegalAdvisor.selectJudgeTitle();
    judgeAndLegalAdvisor.enterJudgeLastName(judgeLastName);
    judgeAndLegalAdvisor.enterLegalAdvisorName(legalAdvisorName);
  },
};
