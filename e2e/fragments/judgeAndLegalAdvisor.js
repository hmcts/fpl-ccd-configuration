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
    useAllocatedJudge: {
      groupName: 'judgeAndLegalAdvisor_useAllocatedJudge',
      yes: 'Yes',
      no: 'No',
    },
    otherTitle: 'judgeAndLegalAdvisor_otherTitle',
    judgeLastName: 'judgeAndLegalAdvisor_judgeLastName',
    legalAdvisorName: 'judgeAndLegalAdvisor_legalAdvisorName',
  },

  useAllocatedJudge(complexTypeAppender = '') {
    within('#' + complexTypeAppender + this.fields.useAllocatedJudge.groupName, () => {
      I.click(locate('label').withText(this.fields.useAllocatedJudge.yes));
    });
  },

  useAlternateJudge(complexTypeAppender = '') {
    within('#' + complexTypeAppender + this.fields.useAllocatedJudge.groupName, () => {
      I.click(locate('label').withText(this.fields.useAllocatedJudge.no));
    });
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
