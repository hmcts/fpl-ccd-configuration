const { I } = inject();
const supportingDocumentsFragment = require('../../fragments/supportingDocuments.js');

module.exports = {
  fields: {
    documentType: {
      furtherEvidence: '#manageDocument_type-FURTHER_EVIDENCE_DOCUMENTS',
      correspondence: '#manageDocument_type-CORRESPONDENCE',
      c2: '#manageDocument_type-C2',
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

  async selectC2SupportingDocuments() {
    await I.runAccessibilityTest();
    console.log('manage documents event 1');
    I.click(this.fields.documentType.c2);
  },

  selectFurtherEvidenceIsRelatedToHearing() {
    I.waitForElement(this.fields.relatedToHearing.yes);
    I.click(this.fields.relatedToHearing.yes);
  },

  selectHearing(hearingDate) {
    I.waitForElement(this.fields.hearingList);
    I.selectOption(this.fields.hearingList, `Case management hearing, ${hearingDate}`);
  },

  async selectC2FromDropdown() {
    const dropdownLabel = await I.grabTextFrom(`${this.fields.c2DocumentsList} option:nth-child(2)`);
    I.waitForElement(this.fields.c2DocumentsList);
    //await I.runAccessibilityTest();
    //console.log('manage documents event 2');
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

  async selectConfidential(index = 0) {
    I.click(this.fields.supportingDocuments(index).confidential);
  },

  async uploadSupportingEvidenceDocument(supportingEvidenceDocument) {
    const index = await I.getActiveElementIndex();
    this.enterDocumentName(supportingEvidenceDocument.name, index);
    this.enterDocumentNotes(supportingEvidenceDocument.notes, index);
    await this.enterDateAndTimeReceived(supportingEvidenceDocument.date, index);
    this.uploadDocument(supportingEvidenceDocument.document, index);
  },

  async uploadConfidentialSupportingEvidenceDocument(supportingEvidenceDocument) {
    const index = await I.getActiveElementIndex();
    this.enterDocumentName(supportingEvidenceDocument.name, index);
    this.enterDocumentNotes(supportingEvidenceDocument.notes, index);
    await this.enterDateAndTimeReceived(supportingEvidenceDocument.date, index);
    this.uploadDocument(supportingEvidenceDocument.document, index);
    this.selectConfidential(index);
  },
};
