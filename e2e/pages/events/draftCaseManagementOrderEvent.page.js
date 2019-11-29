const { I } = inject();
const draftDirections = require('../../fragments/draftDirections');

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

  validatePreviousSelectedHearingDate(date) {
    I.waitForElement(this.fields.cmoHearingDateList);
    I.see(date, this.fields.cmoHearingDateList);
  },

  async enterDirection(direction) {
    await I.addAnotherElementToCollection();
    await draftDirections.enterTitleAndDescription('allPartiesCustom', direction);
    await draftDirections.enterDate('allPartiesCustom', direction);
    await I.click('Continue');
    await I.addAnotherElementToCollection();
    await draftDirections.enterTitleAndDescription('localAuthorityDirectionsCustom', direction);
    await draftDirections.enterDate('localAuthorityDirectionsCustom', direction);
    await I.click('Continue');
    await I.addAnotherElementToCollection();
    await draftDirections.enterTitleAndDescription('cafcassDirectionsCustom', direction);
    await draftDirections.enterDate('cafcassDirectionsCustom', direction);
    await I.click('Continue');
    await I.addAnotherElementToCollection();
    await draftDirections.enterTitleAndDescription('courtDirectionsCustom', direction);
    await draftDirections.enterDate('courtDirectionsCustom', direction);
  },

  async enterSchedule(schedule) {
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

  async enterRecital(title,description) {
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
