const I = actor();

module.exports = {

  documents: {
    socialWorkChronology: '#documents_socialWorkChronology_document_uploadDocuments_typeOfDocument',
    socialWorkStatementAndGenogram: '#documents_socialWorkStatement_document_uploadDocuments_typeOfDocument',
    socialWorkAssessment: '#documents_socialWorkAssessement_document_uploadDocuments_typeOfDocument',
    carePlan: '#documents_socialWorkCarePlan_document_uploadDocuments_typeOfDocument',
    otherDocuments_1: '#documents_socialWorkOther_0_otherDocuments_typeOfDocument',
    otherDocuments_2: '#documents_socialWorkOther_1_otherDocuments_typeOfDocument',
  },

  fields: {
    socialWorkChronologyStatus: '#documents_socialWorkChronology_document_uploadDocuments_documentStatus',
    socialWorkChronologyReason: '#documents_socialWorkChronology_document_uploadDocuments_statusReason',
    socialWorkStatementAndGenogramStatus: '#documents_socialWorkStatement_document_uploadDocuments_documentStatus',
    socialWorkAssessmentStatus: '#documents_socialWorkAssessement_document_uploadDocuments_documentStatus',
    carePlanStatus: '#documents_socialWorkCarePlan_document_uploadDocuments_documentStatus',
    otherDocumentsTitle_1: '#documents_socialWorkOther_0_otherDocuments_documentTitle',
    otherDocumentsTitle_2: '#documents_socialWorkOther_1_otherDocuments_documentTitle',

  },

  uploadSocialWorkChronology(file) {
    I.attachFile(this.documents.socialWorkChronology, file);
    I.selectOption(this.fields.socialWorkChronologyStatus, 'Attached');
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
};
