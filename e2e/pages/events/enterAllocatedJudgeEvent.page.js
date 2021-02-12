const { I } = inject();

module.exports = {
  fields: {
    allocatedJudge: {
      judgeTitle: '#allocatedJudge_judgeTitle-HER_HONOUR_JUDGE',
      judgeLastName: '#allocatedJudge_judgeLastName',
      judgeEmailAddress: '#allocatedJudge_judgeEmailAddress',
    },
  },

  enterAllocatedJudge(judgeLastName, judgeEmailAddress) {
    I.click(this.fields.allocatedJudge.judgeTitle);
    I.fillField(this.fields.allocatedJudge.judgeLastName, judgeLastName);
    I.fillField(this.fields.allocatedJudge.judgeEmailAddress, judgeEmailAddress);
    //I.runAccessibilityTest();
  },
};
