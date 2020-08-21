const config = require('../config');

const localAuthoritySendsAgreedCmo = async function (I, caseViewPage, uploadCaseManagementOrderEventPage, hearingDate, multiHearings) {
  await caseViewPage.goToNewActions(config.applicationActions.uploadCMO);

  if (multiHearings) {
    await uploadCaseManagementOrderEventPage.associateHearing(hearingDate);
    await I.retryUntilExists(() => I.click('Continue'), '#uploadedCaseManagementOrder');
  }

  await uploadCaseManagementOrderEventPage.uploadCaseManagementOrder(config.testNonEmptyWordFile);
  await I.completeEvent('Submit');
};

module.exports = {
  localAuthoritySendsAgreedCmo,
};
