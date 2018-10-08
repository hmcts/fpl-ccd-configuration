const I = actor();

module.exports = {

	documents: {
		socialWorkChronology: '#documents_socialWorkChronology_document_uploadDocuments_typeOfDocument',
		socialWorkStatementAndGenogram: '#documents_socialWorkStatement_document_uploadDocuments_typeOfDocument',
		socialWorkAssessment: '#documents_socialWorkAssessement_document_uploadDocuments_typeOfDocument',
		carePlan: '#documents_socialWorkCarePlan_document_uploadDocuments_typeOfDocument',
	},

	fields: {
		socialWorkChronologyStatus: '#documents_socialWorkChronology_document_uploadDocuments_documentStatus',
		socialWorkChronologyReason: '#documents_socialWorkChronology_document_uploadDocuments_statusReason',
		socialWorkStatementAndGenogramStatus: '#documents_socialWorkStatement_document_uploadDocuments_documentStatus',
		socialWorkAssessmentStatus: '#documents_socialWorkAssessement_document_uploadDocuments_documentStatus',
		carePlanStatus: '#documents_socialWorkCarePlan_document_uploadDocuments_documentStatus',

	},

	uploadDocuments(file) {
		I.attachFile(this.documents.socialWorkChronology, file);
		I.selectOption(this.fields.socialWorkChronologyStatus, 'Attached');
		I.attachFile(this.documents.socialWorkStatementAndGenogram, file);
		I.selectOption(this.fields.socialWorkStatementAndGenogramStatus, 'Attached');
		I.attachFile(this.documents.socialWorkAssessment, file);
		I.selectOption(this.fields.socialWorkAssessmentStatus, 'Attached');
		I.attachFile(this.documents.carePlan, file);
		I.selectOption(this.fields.carePlanStatus, 'Attached');
	}
};
