const { I } = inject();

module.exports = {

  fields: {
    orderTitle: '#temporaryC21Order_orderTitle',
    orderDetails: '#temporaryC21Order_orderDetails',
    judgeTitleRadioGroup: {
      groupName: '#judgeAndLegalAdvisor_judgeTitle',
      herHonourJudge: 'Her Honour Judge',
      hisHonourJudge: 'His Honour Judge',
      deputyDistrictJudge: 'Deputy District Judge',
      magistrates: 'Magistrates (JP)',
    },
    judgeLastName: '#judgeAndLegalAdvisor_judgeLastName',
    legalAdvisorName: '#judgeAndLegalAdvisor_legalAdvisorName',
  },

  enterOrder(){
    I.fillField(this.fields.orderTitle, 'Example Title');
    I.fillField(this.fields.orderDetails, 'Example order details here - Lorem ipsum dolor sit amet...');
  },

  selectJudgeTitle() {
    within(this.fields.judgeTitleRadioGroup.groupName, () => {
      I.click(locate('label').withText(this.fields.judgeTitleRadioGroup.herHonourJudge));
    });
  },

  async enterJudgeLastName(judgeLastName) {
    await I.fillField(this.fields.judgeLastName, judgeLastName);
  },

  async enterLegalAdvisorName(legalAdvisorName) {
    await I.fillField(this.fields.legalAdvisorName, legalAdvisorName);
  },
};
