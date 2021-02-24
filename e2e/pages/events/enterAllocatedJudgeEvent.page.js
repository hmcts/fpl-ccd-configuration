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
    await I.runAccessibilityTest();
    console.log('enter allocated judge');
    I.click(this.fields.allocatedJudge.judgeTitle);
    I.fillField(this.fields.allocatedJudge.judgeLastName, judgeLastName);
    I.fillField(this.fields.allocatedJudge.judgeEmailAddress, judgeEmailAddress);
  },
};
