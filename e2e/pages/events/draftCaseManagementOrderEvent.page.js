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
    parentsAndRespondentsDirection: {
      assigneeDropdown: '#parentsAndRespondentsCustom_0_parentsAndRespondentsAssignee'
    },
    otherPartiesDirectionsCustom: {
      assigneeDropdown: '#otherPartiesDirectionsCustom_0_otherPartiesAssignee'
    }
  },

  associateHearingDate(date) {
    I.waitForElement(this.fields.cmoHearingDateList);
    I.selectOption(this.fields.cmoHearingDateList, date);
  },

  validatePreviousSelectedHearingDate(date) {
    I.waitForElement(this.fields.cmoHearingDateList);
    I.see(date,this.fields.cmoHearingDateList);
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

  async enterDirection(direction) {
    await I.addAnotherElementToCollection();
    await draftDirections.enterTitleAndDescription('parentsAndRespondentsCustom', direction);
    await I.selectOption(this.fields.parentsAndRespondentsDirection.assigneeDropdown, 'Respondent 1');
    await draftDirections.enterDate('parentsAndRespondentsCustom', direction);
    await I.click('Continue');
    await I.addAnotherElementToCollection();
    await draftDirections.enterTitleAndDescription('otherPartiesDirectionsCustom', direction);
    I.selectOption(this.fields.otherPartiesDirectionsCustom.assigneeDropdown, 'Person 1');
    await draftDirections.enterDate('otherPartiesDirectionsCustom', direction);
  },

  async enterRecital(title,description) {
    I.fillField(this.fields.recitals.title, title);
    I.fillField(this.fields.recitals.description, description);
  },
};
