const I = actor();

module.exports = {
  fields: {
    proceedingType: {
      c6: locate('input').withAttr({id: 'proceedingTypes-NOTICE_OF_PROCEEDINGS_FOR_PARTIES'}),
      c6a: locate('input').withAttr({id: 'proceedingTypes-NOTICE_OF_PROCEEDINGS_FOR_NON_PARTIES'}),
    },
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

  checkC6() {
    I.checkOption(this.fields.proceedingType.c6);
  },

  checkC6A() {
    I.checkOption(this.fields.proceedingType.c6a);
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
