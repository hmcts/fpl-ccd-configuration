const { I } = inject();

module.exports = {
  fields: {
    allocatedJudge: {
      judgeTitle: '#allocatedJudge_judgeTitle-HER_HONOUR_JUDGE',
      judgeLastName: '#allocatedJudge_judgeLastName',
      judgeEmailAddress: '#allocatedJudge_judgeEmailAddress',
    },
  },

  async enterAllocatedJudge(judgeLastName, judgeEmailAddress) {
    I.click(this.fields.allocatedJudge.judgeTitle);
    await I.runAccessibilityTest();
    I.fillField(this.fields.allocatedJudge.judgeLastName, judgeLastName);
    I.fillField(this.fields.allocatedJudge.judgeEmailAddress, judgeEmailAddress);
  },
};
