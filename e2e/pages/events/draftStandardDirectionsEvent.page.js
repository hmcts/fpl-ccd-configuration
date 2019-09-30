const {I} = inject();

module.exports = {
  fields: function (party, index) {
    return {
      direction: {
        title: `#${party}_${index}_type`,
        description: `#${party}_${index}_text`,
        dueDate: {
          day: `#${party}_${index}_completeBy-day`,
          month: `#${party}_${index}_completeBy-month`,
          year: `#${party}_${index}_completeBy-year`,
          hour: `#${party}_${index}_completeBy-hour`,
          minute: `#${party}_${index}_completeBy-minute`,
          second: `#${party}_${index}_completeBy-second`,
        },
      },
    };
  },

  async enterDate(party, index, direction) {
    I.fillField(this.fields(party, index).direction.dueDate.day, direction.dueDate.day);
    I.fillField(this.fields(party, index).direction.dueDate.month, direction.dueDate.month);
    I.fillField(this.fields(party, index).direction.dueDate.year, direction.dueDate.year);
    I.fillField(this.fields(party, index).direction.dueDate.hour, direction.dueDate.hour);
    I.fillField(this.fields(party, index).direction.dueDate.minute, direction.dueDate.minute);
    I.fillField(this.fields(party, index).direction.dueDate.second, direction.dueDate.second);
  },

  async enterDatesForDirections(direction) {
    await this.enterDate('allParties', 0, direction);
    await I.retryUntilExists(() => I.click('Continue'), '#localAuthorityDirections');
    await this.enterDate('localAuthorityDirections', 0, direction);
    await I.retryUntilExists(() => I.click('Continue'), '#parentsAndRespondentsDirections');
    await this.enterDate('parentsAndRespondentsDirections', 0, direction);
    await I.retryUntilExists(() => I.click('Continue'), '#cafcassDirections');
    await this.enterDate('cafcassDirections', 0, direction);
    await I.retryUntilExists(() => I.click('Continue'), '#otherPartiesDirections');
    await this.enterDate('otherPartiesDirections', 0, direction);
    await I.retryUntilExists(() => I.click('Continue'), '#courtDirections');
    await this.enterDate('courtDirections', 0, direction);
  },
};
