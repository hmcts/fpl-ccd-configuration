const { I } = inject();

module.exports = {
  fields: {
    date: '#closeCase_date',
    details: '#closeCase_details',
    radioGroup: {
      fullReason: {
        id: '#closeCase_fullReason',
        options: {
          finalOrder: 'Final order',
          refusal: 'Refusal',
          withdrawn: 'Withdrawn',
          noOrder: 'No order was made',
          deprivation: 'Deprivation of liberty',
          other: 'Other',
        },
      },
      partialReason: {
        id: '#closeCase_partialReason',
        options: {
          refusal: 'Refusal',
          withdrawn: 'Withdrawn',
          noOrder: 'No order was made',
          deprivation: 'Deprivation of liberty',
          other: 'Other',
        },
      },
    },
  },

  closeCase(date, fullReason, option, details) {
    this.addDate(date);

    if (fullReason) {
      this.selectFullReason(option);
    } else {
      this.selectPartialReason(option);
    }

    if (details) {
      this.addDetails(details);
    }
  },

  addDetails(details) {
    I.fillField(this.fields.details, details);
  },

  selectFullReason(option) {
    within(this.fields.radioGroup.fullReason.id, () => {
      I.click(locate('label').withText(option));
    });
  },

  selectPartialReason(option) {
    within(this.fields.radioGroup.partialReason.id, () => {
      I.click(locate('label').withText(option));
    });
  },

  addDate(date) {
    I.fillDate(date, this.fields.date);
  },
};
