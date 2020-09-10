const { I } = inject();

module.exports = {
  supportingDocumentType: '',
  fields: function(index, supportingDocumentType) {
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
      supportingEvidenceDocuments: {
        name: `#${supportingDocumentType}_${index}_name`,
        notes: `#${supportingDocumentType}_${index}_notes`,
        dateAndTime: `#${supportingDocumentType}_${index}_dateTimeReceived`,
        document: `#${supportingDocumentType}_${index}_document`,
      },
    };
  },

  async selectFurtherEvidence() {
    const elementIndex = await this.getActiveElementIndex();
    const type = this.getSupportingEvidenceDocumentType();
    I.click(this.fields(elementIndex, type).documentType.furtherEvidence);
  },

  async selectCorrespondence() {
    const elementIndex = await this.getActiveElementIndex();
    const type = this.getSupportingEvidenceDocumentType();
    I.click(this.fields(elementIndex, type).documentType.correspondence);
  },

  async selectC2SupportingDocuments() {
    const elementIndex = await this.getActiveElementIndex();
    const type = this.getSupportingEvidenceDocumentType();
    I.click(this.fields(elementIndex, type).documentType.c2);
  },

  async selectFurtherEvidenceIsRelatedToHearing() {
    const elementIndex = await this.getActiveElementIndex();
    const type = this.getSupportingEvidenceDocumentType();
    I.waitForElement(this.fields(elementIndex, type).relatedToHearing.yes);
    I.click(this.fields(elementIndex, type).relatedToHearing.yes);
  },

  async selectHearing(hearingDate) {
    const elementIndex = await this.getActiveElementIndex();
    const type = this.getSupportingEvidenceDocumentType();
    I.waitForElement(this.fields(elementIndex, type).hearingList);
    I.selectOption(this.fields(elementIndex, type).hearingList, `Case management hearing, ${hearingDate}`);
  },

  async selectC2Document(index, uploadedDateTime) {
    const elementIndex = await this.getActiveElementIndex();
    const type = this.getSupportingEvidenceDocumentType();
    I.waitForElement(this.fields(elementIndex, type).c2DocumentsList);
    I.selectOption(this.fields(elementIndex, type).c2DocumentsList, `Application ${index}, ${uploadedDateTime}`);
  },

  async enterDocumentName(documentName) {
    const elementIndex = await this.getActiveElementIndex();
    const type = this.getSupportingEvidenceDocumentType();
    I.fillField(this.fields(elementIndex, type).supportingEvidenceDocuments.name, documentName);
  },

  async enterDocumentNotes(notes) {
    const elementIndex = await this.getActiveElementIndex();
    const type = this.getSupportingEvidenceDocumentType();
    I.fillField(this.fields(elementIndex, type).supportingEvidenceDocuments.notes, notes);
  },

  async enterDateAndTimeReceived(dateAndTime) {
    const elementIndex = await this.getActiveElementIndex();
    const type = this.getSupportingEvidenceDocumentType();
    I.fillDateAndTime(dateAndTime, this.fields(elementIndex, type).supportingEvidenceDocuments.dateAndTime);
  },

  async uploadDocument(document) {
    const elementIndex = await this.getActiveElementIndex();
    const type = this.getSupportingEvidenceDocumentType();
    I.attachFile(this.fields(elementIndex, type).supportingEvidenceDocuments.document, document);
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

  setSupportingEvidenceDocumentType(type) {
    this.supportingDocumentType = type;
  },

  getSupportingEvidenceDocumentType() {
    return this.supportingDocumentType;
  },
};
