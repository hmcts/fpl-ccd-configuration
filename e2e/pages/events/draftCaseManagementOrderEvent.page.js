const { I } = inject();
const directions = require('../../fragments/directions');

module.exports = {
  fields: {
    recitals: {
      title: '#recitals_0_title',
      description: '#recitals_0_description',
    },
    cmoHearingDateList: '#cmoHearingDateList',
    schedule: {
      includeSchedule: '#schedule_includeSchedule-Yes',
      allocation: '#schedule_allocation',
      application: '#schedule_application',
      todaysHearing: '#schedule_todaysHearing',
      childrensCurrentArrangement: '#schedule_childrensCurrentArrangement',
      timetableForProceedings: '#schedule_timetableForProceedings',
      timetableForTheChildren: '#schedule_timetableForChildren',
      alternativeCarers: '#schedule_alternativeCarers',
      threshold: '#schedule_threshold',
      keyIssues: '#schedule_keyIssues',
      partiesPositions: '#schedule_partiesPositions',
    },
    respondentDirectionsCustom: {
      assigneeDropdown: '#respondentDirectionsCustom_0_parentsAndRespondentsAssignee',
    },
    otherPartiesDirectionsCustom: {
      assigneeDropdown: '#otherPartiesDirectionsCustom_0_otherPartiesAssignee',
    },
  },

  staticFields: {
    statusRadioGroup: {
      groupName: '#reviewCaseManagementOrder_cmoStatus',
      sendToJudge: 'Yes, send this to the judge',
      partiesReview: 'No, parties need to review it',
      selfReview: 'No, I need to make changes',
    },
  },

  associateHearingDate(date) {
    I.waitForElement(this.fields.cmoHearingDateList);
    I.selectOption(this.fields.cmoHearingDateList, date);
  },

  async enterDirection(direction) {
    await I.addAnotherElementToCollection();
    await directions.enterTitleAndDescription('allPartiesCustom', direction.title, direction.description);
    await directions.enterDate('allPartiesCustom', direction.dueDate);
    await I.retryUntilExists(() => I.click('Continue'), '#localAuthorityDirectionsLabelCMO');
    await I.addAnotherElementToCollection();
    await directions.enterTitleAndDescription('localAuthorityDirectionsCustom', direction.title, direction.description);
    await directions.enterDate('localAuthorityDirectionsCustom', direction.dueDate);
    await I.retryUntilExists(() => I.click('Continue'), '#respondentsDirectionLabelCMO');
    await I.addAnotherElementToCollection();
    await directions.enterTitleAndDescription('respondentDirectionsCustom', direction.title, direction.description);
    await I.selectOption(this.fields.respondentDirectionsCustom.assigneeDropdown, 'Respondent 1');
    await directions.enterDate('respondentDirectionsCustom', direction.dueDate);
    await I.retryUntilExists(() => I.click('Continue'), '#cafcassDirectionsLabelCMO');
    await I.addAnotherElementToCollection();
    await directions.enterTitleAndDescription('cafcassDirectionsCustom', direction.title, direction.description);
    await directions.enterDate('cafcassDirectionsCustom', direction.dueDate);
    await I.retryUntilExists(() => I.click('Continue'), '#otherPartiesDirectionLabelCMO');
    await I.addAnotherElementToCollection();
    await directions.enterTitleAndDescription('otherPartiesDirectionsCustom', direction.title, direction.description);
    I.selectOption(this.fields.otherPartiesDirectionsCustom.assigneeDropdown, 'Person 1');
    await directions.enterDate('otherPartiesDirectionsCustom', direction.dueDate);
    await I.retryUntilExists(() => I.click('Continue'), '#courtDirectionsLabelCMO');
    await I.addAnotherElementToCollection();
    await directions.enterTitleAndDescription('courtDirectionsCustom', direction.title, direction.description);
    await directions.enterDate('courtDirectionsCustom', direction.dueDate);
  },

  enterSchedule(schedule) {
    I.click(this.fields.schedule.includeSchedule);
    I.fillField(this.fields.schedule.allocation, schedule.allocation);
    I.fillField(this.fields.schedule.application, schedule.application);
    I.fillField(this.fields.schedule.todaysHearing, schedule.todaysHearing);
    I.fillField(this.fields.schedule.childrensCurrentArrangement, schedule.childrensCurrentArrangement);
    I.fillField(this.fields.schedule.timetableForProceedings, schedule.timetableForProceedings);
    I.fillField(this.fields.schedule.timetableForTheChildren, schedule.timetableForTheChildren);
    I.fillField(this.fields.schedule.alternativeCarers, schedule.alternativeCarers);
    I.fillField(this.fields.schedule.threshold, schedule.threshold);
    I.fillField(this.fields.schedule.keyIssues, schedule.keyIssues);
    I.fillField(this.fields.schedule.partiesPositions, schedule.partiesPositions);
  },

  async enterRecital(title, description) {
    I.fillField(this.fields.recitals.title, title);
    I.fillField(this.fields.recitals.description, description);
  },

  markToBeSentToJudge() {
    within(this.staticFields.statusRadioGroup.groupName, () => {
      I.click(locate('label').withText(this.staticFields.statusRadioGroup.sendToJudge));
    });
  },

  markToBeReviewedByParties() {
    within(this.staticFields.statusRadioGroup.groupName, () => {
      I.click(locate('label').withText(this.staticFields.statusRadioGroup.partiesReview));
    });
  },

  markToReviewedBySelf() {
    within(this.staticFields.statusRadioGroup.groupName, () => {
      I.click(locate('label').withText(this.staticFields.statusRadioGroup.selfReview));
    });
  },

};
