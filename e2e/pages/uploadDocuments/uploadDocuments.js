const I = actor();

module.exports = {

  documents: {
    socialWorkChronology: '#documents_socialWorkChronology_document_typeOfDocument',
    socialWorkStatementAndGenogram: '#documents_socialWorkStatement_document_typeOfDocument',
    socialWorkAssessment: '#documents_socialWorkAssessment_document_typeOfDocument',
    carePlan: '#documents_socialWorkCarePlan_document_typeOfDocument',
    socialWorkEvidenceTemplate: '#documents_socialWorkEvidenceTemplate_document_typeOfDocument',
    thresholdDocument: '#documents_threshold_document_typeOfDocument',
    checklistDocument: '#documents_checklist_document_typeOfDocument',
    otherDocuments_1: '#documents_socialWorkOther_0_typeOfDocument',
    otherDocuments_2: '#documents_socialWorkOther_1_typeOfDocument',
    courtBundle: '#courtBundle_document',
  },

  fields: {
    socialWorkChronologyStatus: '#documents_socialWorkChronology_document_documentStatus',
    socialWorkChronologyReason: '#documents_socialWorkChronology_document_statusReason',
    socialWorkStatementAndGenogramStatus: '#documents_socialWorkStatement_document_documentStatus',
    socialWorkAssessmentStatus: '#documents_socialWorkAssessment_document_documentStatus',
    carePlanStatus: '#documents_socialWorkCarePlan_document_documentStatus',
    socialWorkEvidenceTemplateStatus: '#documents_socialWorkEvidenceTemplate_document_documentStatus',
    thresholdDocumentStatus: '#documents_threshold_document_documentStatus',
    checklistDocumentStatus: '#documents_checklist_document_documentStatus',
    otherDocumentsTitle_1: '#documents_socialWorkOther_0_documentTitle',
    otherDocumentsTitle_2: '#documents_socialWorkOther_1_documentTitle',
  },

  selectSocialWorkChronologyToFollow() {
    I.selectOption(this.fields.socialWorkChronologyStatus, 'To follow');
    I.fillField(this.fields.socialWorkChronologyReason, 'mock reason');
  },

  uploadSocialWorkStatement(file) {
    I.attachFile(this.documents.socialWorkStatementAndGenogram, file);
    I.selectOption(this.fields.socialWorkStatementAndGenogramStatus, 'Included in social work evidence template (SWET)');
  },

  uploadSocialWorkAssessment(file) {
    I.attachFile(this.documents.socialWorkAssessment, file);
    I.selectOption(this.fields.socialWorkAssessmentStatus, 'Attached');
  },

  uploadCarePlan(file) {
    I.attachFile(this.documents.carePlan, file);
    I.selectOption(this.fields.carePlanStatus, 'Attached');
  },

  uploadSWET(file) {
    I.attachFile(this.documents.socialWorkEvidenceTemplate, file);
    I.selectOption(this.fields.socialWorkEvidenceTemplateStatus, 'Attached');
  },

  uploadThresholdDocument(file) {
    I.attachFile(this.documents.thresholdDocument, file);
    I.selectOption(this.fields.thresholdDocumentStatus, 'Attached');
  },

  uploadChecklistDocument(file) {
    I.attachFile(this.documents.checklistDocument, file);
    I.selectOption(this.fields.checklistDocumentStatus, 'Attached');
  },

  uploadAdditionalDocuments(file) {
    I.click('Add new');
    I.fillField(this.fields.otherDocumentsTitle_1, 'Document');
    I.attachFile(this.documents.otherDocuments_1, file);
    I.click('Add new');
    I.fillField(this.fields.otherDocumentsTitle_2, 'Document');
    I.attachFile(this.documents.otherDocuments_2, file);
  },

  uploadCourtBundle(file) {
    I.attachFile(this.documents.courtBundle, file);
  },
};
