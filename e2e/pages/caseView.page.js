const { I } = inject();
const assert = require('assert');
const output = require('codeceptjs').output;

module.exports = {

  file: 'mockFile.txt',
  tabs: {
    summary: 'Summary',
    history: 'History',
    orders: 'Orders',
    draftOrders: 'Draft orders',
    hearings: 'Hearings',
    casePeople: 'People in the case',
    legalBasis: 'Legal basis',
    documents: 'Documents',
    documentsSentToParties: 'Documents sent to parties',
    c2: 'C2',
    confidential: 'Confidential information',
    placement: 'Placement',
    paymentHistory: 'Payment History',
    notes: 'Notes',
    expertReports: 'Expert Reports',
    overview: 'Overview',
    viewApplication: 'View application',
    startApplication: 'Start application',
    correspondence: 'Correspondence',
    courtBundle: 'Court bundle',
    judicialMessages: 'Judicial messages',
    otherApplications: 'Other applications',
  },
  actionsDropdown: '.ccd-dropdown',
  goButton: 'Go',
  caseTitle: '.case-title .markdown',

  async goToNewActions(actionSelected) {
    const currentUrl = await I.grabCurrentUrl();
    await I.retryUntilExists(async () => {
      if(await I.waitForSelector(this.actionsDropdown, 60) != null) {
        I.selectOption(this.actionsDropdown, actionSelected);
        I.click(this.goButton);
      } else {
        const newUrl = await I.grabCurrentUrl();
        if(newUrl === currentUrl){
          output.print('Page refresh');
          I.refreshPage();
        }
      }
    }, 'ccd-case-event-trigger');
  },

  async checkActionsAreAvailable(actions) {
    I.waitForElement(this.actionsDropdown, 10);
    await within(this.actionsDropdown, () => {
      for (const action of actions) {
        I.seeElementInDOM(`//option[text()="${action}"]`);
      }
    });
  },

  async checkActionsAreNotAvailable(actions) {
    I.waitForElement(this.actionsDropdown, 10);
    await within(this.actionsDropdown, () => {
      for (const action of actions) {
        I.dontSeeElementInDOM(`//option[text()="${action}"]`);
      }
    });
  },

  checkTaskStatus(task, status) {
    if(status) {
      I.seeElement(locate(`//p/a[text()="${task}"]/../img`).withAttr({title: status}));
    } else {
      I.seeElement(locate(`//p/a[text()="${task}"]`));
      I.dontSeeElement(locate(`//p/a[text()="${task}"]/../img`));
    }
  },

  checkTaskIsFinished(task) {
    this.checkTaskStatus(task, 'Finished');
  },

  checkTaskIsCompleted(task) {
    this.checkTaskStatus(task, 'Information added');
  },

  checkTaskIsInProgress(task) {
    this.checkTaskStatus(task, 'In progress');
  },

  checkTaskIsNotStarted(task) {
    this.checkTaskStatus(task, undefined);
  },

  async checkTaskIsAvailable(task) {
    await I.retryUntilExists(() => {
      I.click(task);
    }, 'ccd-case-event-trigger');
    await I.retryUntilExists(() => {
      I.click('Cancel');
    }, this.caseTitle);
  },

  async checkTaskIsUnavailable(task) {
    this.checkTaskStatus(task, 'Cannot send yet');
    const taskTarget = await I.grabAttributeFrom(`//p/a[text()="${task}"]`,'href');
    assert.strictEqual(taskTarget, null);
  },

  async startTask(task) {
    await I.retryUntilExists(() => {
      I.click(task);
    }, 'ccd-case-event-trigger');
    await I.runAccessibilityTest();
  },

  getTabSelector(tab){
    return `//*[@role="tab"]/div[text() = "${tab}"]`;
  },

  checkTabIsNotPresent(tab) {
    I.dontSee(this.getTabSelector(tab));
  },

  selectTab(tab) {
    I.click(this.getTabSelector(tab));
  },

  seeInCaseTitle(titleValue) {
    I.seeElement(locate(this.caseTitle).withText(titleValue));
  },
};
