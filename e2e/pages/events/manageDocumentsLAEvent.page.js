const {I} = inject();
const c2SupportingDocuments = require('../../fixtures/c2SupportingDocuments.js');
const supportingDocumentsFragment = require('../../fragments/supportingDocuments.js');

module.exports = {
  fields: {
    documentType: {
      furtherEvidence: '#manageDocumentLA_type-FURTHER_EVIDENCE_DOCUMENTS',
      correspondence: '#manageDocumentLA_type-CORRESPONDENCE',
      additionalApplications: '#manageDocumentLA_type-ADDITIONAL_APPLICATIONS_DOCUMENTS',
      courtBundle: '#manageDocumentLA_type-COURT_BUNDLE',
    },
    relatedToHearing: {
      yes: '#manageDocumentLA_relatedToHearing-Yes',
      no: '#manageDocumentLA_relatedToHearing-No',
    },
    subtype: {
      applicationDocuments: '#manageDocumentSubtypeListLA-APPLICATION_DOCUMENTS',
      otherDocuments: '#manageDocumentSubtypeListLA-OTHER',
    },
    hearingList: '#manageDocumentsHearingList',
    courtBundleHearingList: '#courtBundleHearingList',
    courtBundleDocument: '#manageDocumentsCourtBundle_document',
    c2DocumentsList: '#manageDocumentsSupportingC2List',
    supportingDocumentsForC2: supportingDocumentsFragment.supportingDocuments(0, 'temporaryC2Document_supportingEvidenceBundle'),
    supportingDocumentsCollectionId: '#supportingEvidenceDocumentsTemp',
    supportingDocuments: index => supportingDocumentsFragment.supportingDocuments(index, 'supportingEvidenceDocumentsTemp'),
  },

  async selectFurtherEvidence() {
    I.click(this.fields.documentType.furtherEvidence);
  },

  async selectAnyOtherDocument() {
    I.click(this.fields.subtype.otherDocuments);
  },

  async selectCorrespondence() {
    I.click(this.fields.documentType.correspondence);
  },

  async selectAdditionalApplicationsSupportingDocuments() {
    I.click(this.fields.documentType.additionalApplications);
  },

  async selectCourtBundle() {
    I.click(this.fields.documentType.courtBundle);
  },

  async selectC2() {
    I.click(this.fields.documentType.additionalApplications);
    const dropdownLabel = await I.grabTextFrom(`${this.fields.c2DocumentsList} option:nth-child(2)`);
    I.waitForElement(this.fields.c2DocumentsList);
    I.selectOption(this.fields.c2DocumentsList, dropdownLabel);
  },

  async selectOtherApplication() {
    I.click(this.fields.documentType.additionalApplications);
    const dropdownLabel = await I.grabTextFrom(`${this.fields.c2DocumentsList} option:nth-child(2)`);
    I.waitForElement(this.fields.c2DocumentsList);
    I.selectOption(this.fields.c2DocumentsList, dropdownLabel);
  },

  async uploadC2SupportingDocument() {
    I.fillField(this.fields.supportingDocuments.name, c2SupportingDocuments.name);
    I.fillField(this.fields.supportingDocuments.notes, c2SupportingDocuments.notes);
    I.fillDateAndTime(c2SupportingDocuments.date, this.fields.supportingDocuments.dateAndTime);
    I.attachFile(this.fields.supportingDocuments.document, c2SupportingDocuments.document);
  },

  async selectCourtBundleHearing(hearingDate) {
    I.waitForElement(this.fields.courtBundleHearingList);
    I.selectOption(this.fields.courtBundleHearingList, `Case management hearing, ${hearingDate}`);
  },

  async attachCourtBundle(document) {
    I.attachFile(this.fields.courtBundleDocument, document);
  },

  async selectFurtherEvidenceIsRelatedToHearing() {
    I.waitForElement(this.fields.relatedToHearing.yes);
    I.click(this.fields.relatedToHearing.yes);
  },

  async selectHearing(hearingDate) {
    I.waitForElement(this.fields.hearingList);
    I.selectOption(this.fields.hearingList, `Case management hearing, ${hearingDate}`);
  },

  async selectC2FromDropdown() {
    const dropdownLabel = await I.grabTextFrom(`${this.fields.c2DocumentsList} option:nth-child(2)`);
    I.waitForElement(this.fields.c2DocumentsList);
    await I.runAccessibilityTest();
    I.selectOption(this.fields.c2DocumentsList, dropdownLabel);
  },

  async enterDocumentName(documentName) {
    const elementIndex = await this.getActiveElementIndex();
    I.fillField(this.fields.supportingDocuments(elementIndex).name, documentName);
  },

  async enterDocumentNotes(notes) {
    const elementIndex = await this.getActiveElementIndex();
    I.fillField(this.fields.supportingDocuments(elementIndex).notes, notes);
  },

  async enterDateAndTimeReceived(dateAndTime) {
    const elementIndex = await this.getActiveElementIndex();
    I.fillDateAndTime(dateAndTime, this.fields.supportingDocuments(elementIndex).dateAndTime);
  },

  async uploadDocument(document) {
    const elementIndex = await this.getActiveElementIndex();
    I.attachFile(this.fields.supportingDocuments(elementIndex).document, document);
  },

  async selectFurtherEvidenceType(type) {
    const elementIndex = await this.getActiveElementIndex();
    switch (type) {
      case 'Expert reports':
        I.checkOption(this.fields.supportingDocuments(elementIndex).type.expert);
        break;
      case 'Other reports':
        I.checkOption(this.fields.supportingDocuments(elementIndex).type.other);
        break;
      default:
        throw new Error(`Unsupported further evidence type ${type}`);
    }
  },

  async selectConfidential() {
    const elementIndex = await this.getActiveElementIndex();
    I.click(this.fields.supportingDocuments(elementIndex).confidential);
  },

  async uploadSupportingEvidenceDocument(supportingEvidenceDocument, selectEvidenceType) {
    await this.enterDocumentName(supportingEvidenceDocument.name);
    await this.enterDocumentNotes(supportingEvidenceDocument.notes);
    await this.uploadDocument(supportingEvidenceDocument.document);
    if(selectEvidenceType) {
      await this.selectFurtherEvidenceType(supportingEvidenceDocument.type);
    }
  },

  async uploadConfidentialSupportingEvidenceDocument(supportingEvidenceDocument, selectEvidenceType = false) {
    await this.uploadSupportingEvidenceDocument(supportingEvidenceDocument, selectEvidenceType);
    await this.selectConfidential();
  },

  async uploadCorrespondenceDocuments(supportingEvidenceDocument) {
    await this.enterDocumentName(supportingEvidenceDocument.name);
    await this.enterDocumentNotes(supportingEvidenceDocument.notes);
    await this.enterDateAndTimeReceived(supportingEvidenceDocument.date);
    await this.uploadDocument(supportingEvidenceDocument.document);
    await I.runAccessibilityTest();
  },

  async getActiveElementIndex() {
    return await I.grabNumberOfVisibleElements('//button[text()="Remove"]') - 1;
  },
};
