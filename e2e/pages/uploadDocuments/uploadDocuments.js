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
	},

	uploadDocuments(file) {
		I.attachFile(this.documents.socialWorkChronology, file);
		I.attachFile(this.documents.socialWorkStatementAndGenogram, file);
		I.attachFile(this.documents.socialWorkAssessment, file);
		I.attachFile(this.documents.carePlan, file);
	}
};
