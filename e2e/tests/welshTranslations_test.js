const config = require('../config.js');
const dateFormat = require('dateformat');
const draftOrdersHelper = require('../helpers/cmo_helper');
const caseData = require('../fixtures/caseData/caseWithAllTypesOfOrders.json');
const caseDataGatekeeping = require('../fixtures/caseData/gatekeepingFullDetails.json');
const caseDataCaseManagement = require('../fixtures/caseData/prepareForHearing.json');
const caseView = require('../pages/caseView.page.js');
const closedCaseData = {
  state: 'CLOSED',
  caseData: {
    ...caseData.caseData,
    languageRequirement: 'Yes',
  },
};

const caseDataGatekeepingWithLanguage = {
  state: 'GATEKEEPING',
  caseData: {
    ...caseDataGatekeeping.caseData,
    languageRequirement: 'Yes',
  },
};

const caseDataCaseManagementWithLanguage = {
  state: 'PREPARE_FOR_HEARING',
  caseData: {
    ...caseDataCaseManagement.caseData,
    languageRequirement: 'Yes',
  },
};

// most file names are overridden to the below values in api_helper
const orders = {
  draftOrder: {
    title: 'draft order 1',
    file: config.testWordFile,
    originalFile: 'mockFile.pdf',
    translationFile: 'mockFile-Welsh.pdf',
    name: `draft order 1 - ${dateFormat('d mmmm yyyy')}`,
    translation: 'ENGLISH_TO_WELSH',
    tabName: caseView.tabs.orders,
    tabObjectName: 'Order 1',
  },
  generated: {
    name: 'C32 - Care order - 7 July 2021',
    originalFile: 'C32 - Care order.pdf',
    translationFile: 'C32 - Care order-Welsh.pdf',
    tabName: caseView.tabs.orders,
    tabObjectName: 'Order 4',
  },
  uploadedOrder: {
    name: `Order F789s - ${dateFormat('d mmmm yyyy')}`,
    originalFile: 'other_order.pdf',
    translationFile: 'other_order-Welsh.pdf',
    tabName: caseView.tabs.orders,
    tabObjectName: 'Order 1',
  },
  standardDirectionOrder: {
    name: `Gatekeeping order - ${dateFormat('d mmmm yyyy')}`,
    originalFile: 'sdo.pdf',
    translationFile: 'sdo-Welsh.pdf',
    tabName: caseView.tabs.orders,
    tabObjectName: 'Gatekeeping order',
  },
  urgentHearingOrder: {
    name: `Urgent hearing order - ${dateFormat('d mmmm yyyy')}`,
    originalFile: 'uho.pdf',
    translationFile: 'uho-Welsh.pdf',
    tabName: caseView.tabs.orders,
    tabObjectName: 'Gatekeeping order - urgent hearing order',
  },
  caseManagementOrder: {
    name: `Sealed case management order issued on ${dateFormat('d mmmm yyyy')}`,
    originalFile: 'mockFile.pdf',
    translationFile: 'mockFile-Welsh.pdf',
    tabName: caseView.tabs.orders,
    tabObjectName: 'Sealed Case Management Order 1',
  },
  noticeOfProceedingsC6: {
    name: 'Notice of proceedings (C6)',
    originalFile: 'blah_c6.pdf',
    translationFile: 'blah_c6-Welsh.pdf',
    tabName: caseView.tabs.hearings,
    tabObjectName: 'Notice of proceedings 1',
  },
  noticeOfProceedingsC6A: {
    name: 'Notice of proceedings (C6A)',
    originalFile: 'blah_c6a.pdf',
    translationFile: 'blah_c6a-Welsh.pdf',
    tabName: caseView.tabs.hearings,
    tabObjectName: 'Notice of proceedings 2',
  },
};

let caseId;

Feature('HMCTS Request and upload translations');

async function setupScenario(I, data = caseData) {
  if (!caseId || 'CLOSED' === data.state) {
    caseId = await I.submitNewCaseWithData(data);
  }
  await I.navigateToCaseDetailsAs(config.hmctsAdminUser, caseId);
}

Scenario('Can upload translation only if language requirement is set', async ({ I, caseViewPage, enterLanguageRequirementsEventPage }) => {
  await setupScenario(I);
  await caseViewPage.checkActionsAreNotAvailable([config.administrationActions.uploadWelshTranslations]);

  await caseViewPage.goToNewActions(config.administrationActions.languageRequirement);
  await enterLanguageRequirementsEventPage.disableLanguageRequirement();
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.languageRequirement);
  await caseViewPage.checkActionsAreNotAvailable([config.administrationActions.uploadWelshTranslations]);

  await caseViewPage.goToNewActions(config.administrationActions.languageRequirement);
  await enterLanguageRequirementsEventPage.enterLanguageRequirement();
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.languageRequirement);
  await caseViewPage.checkActionsAreAvailable([config.administrationActions.uploadWelshTranslations]);
});

