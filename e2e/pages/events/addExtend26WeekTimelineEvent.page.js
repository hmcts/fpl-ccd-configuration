const { I } = inject();

module.exports = {
  fields: {
    extendsForAllChildren_Yes: '#extensionForAllChildren_Yes',
    extendsForAllChildren_No: '#extensionForAllChildren_No',
    sameExtensionForAllChildren_Yes: '#sameExtensionForAllChildren_Yes',
    sameExtensionForAllChildren_No: '#sameExtensionForAllChildren_No',
    eightWeekExtensionOption: '#childExtensionAll_caseExtensionTimeList-EightWeekExtension',
    timetableForChildReason: '#childExtensionAll_caseExtensionReasonList-TIMETABLE_FOR_CHILD',
  },

  async selectExtendsForAllChildren() {
    await I.runAccessibilityTest();
    I.click(this.fields.extendsForAllChildren_Yes);
  },

  async selectSameExtensionForAllChildren() {
    await I.runAccessibilityTest();
    I.click(this.fields.sameExtensionForAllChildren_Yes);
  },

  async selectEightWeekExtensionTime() {
    await I.runAccessibilityTest();
    I.click(this.fields.eightWeekExtensionOption);
  },

  selectTimetableForChildExtensionReason() {
    I.click(this.fields.timetableForChildReason);
  },
};
