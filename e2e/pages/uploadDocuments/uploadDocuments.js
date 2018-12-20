const I = actor();

module.exports = {

  documents: {
    socialWorkChronology: '#documents_socialWorkChronology_document_typeOfDocument',
    socialWorkStatementAndGenogram: '#documents_socialWorkStatement_document_typeOfDocument',
    socialWorkAssessment: '#documents_socialWorkAssessment_document_typeOfDocument',
    carePlan: '#documents_socialWorkCarePlan_document_typeOfDocument',
    otherDocuments_1: '#documents_socialWorkOther_0_typeOfDocument',
    otherDocuments_2: '#documents_socialWorkOther_1_typeOfDocument',
    standardDirections: '#standardDirections',
    courtBundle: '#courtBundle_document',
  },

  fields: {
    socialWorkChronologyStatus: '#documents_socialWorkChronology_document_documentStatus',
    socialWorkChronologyReason: '#documents_socialWorkChronology_document_statusReason',
    socialWorkStatementAndGenogramStatus: '#documents_socialWorkStatement_document_documentStatus',
    socialWorkAssessmentStatus: '#documents_socialWorkAssessment_document_documentStatus',
    carePlanStatus: '#documents_socialWorkCarePlan_document_documentStatus',
    otherDocumentsTitle_1: '#documents_socialWorkOther_0_documentTitle',
    otherDocumentsTitle_2: '#documents_socialWorkOther_1_documentTitle',
  },

  uploadSocialWorkStatement(file) {
    I.attachFile(this.documents.socialWorkStatementAndGenogram, file);
    I.selectOption(this.fields.socialWorkStatementAndGenogramStatus, 'Attached');
  },

  uploadSocialWorkAssessment(file) {
    I.attachFile(this.documents.socialWorkAssessment, file);
    I.selectOption(this.fields.socialWorkAssessmentStatus, 'Attached');
  },

  uploadCarePlan(file) {
    I.attachFile(this.documents.carePlan, file);
    I.selectOption(this.fields.carePlanStatus, 'Attached');
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

  selectSocialWorkChronologyToFollow() {
    I.selectOption(this.fields.socialWorkChronologyStatus, 'To follow');
    I.fillField(this.fields.socialWorkChronologyReason, 'mock reason');
  },

  uploadStandardDirections(file) {
    I.attachFile(this.documents.standardDirections, file);
  },
};