Scenario('Upload translation for generated order', async ({ I, caseViewPage, uploadWelshTranslationsPage }) => {
  await setupScenario(I);
  await translateOrder(I, caseViewPage, uploadWelshTranslationsPage, orders.generated);
  assertTranslation(I, caseViewPage, orders.generated);
});

Scenario('Request and upload translation for uploaded order', async ({ I, caseViewPage, uploadWelshTranslationsPage, manageOrdersEventPage }) => {
  let caseId = await I.submitNewCaseWithData(caseDataGatekeepingWithLanguage);
  await I.navigateToCaseDetailsAs(config.hmctsAdminUser, caseId);
  await caseViewPage.goToNewActions(config.administrationActions.manageOrders);
  await manageOrdersEventPage.selectOperation(manageOrdersEventPage.operations.options.upload);
  await I.goToNextPage();
  await manageOrdersEventPage.selectUploadOrder(manageOrdersEventPage.orders.options.other);
  manageOrdersEventPage.specifyOtherOrderTitle('Order F789s');
  await I.goToNextPage();
  await I.goToNextPage();
  await manageOrdersEventPage.selectChildren(manageOrdersEventPage.section3.allChildren.options.select, [0]);
  await I.goToNextPage();
  await manageOrdersEventPage.uploadManualOrder(config.testPdfFile);
  manageOrdersEventPage.selectManualOrderNeedSealing(manageOrdersEventPage.section4.manualOrderNeedSealing.options.yes);
  await manageOrdersEventPage.selectIsFinalOrder();
  await manageOrdersEventPage.selectTranslationRequirement(manageOrdersEventPage.section4.translationRequirement.englishToWelsh);
  await I.goToNextPage();
  await manageOrdersEventPage.checkPreview();
  await manageOrdersEventPage.selectCloseCase();
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.manageOrders);
  assertSentToTranslation(I, caseViewPage, orders.uploadedOrder);
  await translateOrder(I, caseViewPage, uploadWelshTranslationsPage, orders.uploadedOrder);
  assertTranslation(I, caseViewPage, orders.uploadedOrder);
});

Scenario('Request and upload translation for standard directions order', async ({ I, caseViewPage, uploadWelshTranslationsPage, draftStandardDirectionsEventPage }) => {
  let caseId = await I.submitNewCaseWithData(caseDataGatekeepingWithLanguage);
  await I.navigateToCaseDetailsAs(config.gateKeeperUser, caseId);
  await caseViewPage.goToNewActions(config.administrationActions.addGatekeepingOrder);
  I.click('Upload a prepared gatekeeping order');
  await I.goToNextPage();

  await draftStandardDirectionsEventPage.uploadPreparedSDO(config.testWordFileSdo);
  I.see('Case assigned to: Her Honour Judge Moley');
  I.click('Yes');
  await I.goToNextPage();

  await draftStandardDirectionsEventPage.markAsFinal();
  I.see('Is translation needed?');
  draftStandardDirectionsEventPage.selectTranslationRequirement(draftStandardDirectionsEventPage.fields.upload.translationRequirement.englishToWelsh);
  await I.completeEvent('Save and continue');

  I.seeEventSubmissionConfirmation(config.administrationActions.addGatekeepingOrder);
  assertSentToTranslation(I, caseViewPage, orders.standardDirectionOrder);
  await I.navigateToCaseDetailsAs(config.hmctsAdminUser, caseId);
  await translateOrder(I, caseViewPage, uploadWelshTranslationsPage, orders.standardDirectionOrder);
  assertTranslation(I, caseViewPage, orders.standardDirectionOrder);
});

Scenario('Upload translation for notice of proceedings (C36)', async ({ I, caseViewPage, uploadWelshTranslationsPage }) => {
  await setupScenario(I);
  await translateOrder(I, caseViewPage, uploadWelshTranslationsPage, orders.noticeOfProceedingsC6);
  assertTranslation(I, caseViewPage, orders.noticeOfProceedingsC6);
});

Scenario('Upload translation for notice of proceedings - others (C36A)', async ({ I, caseViewPage, uploadWelshTranslationsPage }) => {
  await setupScenario(I);
  await translateOrder(I, caseViewPage, uploadWelshTranslationsPage, orders.noticeOfProceedingsC6A);
  assertTranslation(I, caseViewPage, orders.noticeOfProceedingsC6A);
});

