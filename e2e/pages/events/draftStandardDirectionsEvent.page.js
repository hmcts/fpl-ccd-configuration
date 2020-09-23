const { I } = inject();
const judgeAndLegalAdvisor = require('../../fragments/judgeAndLegalAdvisor');
const directions = require('../../fragments/directions');

module.exports = {
  fields: {
    statusRadioGroup: {
      groupName: '#standardDirectionOrder_orderStatus',
      sealed: 'Yes, it can be sealed and sent to parties',
      draft: 'No, just save it on the system',
    },
    routingRadioGroup: {
      groupName: '#sdoRouter',
      service: '#sdoRouter-SERVICE',
      upload: '#sdoRouter-UPLOAD',
    },
    file: {
      preparedSDO: '#preparedSDO',
      replacementSDO: '#replacementSDO',
    },
  },

  async createSDOThroughService() {
    await I.click(this.fields.routingRadioGroup.service);
    await I.retryUntilExists(() => I.click('Continue'), '#dateOfIssue_label');
  },

  async createSDOThroughUpload() {
    await I.click(this.fields.routingRadioGroup.upload);
    await I.retryUntilExists(() => I.click('Continue'), this.fields.file.preparedSDO);
  },

  async uploadPreparedSDO(file) {
    await I.attachFile(this.fields.file.preparedSDO, file);
    await I.retryUntilExists(() => I.click('Continue'), this.fields.statusRadioGroup.groupName);
  },

  async uploadReplacementSDO(file) {
    await I.attachFile(this.fields.file.replacementSDO, file);
    await I.retryUntilExists(() => I.click('Continue'), this.fields.statusRadioGroup.groupName);
  },

  async skipDateOfIssue(){
    await this.enterDateOfIssue();
  },

  async enterDateOfIssue(date){
    await I.fillDate(date);
    await I.retryUntilExists(() => I.click('Continue'), '#judgeAndLegalAdvisor_judgeAndLegalAdvisor');
  },

  async useAllocatedJudge(legalAdvisorName) {
    judgeAndLegalAdvisor.useAllocatedJudge();
    judgeAndLegalAdvisor.enterLegalAdvisorName(legalAdvisorName);
    await I.retryUntilExists(() => I.click('Continue'), '#allParties');
  },

  async enterDatesForDirections(direction) {
    await directions.enterDate('allParties', direction.dueDate);
    await I.retryUntilExists(() => I.click('Continue'), '#localAuthorityDirections');
    await directions.enterDate('localAuthorityDirections', direction.dueDate);
    await I.retryUntilExists(() => I.click('Continue'), '#respondentDirections');
    await directions.enterDate('respondentDirections', direction.dueDate);
    await I.retryUntilExists(() => I.click('Continue'), '#cafcassDirections');
    await directions.enterDate('cafcassDirections', direction.dueDate);
    await I.retryUntilExists(() => I.click('Continue'), '#otherPartiesDirections');
    await directions.enterDate('otherPartiesDirections', direction.dueDate);
    await I.retryUntilExists(() => I.click('Continue'), '#courtDirections');
    await directions.enterDate('courtDirections', direction.dueDate);
    await I.retryUntilExists(() => I.click('Continue'), this.fields.statusRadioGroup.groupName);
  },

  markAsDraft() {
    within(this.fields.statusRadioGroup.groupName, () => {
      I.click(locate('label').withText(this.fields.statusRadioGroup.draft));
    });
  },

  markAsFinal() {
    within(this.fields.statusRadioGroup.groupName, () => {
      I.click(locate('label').withText(this.fields.statusRadioGroup.sealed));
    });
  },
};
