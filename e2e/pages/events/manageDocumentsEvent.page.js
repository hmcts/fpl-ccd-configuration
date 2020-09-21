const { I } = inject();
const supportingDocumentsFragment = require('../../fragments/supportingDocuments.js');

module.exports = {
  fields: function(index) {
    return {
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
      supportingDocuments: supportingDocumentsFragment.supportingDocuments(index, 'supportingEvidenceDocumentsTemp'),
    };
  },

  async selectFurtherEvidence() {
    const elementIndex = await this.getActiveElementIndex();
    I.click(this.fields(elementIndex).documentType.furtherEvidence);
  },

  async selectCorrespondence() {
    const elementIndex = await this.getActiveElementIndex();
    I.click(this.fields(elementIndex).documentType.correspondence);
  },

  async selectC2SupportingDocuments() {
    const elementIndex = await this.getActiveElementIndex();
    I.click(this.fields(elementIndex).documentType.c2);
  },

  async selectFurtherEvidenceIsRelatedToHearing() {
    const elementIndex = await this.getActiveElementIndex();
    I.waitForElement(this.fields(elementIndex).relatedToHearing.yes);
    I.click(this.fields(elementIndex).relatedToHearing.yes);
  },

  async selectHearing(hearingDate) {
    const elementIndex = await this.getActiveElementIndex();
    I.waitForElement(this.fields(elementIndex).hearingList);
    I.selectOption(this.fields(elementIndex).hearingList, `Case management hearing, ${hearingDate}`);
  },

  async select2FromDropdown() {
    const elementIndex = await this.getActiveElementIndex();
    const dropdownLabel = await I.grabTextFrom(`${this.fields(elementIndex).c2DocumentsList} option:nth-child(2)`);
    I.waitForElement(this.fields(elementIndex).c2DocumentsList);
    I.selectOption(this.fields(elementIndex).c2DocumentsList, dropdownLabel);
  },

  async enterDocumentName(documentName) {
    const elementIndex = await this.getActiveElementIndex();
    I.fillField(this.fields(elementIndex).supportingDocuments.name, documentName);
  },

  async enterDocumentNotes(notes) {
    const elementIndex = await this.getActiveElementIndex();
    I.fillField(this.fields(elementIndex).supportingDocuments.notes, notes);
  },

  async enterDateAndTimeReceived(dateAndTime) {
    const elementIndex = await this.getActiveElementIndex();
    I.fillDateAndTime(dateAndTime, this.fields(elementIndex).supportingDocuments.dateAndTime);
  },

  async uploadDocument(document) {
    const elementIndex = await this.getActiveElementIndex();
    I.attachFile(this.fields(elementIndex).supportingDocuments.document, document);
  },

  async uploadSupportingEvidenceDocument(supportingEvidenceDocument) {
    await this.enterDocumentName(supportingEvidenceDocument.name);
    await this.enterDocumentNotes(supportingEvidenceDocument.notes);
    await this.enterDateAndTimeReceived(supportingEvidenceDocument.date);
    await this.uploadDocument(supportingEvidenceDocument.document);
  },

  async getActiveElementIndex() {
    return await I.grabNumberOfVisibleElements('//button[text()="Remove"]') - 1;
  },
};
