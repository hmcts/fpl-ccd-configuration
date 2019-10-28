const { I } = inject();

module.exports = {
  fields: {
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

  selectJudgeTitle() {
    within(this.fields.judgeTitleRadioGroup.groupName, () => {
      I.click(locate('label').withText(this.fields.judgeTitleRadioGroup.herHonourJudge));
    });
  },

  enterJudgeLastName(judgeLastName) {
    I.fillField(this.fields.judgeLastName, judgeLastName);
  },

  enterLegalAdvisorName(legalAdvisorName) {
    I.fillField(this.fields.legalAdvisorName, legalAdvisorName);
  },
};
