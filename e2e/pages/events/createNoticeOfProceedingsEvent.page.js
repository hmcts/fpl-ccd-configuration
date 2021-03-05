const I = actor();
const judgeAndLegalAdvisor = require('../../fragments/judgeAndLegalAdvisor');

module.exports = {
  fields: {
    proceedingType: {
      c6: locate('input').withAttr({id: 'noticeOfProceedings_proceedingTypes-NOTICE_OF_PROCEEDINGS_FOR_PARTIES'}),
      c6a: locate('input').withAttr({id: 'noticeOfProceedings_proceedingTypes-NOTICE_OF_PROCEEDINGS_FOR_NON_PARTIES'}),
    },
  },

  async checkC6() {
    I.checkOption(this.fields.proceedingType.c6);
  },

  async checkC6A() {
    await I.runAccessibilityTest();
    I.checkOption(this.fields.proceedingType.c6a);
  },

  selectJudgeTitle() {
    judgeAndLegalAdvisor.selectJudgeTitle('noticeOfProceedings_');
  },

  enterJudgeLastName(judgeLastName) {
    judgeAndLegalAdvisor.enterJudgeLastName(judgeLastName, 'noticeOfProceedings_');
  },

  enterJudgeEmailAddress(judgeEmailAddress) {
    judgeAndLegalAdvisor.enterJudgeEmailAddress(judgeEmailAddress, 'noticeOfProceedings_');
  },

  enterLegalAdvisorName(legalAdvisorName) {
    judgeAndLegalAdvisor.enterLegalAdvisorName(legalAdvisorName, 'noticeOfProceedings_');
  },

  useAllocatedJudge() {
    judgeAndLegalAdvisor.useAllocatedJudge('noticeOfProceedings_');
  },

  useAlternateJudge() {
    judgeAndLegalAdvisor.useAlternateJudge('noticeOfProceedings_');
  },
};
