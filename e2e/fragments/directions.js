const { I } = inject();

module.exports = {
  fields: function (party, index) {
    return {
      direction: {
        title: `#${party}_${index}_directionType`,
        description: `#${party}_${index}_directionText`,
        dueDate: `#${party}_${index}_dateToBeCompletedBy`,
      },
    };
  },

  async enterDate2(party, dueDate, index = 0) {
    await I.fillDateAndTime(dueDate, this.fields(party, index).direction.dueDate);
  },

  async enterDate(party, dueDate, index = 0) {

    const dateSelector = `(//*[contains(@class, "collection-title")])[${ index + 1 }]/parent::div//*[contains(@class,"form-date")]`;
    await I.fillDateAndTime(dueDate, dateSelector);
  },

  enterTitleAndDescription(party, title = '', description = '', index = 0) {
    I.fillField(this.fields(party, index).direction.title, title);
    I.fillField(this.fields(party, index).direction.description, description);
  },
};

