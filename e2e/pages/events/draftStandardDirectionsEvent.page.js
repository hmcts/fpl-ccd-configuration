const {I} = inject();
const judgeAndLegalAdvisor = require('../../fragments/judgeAndLegalAdvisor');
const directions = require('../../fragments/directions');

module.exports = {
  fields: {
    statusRadioGroup: {
      groupName: '#standardDirectionOrder_orderStatus',
      sealed: 'Yes, seal it and send to the local authority',
      draft: 'No, just save it on the system',
    },
    routingRadioGroup: {
      groupName: '#sdoRouter',
      service: '#sdoRouter-SERVICE',
      upload: '#sdoRouter-UPLOAD',
      urgent: '#sdoRouter-URGENT',
    },
    file: {
      preparedSDO: '#preparedSDO',
      replacementSDO: '#replacementSDO',
    },
    noticeOfProceedings: {
      groupName: '#noticeOfProceedings_proceedingTypes',
      c6: locate('input').withAttr({id: 'noticeOfProceedings_proceedingTypes-NOTICE_OF_PROCEEDINGS_FOR_PARTIES'}),
      c6a: locate('input').withAttr({id: 'noticeOfProceedings_proceedingTypes-NOTICE_OF_PROCEEDINGS_FOR_NON_PARTIES'}),
    },
    allocationDecision: {
      judgeLevelConfirmation: {
        yes: '#urgentHearingAllocation_judgeLevelRadio-Yes',
        no: '#urgentHearingAllocation_judgeLevelRadio-No',
      },
      allocationLevel: {
        // ids have spaces in so don't work
        circuit: 'Circuit Judge',
        section9Circuit: 'Circuit Judge (Section 9)',
        district: 'District Judge',
        magistrate: 'Magistrate',
        highCourt: 'High Court Judge',
      },
      reason: '#urgentHearingAllocation_proposalReason',
    },
    urgentHearingOrder: '#urgentHearingOrderDocument',
  },

  async createSDOThroughService() {
    I.click(this.fields.routingRadioGroup.service);
    await I.runAccessibilityTest();
    await I.goToNextPage();
  },

  async createSDOThroughUpload() {
    I.click(this.fields.routingRadioGroup.upload);
    await I.runAccessibilityTest();
    await I.goToNextPage();
  },

  async createUrgentHearingOrder() {
    I.click(this.fields.routingRadioGroup.urgent);
    await I.runAccessibilityTest();
    await I.goToNextPage();
  },

  async uploadPreparedSDO(file) {
    await I.runAccessibilityTest();
    I.attachFile(this.fields.file.preparedSDO, file);
    await I.goToNextPage();
  },

  async uploadReplacementSDO(file) {
    await I.runAccessibilityTest();
    I.attachFile(this.fields.file.replacementSDO, file);
    await I.goToNextPage();
  },

  async skipDateOfIssue() {
    await I.runAccessibilityTest();
    await I.goToNextPage();
  },

  async enterDateOfIssue(date) {
    await I.runAccessibilityTest();
    await I.fillDate(date);
    await I.goToNextPage();
  },

  useAllocatedJudge(legalAdvisorName) {
    judgeAndLegalAdvisor.useAllocatedJudge();
    judgeAndLegalAdvisor.enterLegalAdvisorName(legalAdvisorName);
  },

  async enterDatesForDirections(direction) {
    await directions.enterDate('allParties', direction.dueDate);
    await I.goToNextPage();
    await directions.enterDate('localAuthorityDirections', direction.dueDate);
    await I.goToNextPage();
    await directions.enterDate('respondentDirections', direction.dueDate);
    await I.goToNextPage();
    await directions.enterDate('cafcassDirections', direction.dueDate);
    await I.goToNextPage();
    await directions.enterDate('otherPartiesDirections', direction.dueDate);
    await I.goToNextPage();
    await directions.enterDate('courtDirections', direction.dueDate);
    await I.goToNextPage();
  },

  markAsDraft() {
    I.click(this.fields.statusRadioGroup.draft);
  },

  async markAsFinal() {
    await I.runAccessibilityTest();
    I.click(this.fields.statusRadioGroup.sealed);
  },

  checkC6() {
    I.checkOption(this.fields.noticeOfProceedings.c6);
  },

  checkC6A() {
    I.checkOption(this.fields.noticeOfProceedings.c6a);
  },

  async makeAllocationDecision(agreement, level, reason) {
    I.click(agreement);
    if (agreement === this.fields.allocationDecision.judgeLevelConfirmation.no) {
      I.click(level);
      I.fillField(this.fields.allocationDecision.reason, reason);
    }
    await I.runAccessibilityTest();
  },

  async uploadUrgentHearingOrder(order) {
    I.attachFile(this.fields.urgentHearingOrder, order);
    await I.runAccessibilityTest();
  },
};
