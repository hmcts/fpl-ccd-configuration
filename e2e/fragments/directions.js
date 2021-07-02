const { I } = inject();

module.exports = {
  fields: function (party, index) {
    return {
      direction: {
        title: `#${party}_${index}_directionType`,
        description: `#${party}_${index}_directionText`,
        date: `(//*[contains(@class, "collection-title")])[${ index + 1 }]/parent::div//*[contains(@class,"form-date")]`,
      },
    };
  },

  async enterDate(dueDate, index = 0) {
    await I.fillDateAndTime(dueDate, this.fields(null, index).direction.date);
  },

  enterTitleAndDescription(party, title = '', description = '', index = 0) {
    I.fillField(this.fields(party, index).direction.title, title);
    I.fillField(this.fields(party, index).direction.description, description);
  },
};
