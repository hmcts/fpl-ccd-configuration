const {I} = inject();

const fields = {
  date: '#date',
  details: '#closeCase_details',
  fullReason: '#closeCase_fullReason',
  partialReason: '#closeCase_partialReason',
  reasons: {
    finalOrder: 'FINAL_ORDER',
    refusal: 'REFUSAL',
    withdrawn: 'WITHDRAWN',
    noOrder: 'NO_ORDER',
    deprivation: 'DEPRIVATION_OF_LIBERTY',
    other: 'OTHER',
  },
};

module.exports = {
  fields,
  async closeCase(date, reasonOption, details, fullReason = true) {
    await I.runAccessibilityTest();
    await addDate(date);
    selectReason(reasonOption, fullReason);
    if (details) {
      addDetails(details);
    }
  },
};

const selectReason = (reasonOption, fullReason) => {
  const fieldPrefix = fullReason ? fields.fullReason : fields.partialReason;
  const field = `${fieldPrefix}-${reasonOption}`;
  I.waitForVisible(field, 10);
  I.click(field);
};

const addDetails = (details) => {
  I.fillField(fields.details, details);
};

const addDate = async (date) => {
  await I.fillDate(date, fields.date);
};
