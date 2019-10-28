const I = actor();
const judgeAndLegalAdvisor = require('../../fragments/judgeAndLegalAdvisor');

module.exports = {
  fields: {
    proceedingType: {
      c6: locate('input').withAttr({id: 'proceedingTypes-NOTICE_OF_PROCEEDINGS_FOR_PARTIES'}),
      c6a: locate('input').withAttr({id: 'proceedingTypes-NOTICE_OF_PROCEEDINGS_FOR_NON_PARTIES'}),
    },
  },

  checkC6() {
    I.checkOption(this.fields.proceedingType.c6);
  },

  checkC6A() {
    I.checkOption(this.fields.proceedingType.c6a);
  },

  selectJudgeTitle() {
    judgeAndLegalAdvisor.selectJudgeTitle();
  },

  enterJudgeLastName(judgeLastName) {
    judgeAndLegalAdvisor.enterJudgeLastName(judgeLastName);
  },

  enterLegalAdvisorName(legalAdvisorName) {
    judgeAndLegalAdvisor.enterLegalAdvisorName(legalAdvisorName);
  },
};
