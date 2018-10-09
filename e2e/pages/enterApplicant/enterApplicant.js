const applicant = require('../../fixtures/applicant.js');
const solicitor = require('../../fixtures/solicitor.js');

const I = actor();

module.exports = {

	fields: {
		applicant: {
			name: '#enterApplicant_name',
			nameOfPersonToContact: '#enterApplicant_personToContact',
			jobTitle: '#enterApplicant_jobTitle',
			address: '#enterApplicant_address',
			mobileNumber: '#enterApplicant_mobile',
			telephoneNumber: '#enterApplicant_telephone',
			email: '#enterApplicant_email',
		},
		solicitor: {
			name: '#enterApplicant_solicitorName',
			mobileNumber: '#enterApplicant_solicitorMobile',
			telephoneNumber: '#enterApplicant_solicitorTelephone',
			email: '#enterApplicant_solicitorEmail',
			dx: '#enterApplicant_solicitorDx',
			reference: '#enterApplicant_solicitorReference',
		}
	},

	enterApplicantDetails() {
		I.fillField(this.fields.applicant.name, applicant.name);
		I.fillField(this.fields.applicant.nameOfPersonToContact, applicant.nameOfPersonToContact);
		I.fillField(this.fields.applicant.jobTitle, applicant.jobTitle);
		I.fillField(this.fields.applicant.address, applicant.address);
		I.fillField(this.fields.applicant.mobileNumber, applicant.mobileNumber);
		I.fillField(this.fields.applicant.telephoneNumber, applicant.telephoneNumber);
		I.fillField(this.fields.applicant.email, applicant.email);
	},

	enterSolicitorDetails() {
		I.fillField(this.fields.solicitor.name, solicitor.name);
		I.fillField(this.fields.solicitor.mobileNumber, solicitor.mobileNumber);
		I.fillField(this.fields.solicitor.telephoneNumber, solicitor.telephoneNumber);
		I.fillField(this.fields.solicitor.email, solicitor.email);
		I.fillField(this.fields.solicitor.dx, solicitor.dx);
		I.fillField(this.fields.solicitor.reference, solicitor.reference);
	}
};
