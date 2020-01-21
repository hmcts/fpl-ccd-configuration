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
    respondentDirectionsCustomCMO: {
      assigneeDropdown: '#respondentDirectionsCustomCMO_0_parentsAndRespondentsAssignee',
    },
    otherPartiesDirectionsCustomCMO: {
      assigneeDropdown: '#otherPartiesDirectionsCustomCMO_0_otherPartiesAssignee',
    },
  },

  staticFields: {
    statusRadioGroup: {
      groupName: '#caseManagementOrder_status',
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
    await directions.enterTitleAndDescription('allPartiesCustomCMO', direction.title, direction.description);
    await directions.enterDate('allPartiesCustomCMO', direction.dueDate);
    await I.retryUntilExists(() => I.click('Continue'), '#localAuthorityDirectionsLabelCMO');
    await I.addAnotherElementToCollection();
    await directions.enterTitleAndDescription('localAuthorityDirectionsCustomCMO', direction.title, direction.description);
    await directions.enterDate('localAuthorityDirectionsCustomCMO', direction.dueDate);
    await I.retryUntilExists(() => I.click('Continue'), '#respondentsDirectionLabelCMO');
    await I.addAnotherElementToCollection();
    await directions.enterTitleAndDescription('respondentDirectionsCustomCMO', direction.title, direction.description);
    await I.selectOption(this.fields.respondentDirectionsCustomCMO.assigneeDropdown, 'Respondent 1');
    await directions.enterDate('respondentDirectionsCustomCMO', direction.dueDate);
    await I.retryUntilExists(() => I.click('Continue'), '#cafcassDirectionsLabelCMO');
    await I.addAnotherElementToCollection();
    await directions.enterTitleAndDescription('cafcassDirectionsCustomCMO', direction.title, direction.description);
    await directions.enterDate('cafcassDirectionsCustomCMO', direction.dueDate);
    await I.retryUntilExists(() => I.click('Continue'), '#otherPartiesDirectionLabelCMO');
    await I.addAnotherElementToCollection();
    await directions.enterTitleAndDescription('otherPartiesDirectionsCustomCMO', direction.title, direction.description);
    I.selectOption(this.fields.otherPartiesDirectionsCustomCMO.assigneeDropdown, 'Person 1');
    await directions.enterDate('otherPartiesDirectionsCustomCMO', direction.dueDate);
    await I.retryUntilExists(() => I.click('Continue'), '#courtDirectionsLabelCMO');
    await I.addAnotherElementToCollection();
    await directions.enterTitleAndDescription('courtDirectionsCustomCMO', direction.title, direction.description);
    await directions.enterDate('courtDirectionsCustomCMO', direction.dueDate);
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
