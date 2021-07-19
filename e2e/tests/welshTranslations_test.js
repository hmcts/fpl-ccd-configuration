const config = require('../config.js');
const caseData = require('../fixtures/caseData/caseWithAllTypesOfOrders.json');
const caseView = require('../pages/caseView.page.js');
const closedCaseData = {
  state: 'CLOSED',
  caseData: caseData.caseData,
};

// most file names are overridden to the below values in api_helper
const orders = {
  generated: {
    name: 'C32 - Care order - 7 July 2021',
    file: 'C32 - Care order.pdf',
    translationFile: 'C32 - Care order-Welsh.pdf',
    tabName: caseView.tabs.orders,
    tabObjectName: 'Order 4',
  },
  standardDirectionOrder: {
    name: 'Gatekeeping order - 4 July 2021',
    file: 'sdo.pdf',
    translationFile: 'sdo-Welsh.pdf',
    tabName: caseView.tabs.orders,
    tabObjectName: 'Gatekeeping order',
  },
  urgentHearingOrder: {
    name: 'Urgent hearing order - 3 July 2021',
    file: 'uho.pdf',
    translationFile: 'uho-Welsh.pdf',
    tabName: caseView.tabs.orders,
    tabObjectName: 'Gatekeeping order - urgent hearing order',
  },
  caseManagementOrder: {
    name: 'Sealed case management order issued on 6 July 2021',
    file: 'mockFile.pdf',
    translationFile: 'mockFile-Welsh.pdf',
    tabName: caseView.tabs.orders,
    tabObjectName: 'Sealed Case Management Order 1',
  },
  noticeOfProceedingsC6: {
    name: 'Notice of proceedings (C6)',
    file: 'blah_c6.pdf',
    translationFile: 'blah_c6-Welsh.pdf',
    tabName: caseView.tabs.hearings,
    tabObjectName: 'Notice of proceedings 1',
  },
  noticeOfProceedingsC6A: {
    name: 'Notice of proceedings (C6A)',
    file: 'blah_c6a.pdf',
    translationFile: 'blah_c6a-Welsh.pdf',
    tabName: caseView.tabs.hearings,
    tabObjectName: 'Notice of proceedings 2',
  },
};

let caseId;

Feature('HMCTS Admin upload translations');

async function setupScenario(I, data = caseData) {
  if (!caseId || 'CLOSED' === data.state) {
    caseId = await I.submitNewCaseWithData(data);
  }
  await I.navigateToCaseDetailsAs(config.hmctsAdminUser, caseId);
}

Scenario('Upload translation for generated order', async ({ I, caseViewPage, uploadWelshTranslationsPage }) => {
  await setupScenario(I);
  await translateOrder(I, caseViewPage, uploadWelshTranslationsPage, orders.generated);
  assertTranslation(I, caseViewPage, orders.generated);
});

Scenario('Upload translation for standard directions order', async ({ I, caseViewPage, uploadWelshTranslationsPage }) => {
  await setupScenario(I);
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

Scenario('Upload translation for urgent hearing order', async ({ I, caseViewPage, uploadWelshTranslationsPage }) => {
  await setupScenario(I);
  await translateOrder(I, caseViewPage, uploadWelshTranslationsPage, orders.urgentHearingOrder);
  assertTranslation(I, caseViewPage, orders.urgentHearingOrder);
});

Scenario('Upload translation for case management order', async ({ I, caseViewPage, uploadWelshTranslationsPage }) => {
  await setupScenario(I);
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
  uploadWelshTranslationsPage.reviewItemToTranslate(item.file);
  uploadWelshTranslationsPage.uploadTranslatedItem(config.testPdfFile);
  await I.runAccessibilityTest();
  await I.completeEvent('Save and continue');
}

function assertTranslation(I, caseViewPage, order) {
  I.seeEventSubmissionConfirmation(config.administrationActions.uploadWelshTranslations);
  caseViewPage.selectTab(order.tabName);
  I.seeInTab([order.tabObjectName, 'Welsh translation'], `${order.translationFile}`);
}