Scenario('Request and upload translation for urgent hearing order', async ({ I, caseViewPage, uploadWelshTranslationsPage, draftStandardDirectionsEventPage }) => {
  let caseId = await I.submitNewCaseWithData(caseDataGatekeepingWithLanguage);
  await I.navigateToCaseDetailsAs(config.gateKeeperUser, caseId);
  const allocationDecisionFields = draftStandardDirectionsEventPage.fields.allocationDecision;
  await caseViewPage.goToNewActions(config.administrationActions.addGatekeepingOrder);
  I.click('Upload an urgent hearing order');
  await I.goToNextPage();

  await draftStandardDirectionsEventPage.makeAllocationDecision(allocationDecisionFields.judgeLevelConfirmation.no, allocationDecisionFields.allocationLevel.magistrate, 'some reason');
  await I.goToNextPage();
  await draftStandardDirectionsEventPage.uploadUrgentHearingOrder(config.testPdfFileUho);
  I.see('Is translation needed?');
  draftStandardDirectionsEventPage.selectTranslationRequirement(draftStandardDirectionsEventPage.fields.urgent.translationRequirement.englishToWelsh);
  await I.completeEvent('Save and continue');

  I.seeEventSubmissionConfirmation(config.administrationActions.addGatekeepingOrder);
  assertSentToTranslation(I, caseViewPage, orders.urgentHearingOrder);

  await I.navigateToCaseDetailsAs(config.hmctsAdminUser, caseId);
  await translateOrder(I, caseViewPage, uploadWelshTranslationsPage, orders.urgentHearingOrder);
  assertTranslation(I, caseViewPage, orders.urgentHearingOrder);
});

Scenario('Request and upload translation for case management order', async ({ I, caseViewPage, uploadWelshTranslationsPage, uploadCaseManagementOrderEventPage, reviewAgreedCaseManagementOrderEventPage }) => {
  const hearing1 = 'Case management hearing, 1 January 2020';

  let caseId = await I.submitNewCaseWithData(caseDataCaseManagementWithLanguage);
  // Local authority upload and request translation
  await I.navigateToCaseDetailsAs(config.swanseaLocalAuthorityUserOne, caseId);
  await draftOrdersHelper.localAuthoritySendsAgreedCmo(I, caseViewPage, uploadCaseManagementOrderEventPage, hearing1,null, orders.draftOrder,'ENGLISH_TO_WELSH');

  // Judge approves and send to translation
  await I.navigateToCaseDetailsAs(config.judicaryUser, caseId);
  await caseViewPage.goToNewActions(config.applicationActions.approveOrders);
  I.see('mockFile.docx');
  await reviewAgreedCaseManagementOrderEventPage.selectSealCmo();
  reviewAgreedCaseManagementOrderEventPage.selectSealC21(1);
  await I.goToNextPage();
  reviewAgreedCaseManagementOrderEventPage.selectAllOthers();
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.applicationActions.approveOrders);

  assertSentToTranslation(I, caseViewPage, orders.draftOrder);
  assertSentToTranslation(I, caseViewPage, orders.caseManagementOrder);

  // Upload the translated orders
  await I.navigateToCaseDetailsAs(config.hmctsAdminUser, caseId);

  await translateOrder(I, caseViewPage, uploadWelshTranslationsPage, orders.draftOrder);
  assertTranslation(I, caseViewPage, orders.draftOrder);

  await translateOrder(I, caseViewPage, uploadWelshTranslationsPage, orders.caseManagementOrder);
  assertTranslation(I, caseViewPage, orders.caseManagementOrder);
});

Scenario('Upload translation for generated order (closed)', async ({ I, caseViewPage, uploadWelshTranslationsPage }) => {
  await setupScenario(I, closedCaseData);
  await translateOrder(I, caseViewPage, uploadWelshTranslationsPage, orders.generated);
  assertTranslation(I, caseViewPage, orders.generated);
});

async function translateOrder(I, caseViewPage, uploadWelshTranslationsPage, item) {
  await caseViewPage.goToNewActions(config.administrationActions.uploadWelshTranslations);
  uploadWelshTranslationsPage.selectItemToTranslate(item.name);
  await I.goToNextPage();
  uploadWelshTranslationsPage.reviewItemToTranslate(item.originalFile);
  uploadWelshTranslationsPage.uploadTranslatedItem(config.testPdfFile);
  await I.runAccessibilityTest();
  await I.completeEvent('Save and continue');
}

function assertSentToTranslation(I, caseViewPage, order) {
  caseViewPage.selectTab(order.tabName);
  I.seeLabelInTab([order.tabObjectName, 'Sent for translation']);
  I.dontSeeInTab([order.tabObjectName, 'Translated document'], order.translationFile);
}

function assertTranslation(I, caseViewPage, order) {
  I.seeEventSubmissionConfirmation(config.administrationActions.uploadWelshTranslations);
  caseViewPage.selectTab(order.tabName);
  I.seeInTab([order.tabObjectName, 'Translated document'], order.translationFile);
  I.dontSeeLabelInTab([order.tabObjectName, 'Sent for translation']);
}
