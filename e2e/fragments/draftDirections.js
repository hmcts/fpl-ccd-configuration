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

  enterDate(party, direction, index = 0) {
    I.fillField(this.fields(party, index).direction.dueDate.day, direction.dueDate.day);
    I.fillField(this.fields(party, index).direction.dueDate.month, direction.dueDate.month);
    I.fillField(this.fields(party, index).direction.dueDate.year, direction.dueDate.year);
    I.fillField(this.fields(party, index).direction.dueDate.hour, direction.dueDate.hour);
    I.fillField(this.fields(party, index).direction.dueDate.minute, direction.dueDate.minute);
    I.fillField(this.fields(party, index).direction.dueDate.second, direction.dueDate.second);
  },

  enterTitleAndDescription(party, direction, index = 0) {
    I.fillField(this.fields(party, index).direction.title, direction.title);
    I.fillField(this.fields(party, index).direction.description, direction.description);
  },
};
