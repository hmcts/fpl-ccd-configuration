const { I } = inject();

module.exports = {
  fields: function (party, index) {
    return {
      direction: {
        title: `#${party}_${index}_directionType`,
        description: `#${party}_${index}_directionText`,
        dueDate: {
          day: `#${party}_${index}_dateToBeCompletedBy-day`,
          month: `#${party}_${index}_dateToBeCompletedBy-month`,
          year: `#${party}_${index}_dateToBeCompletedBy-year`,
          hour: `#${party}_${index}_dateToBeCompletedBy-hour`,
          minute: `#${party}_${index}_dateToBeCompletedBy-minute`,
          second: `#${party}_${index}_dateToBeCompletedBy-second`,
        },
      },
    };
  },

  enterDate(party, dueDate, index = 0) {
    I.fillField(this.fields(party, index).direction.dueDate.day, dueDate.day);
    I.fillField(this.fields(party, index).direction.dueDate.month, dueDate.month);
    I.fillField(this.fields(party, index).direction.dueDate.year, dueDate.year);
    I.fillField(this.fields(party, index).direction.dueDate.hour, dueDate.hour);
    I.fillField(this.fields(party, index).direction.dueDate.minute, dueDate.minute);
    I.fillField(this.fields(party, index).direction.dueDate.second, dueDate.second);
  },

  enterTitleAndDescription(party, title = "", description = "", index = 0) {
    I.fillField(this.fields(party, index).direction.title, title);
    I.fillField(this.fields(party, index).direction.description, description);
  },
};
