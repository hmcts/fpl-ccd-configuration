const config = require('../config');

const localAuthoritySendsAgreedCmo = async (I, caseViewPage, uploadCMOEventPage, hearing, supportingDocs, c21s, cmoTranslation=null) => {
  await uploadCMO(I, caseViewPage, uploadCMOEventPage, hearing, supportingDocs, c21s, cmoTranslation, () => {
    uploadCMOEventPage.selectAgreedCMO();
    uploadCMOEventPage.selectPastHearing(hearing);
  });
};

const localAuthorityUploadsDraftCmo = async (I, caseViewPage, uploadCMOEventPage, hearing, supportingDocs, c21s, cmoTranslation=null) => {
  await uploadCMO(I, caseViewPage, uploadCMOEventPage, hearing, supportingDocs, c21s, cmoTranslation, () => {
    uploadCMOEventPage.selectDraftCMO();
    uploadCMOEventPage.selectFutureHearing(hearing);
  });
};

const localAuthorityUploadsC21 = async (I, caseViewPage, uploadCMOEventPage, c21s, hearing) => {
  await uploadC21(I, caseViewPage, uploadCMOEventPage, c21s, hearing);
};

const judgeSendsReviewedCmoToAllParties = async (I, caseId, caseViewPage, uploadCaseManagementOrderEventPage, reviewAgreedCaseManagementOrderEventPage) => {
  await I.navigateToCaseDetailsAs(config.swanseaLocalAuthorityUserOne, caseId);
  await localAuthoritySendsAgreedCmo(I, caseViewPage, uploadCaseManagementOrderEventPage, 'Case management hearing, 1 January 2020');
  await localAuthoritySendsAgreedCmo(I, caseViewPage, uploadCaseManagementOrderEventPage, 'Final hearing, 1 March 2020');
  await I.navigateToCaseDetailsAs(config.judicaryUser, caseId);
  await caseViewPage.goToNewActions(config.applicationActions.approveOrders);
  await reviewAgreedCaseManagementOrderEventPage.selectCMOToReview('Case management hearing, 1 January 2020');
  await I.goToNextPage();
  await reviewAgreedCaseManagementOrderEventPage.selectSealCmo();
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.applicationActions.approveOrders);
};

const uploadCMO = async (I, caseViewPage, uploadCMOEventPage, hearing, supportingDocs, c21s, cmoTranslation=null, selectHearing) => {
  await caseViewPage.goToNewActions(config.applicationActions.uploadCMO);

  I.waitForElement(uploadCMOEventPage.fields.cmoDraftOrder);
  I.click(uploadCMOEventPage.fields.cmoDraftOrder);
  if(c21s) {
    I.click(uploadCMOEventPage.fields.c21DraftOrder);
  }
  await I.goToNextPage();

  I.waitForElement(uploadCMOEventPage.fields.cmoUploadType.id);
  selectHearing();
  await I.goToNextPage();

  uploadCMOEventPage.checkCMOInfo(hearing);
  uploadCMOEventPage.uploadCaseManagementOrder(config.testWordFile);
  if (supportingDocs) {
    await uploadCMOEventPage.attachSupportingDocs(supportingDocs);
  }
  await I.goToNextPage();
  if (c21s) {
    await uploadCMOEventPage.attachC21({name: c21s.title, file: c21s.file, orderNumber: c21s.number});
    await I.goToNextPage();
  }
  uploadCMOEventPage.reviewInfo('mockFile.docx', 'Her Honour Judge Reed');
  if (cmoTranslation) {
    uploadCMOEventPage.requestTranslationForCmo(cmoTranslation);
  }
  if (c21s && c21s.translation) {
    uploadCMOEventPage.requestTranslationForC21(c21s.translation);
  }
  await I.completeEvent('Submit');
};

const uploadC21 = async (I, caseViewPage, uploadCMOEventPage, c21s, hearing) => {
  await caseViewPage.goToNewActions(config.applicationActions.uploadCMO);

  I.waitForElement(uploadCMOEventPage.fields.cmoDraftOrder);
  I.click(uploadCMOEventPage.fields.c21DraftOrder);

  await I.goToNextPage();

  uploadCMOEventPage.selectDraftHearing(hearing);
  await I.goToNextPage();
  await uploadCMOEventPage.attachC21({name: c21s.title, file: c21s.file, orderNumber:  c21s.number});
  await I.goToNextPage();
  uploadCMOEventPage.reviewInfo('mockFile.docx', hearing ? 'Her Honour Judge Reed' : null);
  await I.completeEvent('Submit');
  I.seeEventSubmissionConfirmation(config.applicationActions.uploadCMO);
};

module.exports = {
  localAuthoritySendsAgreedCmo,
  localAuthorityUploadsDraftCmo,
  localAuthorityUploadsC21,
  judgeSendsReviewedCmoToAllParties,
};
