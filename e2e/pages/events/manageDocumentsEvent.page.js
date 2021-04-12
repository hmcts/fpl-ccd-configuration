const { I } = inject();
const supportingDocumentsFragment = require('../../fragments/supportingDocuments.js');

module.exports = {
  fields: {
    documentType: {
      furtherEvidence: '#manageDocument_type-FURTHER_EVIDENCE_DOCUMENTS',
      correspondence: '#manageDocument_type-CORRESPONDENCE',
      additionalApplications: '#manageDocument_type-ADDITIONAL_APPLICATIONS_DOCUMENTS',
    },
    relatedToHearing: {
      yes: '#manageDocument_relatedToHearing-Yes',
      no: '#manageDocument_relatedToHearing-No',
    },
    hearingList: '#manageDocumentsHearingList',
    c2DocumentsList: '#manageDocumentsSupportingC2List',
    supportingDocumentsCollectionId: '#supportingEvidenceDocumentsTemp',
    supportingDocuments: function(index) {
      return supportingDocumentsFragment.supportingDocuments(index, 'supportingEvidenceDocumentsTemp');
    },
  },

  selectFurtherEvidence() {
    I.click(this.fields.documentType.furtherEvidence);
  },

  selectCorrespondence() {
    I.click(this.fields.documentType.correspondence);
  },

  async selectAdditionalApplicationsSupportingDocuments() {
    await I.runAccessibilityTest();
    I.click(this.fields.documentType.additionalApplications);
  },

  selectFurtherEvidenceIsRelatedToHearing() {
    I.waitForElement(this.fields.relatedToHearing.yes);
    I.click(this.fields.relatedToHearing.yes);
  },

  selectHearing(hearingDate) {
    I.waitForElement(this.fields.hearingList);
    I.selectOption(this.fields.hearingList, `Case management hearing, ${hearingDate}`);
  },

  async selectApplicationBundleFromDropdown(index) {
    const dropdownLabel = await I.grabTextFrom(`${this.fields.c2DocumentsList} option:nth-child(${index})`);
    I.waitForElement(this.fields.c2DocumentsList);
    I.selectOption(this.fields.c2DocumentsList, dropdownLabel);
  },

  enterDocumentName(documentName, index = 0) {
    I.fillField(this.fields.supportingDocuments(index).name, documentName);
  },

  enterDocumentNotes(notes, index = 0) {
    I.fillField(this.fields.supportingDocuments(index).notes, notes);
  },

  async enterDateAndTimeReceived(dateAndTime, index = 0) {
    await I.fillDateAndTime(dateAndTime, this.fields.supportingDocuments(index).dateAndTime);
  },

  uploadDocument(document, index = 0) {
    I.attachFile(this.fields.supportingDocuments(index).document, document);
  },

  async selectFurtherEvidenceType(type, index = 0) {
    switch (type) {
      case 'Expert reports':
        I.checkOption(this.fields.supportingDocuments(index).type.expert);
        break;
      case 'Other reports':
        I.checkOption(this.fields.supportingDocuments(index).type.other);
        break;
      default:
        throw new Error(`Unsupported further evidence type ${type}`);
    }
  },

  async selectConfidential(index = 0) {
    I.click(this.fields.supportingDocuments(index).confidential);
  },

  async uploadSupportingEvidenceDocument(supportingEvidenceDocument, selectEvidenceType = false) {
    const index = await I.getActiveElementIndex();
    this.enterDocumentName(supportingEvidenceDocument.name, index);
    this.enterDocumentNotes(supportingEvidenceDocument.notes, index);
    this.uploadDocument(supportingEvidenceDocument.document, index);
    if(selectEvidenceType) {
      this.selectFurtherEvidenceType(supportingEvidenceDocument.type, index);
    }
  },

  async uploadConfidentialSupportingEvidenceDocument(supportingEvidenceDocument, selectEvidenceType = false) {
    const index = await I.getActiveElementIndex();
    await this.uploadSupportingEvidenceDocument(supportingEvidenceDocument, selectEvidenceType);
    this.selectConfidential(index);
  },
};
