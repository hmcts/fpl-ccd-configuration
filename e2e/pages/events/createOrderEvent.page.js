const {I} = inject();
const judgeAndLegalAdvisor = require('../../fragments/judgeAndLegalAdvisor');
const orders = require('../../fixtures/orders.js');

module.exports = {
  fields: {
    title: '#order_title',
    details: '#order_details',
    orderTypeList: '#orderTypeAndDocument_type',
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

  async enterJudgeAndLegalAdvisor(judgeLastName, legalAdvisorName) {
    judgeAndLegalAdvisor.selectJudgeTitle();
    judgeAndLegalAdvisor.enterJudgeLastName(judgeLastName);
    judgeAndLegalAdvisor.enterLegalAdvisorName(legalAdvisorName);
  },
};
