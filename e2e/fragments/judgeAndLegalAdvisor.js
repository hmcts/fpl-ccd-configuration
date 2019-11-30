const { I } = inject();

module.exports = {
  fields: {
    judgeTitleRadioGroup: {
      groupName: 'judgeAndLegalAdvisor_judgeTitle',
      herHonourJudge: 'Her Honour Judge',
      hisHonourJudge: 'His Honour Judge',
      districtJudge: 'District Judge',
      deputyDistrictJudge: 'Deputy District Judge',
      districtJudgeMagistratesCourt: 'District Judge Magistrates Court',
      magistrates: 'Magistrates (JP)',
      other: 'Other',
    },
    otherTitle: 'judgeAndLegalAdvisor_otherTitle',
    judgeLastName: 'judgeAndLegalAdvisor_judgeLastName',
    legalAdvisorName: 'judgeAndLegalAdvisor_legalAdvisorName',
  },

  selectJudgeTitle(complexTypeAppender = '', title = this.fields.judgeTitleRadioGroup.herHonourJudge, otherTitle = '') {
    within('#' + complexTypeAppender + this.fields.judgeTitleRadioGroup.groupName, () => {
      I.click(locate('label').withText(title));
    });

    if (title === this.fields.judgeTitleRadioGroup.other) {
      I.fillField('#' + complexTypeAppender + this.fields.otherTitle, otherTitle);
    }
  },

  enterJudgeLastName(judgeLastName, complexTypeAppender = '') {
    I.fillField('#' + complexTypeAppender + this.fields.judgeLastName, judgeLastName);
  },

  enterLegalAdvisorName(legalAdvisorName, complexTypeAppender = '') {
    I.fillField('#' + complexTypeAppender + this.fields.legalAdvisorName, legalAdvisorName);
  },
};
