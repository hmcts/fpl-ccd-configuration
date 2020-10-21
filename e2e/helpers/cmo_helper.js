const config = require('../config');

const localAuthoritySendsAgreedCmo = async (I, caseViewPage, uploadCMOEventPage, hearing, supportingDocs) => {
  await uploadCMO(I, caseViewPage, uploadCMOEventPage, hearing, supportingDocs, () => {
    uploadCMOEventPage.selectAgreedCMO();
    uploadCMOEventPage.selectPastHearing(hearing);
  });

  // await caseViewPage.goToNewActions(config.applicationActions.uploadCMO);
  // await I.waitFor(uploadCMOEventPage.fields.cmoUploadType.id);
  // uploadCMOEventPage.selectAgreedCMO();
  // uploadCMOEventPage.selectPastHearing(hearing);
  // await I.retryUntilExists(() => I.click('Continue'), '#uploadedCaseManagementOrder');
  // uploadCMOEventPage.checkCMOInfo(hearing);
  // await uploadCMOEventPage.uploadCaseManagementOrder(config.testWordFile);
  //
  // if (supportingDocs) {
  //   await uploadCMOEventPage.attachSupportingDocs(supportingDocs);
  // }
  //
  // await I.retryUntilExists(() => I.click('Continue'), 'ccd-read-document-field');
  // uploadCMOEventPage.checkCMOInfo('mockFile.docx', 'Her Honour Judge Reed');
  // await I.completeEvent('Submit');
  // I.seeEventSubmissionConfirmation(config.applicationActions.uploadCMO);
};

const localAuthorityUploadsDraftCmo = async (I, caseViewPage, uploadCMOEventPage, hearing, supportingDocs) => {
  await uploadCMO(I, caseViewPage, uploadCMOEventPage, hearing, supportingDocs, () => {
    uploadCMOEventPage.selectDraftCMO();
    uploadCMOEventPage.selectFutureHearing(hearing);
  });

  // await caseViewPage.goToNewActions(config.applicationActions.uploadCMO);
  // await I.waitFor(uploadCMOEventPage.fields.cmoUploadType.id);
  // uploadCMOEventPage.selectDraftCMO();
  // uploadCMOEventPage.selectFutureHearing(hearing);
  // await I.retryUntilExists(() => I.click('Continue'), '#uploadedCaseManagementOrder');
  // await uploadCMOEventPage.uploadCaseManagementOrder(config.testWordFile);
  //
  // if (supportingDocs) {
  //   await uploadCMOEventPage.attachSupportingDocs(supportingDocs);
  // }
  // await I.retryUntilExists(() => I.click('Continue'), 'ccd-read-document-field');
  // uploadCMOEventPage.checkCMOInfo('mockFile.docx', 'Her Honour Judge Reed');
  // await I.completeEvent('Submit');
  // I.seeEventSubmissionConfirmation(config.applicationActions.uploadCMO);
};

const judgeSendsReviewedCmoToAllParties = async (I, caseId, caseViewPage, uploadCaseManagementOrderEventPage, reviewAgreedCaseManagementOrderEventPage) => {
  await I.navigateToCaseDetailsAs(config.swanseaLocalAuthorityUserOne, caseId);
  await localAuthoritySendsAgreedCmo(I, caseViewPage, uploadCaseManagementOrderEventPage, '1 January 2020', true);
  I.seeEventSubmissionConfirmation(config.applicationActions.uploadCMO);
  await localAuthoritySendsAgreedCmo(I, caseViewPage, uploadCaseManagementOrderEventPage);
  I.seeEventSubmissionConfirmation(config.applicationActions.uploadCMO);
  await I.navigateToCaseDetailsAs(config.judicaryUser, caseId);
  await caseViewPage.goToNewActions(config.applicationActions.reviewAgreedCmo);
  reviewAgreedCaseManagementOrderEventPage.selectCMOToReview('1 January 2020');
  await I.retryUntilExists(() => I.click('Continue'), '#reviewCMODecision_decision');
  reviewAgreedCaseManagementOrderEventPage.selectSealCmo();
  await I.completeEvent('Save and continue', {summary: 'Summary', description: 'Description'});
  I.seeEventSubmissionConfirmation(config.applicationActions.reviewAgreedCmo);
};

const uploadCMO = async (I, caseViewPage, uploadCMOEventPage, hearing, supportingDocs, selectHearing) => {
  await caseViewPage.goToNewActions(config.applicationActions.uploadCMO);
  await I.waitForSelector(uploadCMOEventPage.fields.cmoUploadType.id);
  selectHearing();
  await I.retryUntilExists(() => I.click('Continue'), '#uploadedCaseManagementOrder');
  uploadCMOEventPage.checkCMOInfo(hearing);
  await uploadCMOEventPage.uploadCaseManagementOrder(config.testWordFile);
  if (supportingDocs) {
    await uploadCMOEventPage.attachSupportingDocs(supportingDocs);
  }
  await I.retryUntilExists(() => I.click('Continue'), 'ccd-read-document-field');
  uploadCMOEventPage.reviewInfo('mockFile.docx', 'Her Honour Judge Reed');
  await I.completeEvent('Submit');
  I.seeEventSubmissionConfirmation(config.applicationActions.uploadCMO);
};

module.exports = {
  localAuthoritySendsAgreedCmo,
  localAuthorityUploadsDraftCmo,
  judgeSendsReviewedCmoToAllParties,
};
