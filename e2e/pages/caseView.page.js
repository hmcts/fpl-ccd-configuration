const { I } = inject();
const assert = require('assert');
// eslint-disable-next-line no-unused-vars
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
    changeOfRepresentatives: 'Change of representatives',
    legalBasis: 'Legal basis',
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
    furtherEvidence: 'Documents',
    refusedOrders: 'Refused Orders',
  },
  actionsDropdown: '#next-step',
  goButton: 'Go',
  caseTitle: '.case-title .markdown',
  tasksErrorsTitle: 'Why can\'t I submit my application?',

  async getTaskListErrors() {
    if (await I.hasSelector(`//p[text() = "${this.tasksErrorsTitle}"]`)) {
      I.click(`//p[text() = "${this.tasksErrorsTitle}"]`);

      return (await I.grabTextFrom('details div'))
        .replace('\n\n', '\n')
        .split('\n')
        .filter(item => item);
    }
    return [];
  },

  async goToNewActions(actionSelected) {
    // eslint-disable-next-line no-unused-vars
    const currentUrl = await I.grabCurrentUrl();
    await I.retryUntilExists(async () => {
      I.selectOption(this.actionsDropdown, actionSelected);
      I.click(this.goButton);
  
    }, '#next-step', false);
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
    const taskElement = `//p/a[text()="${task}"]`;
    I.waitForElement(locate(taskElement), 10);
    I.scrollIntoView(taskElement);
    if(status) {
      I.waitForElement(locate(`${taskElement}/../img`).withAttr({title: status}), 10);
    } else {
      I.dontSeeElement(locate(`${taskElement}/../img`));
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

  async checkTaskIsNoPresent(task) {
    I.dontSeeElement(`//p/a[text()="${task}"]`);
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
    const taskTarget = await I.grabAttribute(`//p/a[text()="${task}"]`, 'href');
    assert.strictEqual(!!taskTarget, false);
  },

  async checkTasksHaveErrors(tasksErrors) {
    const errors = await this.getTaskListErrors();

    assert.deepStrictEqual(errors, tasksErrors);
  },

  async checkTasksDoesNotContainError(error) {
    const errors = await this.getTaskListErrors();

    assert.strictEqual(errors.includes(error), false);
  },

  async checkTasksContainsError(error) {
    const errors = await this.getTaskListErrors();

    assert.strictEqual(errors.includes(error), true);
  },

  async checkTasksHaveNoErrors() {
    I.dontSee(this.tasksErrorsTitle);
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

  seeCCDCaseNumber(ccdCaseNumberPrefix, ccdCaseNumber) {
    I.see(ccdCaseNumberPrefix);
    I.see(I.uiFormatted(ccdCaseNumber));
  },

};
