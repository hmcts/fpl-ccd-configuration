const {I} = inject();
const supportingDocumentsFragment = require('../../fragments/supportingDocuments.js');

module.exports = {
  fields: {
    cmoDraftOrder: '#hearingOrderDraftKind-CMO',
    c21DraftOrder: '#hearingOrderDraftKind-C21',
    cmoUploadType: {
      id: '#cmoUploadType',
      options: {
        agreed: 'AGREED',
        draft: 'DRAFT',
      },
    },
    pastHearingDropdown: '#pastHearingsForCMO',
    futureHearingDropdown: '#futureHearingsForCMO',
    hearingsForHearingOrderDrafts: '#hearingsForHearingOrderDrafts',
    uploadCMO: {
      main: '#uploadedCaseManagementOrder',
      replacement: '#replacementCMO',
    },
    supportingDocuments: {
      id: '#cmoSupportingDocs',
      fields: index => supportingDocumentsFragment.supportingDocuments(index, 'cmoSupportingDocs'),
    },
    c21Documents: {
      fields (index) {
        return {
          name: `#currentHearingOrderDrafts_${index}_title`,
          document: `#currentHearingOrderDrafts_${index}_order`,
        };
      },
    },
  },

  selectDraftCMO() {
    I.click(this.fields.cmoUploadType.id + '-' + this.fields.cmoUploadType.options.draft);
  },

  selectAgreedCMO() {
    I.click(this.fields.cmoUploadType.id + '-' + this.fields.cmoUploadType.options.agreed);
  },

  selectPastHearing(hearing) {
    I.selectOption(this.fields.pastHearingDropdown, hearing);
  },

  selectFutureHearing(hearing) {
    I.selectOption(this.fields.futureHearingDropdown, hearing);
  },

  selectDraftHearing(hearing='No hearing') {
    I.selectOption(this.fields.hearingsForHearingOrderDrafts, hearing);
  },

  uploadCaseManagementOrder(file) {
    I.attachFile(this.fields.uploadCMO.main, file);
  },

  uploadReplacementCaseManagementOrder(file) {
    I.attachFile(this.fields.uploadCMO.replacement, file);
  },

  checkCMOInfo(hearing, previousFileName = undefined) {
    I.see(hearing);
    if (previousFileName) {
      I.see(previousFileName);
    }
  },

  reviewInfo(fileName, judge) {
    I.see(fileName);
    I.see(judge);
  },

  async attachSupportingDocs({name, notes, file}) {
    await I.addAnotherElementToCollection('Case summary or supporting documents');
    const fields = this.fields.supportingDocuments.fields(await I.getActiveElementIndex());
    I.fillField(fields.name, name);
    I.fillField(fields.notes, notes);
    I.attachFile(fields.document, file);
  },

  async attachC21({name, file, orderNumber=1}) {
    const numberOfElements = await I.grabNumberOfVisibleElements('.collection-title');

    for (let i = 0; i < orderNumber - numberOfElements; i++) {
      await I.addAnotherElementToCollection();
    }

    const fields = this.fields.c21Documents.fields(orderNumber - 1);
    I.fillField(fields.name, name);
    I.attachFile(fields.document, file);
    await I.runAccessibilityTest();
  },
};
