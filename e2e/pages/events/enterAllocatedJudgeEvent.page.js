const { I } = inject();

module.exports = {

  fields: {
    allocatedJudge: {
      judgeTitle: '#allocatedJudge_judgeTitle-HER_HONOUR_JUDGE',
      judgeLastName: '#allocatedJudge_judgeLastName',
    },
  },

  async enterAllocatedJudge(judeLastName) {
    I.click(this.fields.allocatedJudge.judgeTitle);
    I.fillField(this.fields.allocatedJudge.judgeLastName, judeLastName);
  },

};
