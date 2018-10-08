const I = actor();

module.exports = {

	documents: {
		socialWorkChronology: '#uploadDocuments_chronology1_uploadDocuments_typeOfDocument',
		socialWorkStatementAndGenogram: '#uploadDocuments_chronology2_uploadDocuments_typeOfDocument',
		socialWorkAssessment: '#uploadDocuments_chronology3_uploadDocuments_typeOfDocument',
		carePlan: '#uploadDocuments_chronology4_uploadDocuments_typeOfDocument',
	},

	fields: {
		socialWorkChronologyStatus: '#uploadDocuments_chronology1_uploadDocuments_documentStatus',
		socialWorkChronologyReason: '#uploadDocuments_chronology1_uploadDocuments_statusReason',
		socialWorkStatementAndGenogramStatus: '#uploadDocuments_chronology2_uploadDocuments_documentStatus',
		socialWorkAssessmentStatus: '#uploadDocuments_chronology3_uploadDocuments_documentStatus',
		carePlanStatus: '#uploadDocuments_chronology4_uploadDocuments_documentStatus',

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
