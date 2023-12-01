const {I} = inject();

module.exports = {
  fields: {
    judgeTitleRadioGroup: {
      groupName: 'hearingJudge_judgeTitle',
      herHonourJudge: 'Her Honour Judge',
      hisHonourJudge: 'His Honour Judge',
      districtJudge: 'District Judge',
      deputyDistrictJudge: 'Deputy District Judge',
      districtJudgeMagistratesCourt: 'District Judge Magistrates Court',
      magistrates: 'Magistrates (JP)',
      other: 'Other',
    },
    useAllocatedJudge: {
      groupName: 'useAllocatedJudge_radio',
      yes: 'useAllocatedJudge_Yes',
      no: 'useAllocatedJudge_No',
    },
    uselegaladviser: {
      groupName: 'enterManuallyHearingJudge_radio',
      yes: 'enterManuallyHearingJudge_Yes',
      no: 'enterManuallyHearingJudge_No',
    },
    otherTitle: 'judgeAndLegalAdvisor_otherTitle',
    judgeLastName: 'hearingJudge_judgeLastName',
    judgeEmailAddress: 'hearingJudge_judgeEmailAddress',
    legalAdvisorName: 'legalAdvisorName',
  },

  useAllocatedJudge(complexTypeAppender = '') {
    I.click(`#${complexTypeAppender}${this.fields.useAllocatedJudge.yes}`);
  },

  useAlternateJudge(complexTypeAppender = '') {
    I.click(`#${complexTypeAppender}${this.fields.useAllocatedJudge.no}`);
  }, useLegaladviser(complexTypeAppender = '') {
    I.click(`#${complexTypeAppender}${this.fields.uselegaladviser.yes}`);
  },

  useAlternatelegaladviser(complexTypeAppender = '') {
    I.click(`#${complexTypeAppender}${this.fields.uselegaladviser.no}`);
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
