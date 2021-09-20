const {I} = inject();

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
    judgeEmailAddress: 'judgeAndLegalAdvisor_judgeEmailAddress',
    legalAdvisorName: 'judgeAndLegalAdvisor_legalAdvisorName',
  },

  useAllocatedJudge(complexTypeAppender = '') {
    I.click(`#${complexTypeAppender}${this.fields.useAllocatedJudge.groupName}_Yes`);
  },

  useAlternateJudge(complexTypeAppender = '') {
    I.click(`#${complexTypeAppender}${this.fields.useAllocatedJudge.groupName}_No`);
  },

  selectJudgeTitle(complexTypeAppender = '', title = this.fields.judgeTitleRadioGroup.herHonourJudge, otherTitle = '') {
    I.click(`#${complexTypeAppender}${this.fields.judgeTitleRadioGroup.groupName}-${judgeTitleToIdMap[title]}`);
    if (title === this.fields.judgeTitleRadioGroup.other) {
      I.fillField('#' + complexTypeAppender + this.fields.otherTitle, otherTitle);
    }
  },

  enterJudgeLastName(judgeLastName, complexTypeAppender = '') {
    I.fillField('#' + complexTypeAppender + this.fields.judgeLastName, judgeLastName);
  },

  enterJudgeEmailAddress(judgeEmailAddress, complexTypeAppender = '') {
    I.fillField('#' + complexTypeAppender + this.fields.judgeEmailAddress, judgeEmailAddress);
  },

  enterLegalAdvisorName(legalAdvisorName, complexTypeAppender = '') {
    I.fillField('#' + complexTypeAppender + this.fields.legalAdvisorName, legalAdvisorName);
  },
};

const judgeTitleToIdMap = {
  'Her Honour Judge': 'HER_HONOUR_JUDGE',
  'His Honour Judge': 'HIS_HONOUR_JUDGE',
  'District Judge': 'DISTRICT_JUDGE',
  'Deputy District Judge': 'DEPUTY_DISTRICT_JUDGE',
  'District Judge Magistrates Court': 'DEPUTY_DISTRICT_JUDGE_MAGISTRATES_COURT',
  'Magistrates': 'MAGISTRATES',
  'Other': 'OTHER',
};
