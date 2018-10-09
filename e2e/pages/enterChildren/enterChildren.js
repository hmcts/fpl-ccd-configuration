
const I = actor();

module.exports = {

	fields: {
		enteredInformation: 'test',
		child1FullName: "#children_firstChild_enterChildren_childName",
		child1DOB: {
			day: '#children_firstChild_enterChildren_childDOB-day',
			month: '#children_firstChild_enterChildren_childDOB-month',
			year: '#children_firstChild_enterChildren_childDOB-year'
		},
		child1GenderDropdown: {
			selector:'#children_firstChild_enterChildren_childGender',
			option: 'Boy'
		},
		child1SituationDropdown: {
			selector: '#children_firstChild_enterChildren_livingSituation',
			option: 'Living with respondents',
			dateStartedStaying: {
				day: '#children_firstChild_enterChildren_situationDate-day',
				month: '#children_firstChild_enterChildren_situationDate-month',
				year: '#children_firstChild_enterChildren_situationDate-year'
			},
			addressOfChild: '#children_firstChild_enterChildren_situationAddress',
		},
		child1KeyDates: '#children_firstChild_enterChildren_keyDates',
		child1CareAndContactPlan: '#children_firstChild_enterChildren_careAndContact',
		child1AdoptionNo: '#children_firstChild_enterChildren_adoption-No',
		child1MothersName: '#children_firstChild_enterChildren_mothersName',
		child1FathersName: '#children_firstChild_enterChildren_fathersName',
		child1FatherResponsibleDropdown: {
			selector: '#children_firstChild_enterChildren_fathersResponsibility',
			option: 'Yes'
		},
		child1SocialWorkerName: '#children_firstChild_enterChildren_socialWorkerName',
		child1SocialWorkerTel: '#children_firstChild_enterChildren_socialWorkerTel',
		child1AdditionalNeedsNo: '#children_firstChild_enterChildren_additionalNeeds-No',
		child1ContactHiddenNo: '#children_firstChild_enterChildren_detailsHidden-No',
		child1LitigationNo: '#children_firstChild_enterChildren_litigationIssues-No',
		addChildButton: 'Add new',

		child2FullName: "#children_additionalChildren_0_enterChildren_childName",
		child2DOB: {
			day: '#children_additionalChildren_0_enterChildren_childDOB-day',
			month: '#children_additionalChildren_0_enterChildren_childDOB-month',
			year: '#children_additionalChildren_0_enterChildren_childDOB-year'
		},
		child2GenderDropdown: {
			selector:'#children_additionalChildren_0_enterChildren_childGender',
			option: 'Boy'
		},
		child2SituationDropdown: {
			selector: '#children_additionalChildren_0_enterChildren_livingSituation',
			option: 'Living with respondents',
			dateStartedStaying: {
				day: '#children_additionalChildren_0_enterChildren_situationDate-day',
				month: '#children_additionalChildren_0_enterChildren_situationDate-month',
				year: '#children_additionalChildren_0_enterChildren_situationDate-year'
			},
			addressOfChild: '#children_additionalChildren_0_enterChildren_situationAddress',
		},
		child2KeyDates: '#children_additionalChildren_0_enterChildren_keyDates',
		child2CareAndContactPlan: '#children_additionalChildren_0_enterChildren_careAndContact',
		child2AdoptionNo: '#children_additionalChildren_0_enterChildren_adoption-No',
		child2MothersName: '#children_additionalChildren_0_enterChildren_mothersName',
		child2FathersName: '#children_additionalChildren_0_enterChildren_fathersName',
		child2FatherResponsibleDropdown: {
			selector: '#children_additionalChildren_0_enterChildren_fathersResponsibility',
			option: 'Yes'
		},
		child2SocialWorkerName: '#children_additionalChildren_0_enterChildren_socialWorkerName',
		child2SocialWorkerTel: '#children_additionalChildren_0_enterChildren_socialWorkerTel',
		child2AdditionalNeedsNo: '#children_additionalChildren_0_enterChildren_additionalNeeds-No',
		child2ContactHiddenNo: '#children_additionalChildren_0_enterChildren_detailsHidden-No',
		child2LitigationNo: '#children_additionalChildren_0_enterChildren_litigationIssues-No',
	},

	halfFillForm() {
		I.fillField(this.fields.child1FullName, 'Timothy');
		I.fillField(this.fields.child1DOB.day, '01');
		I.fillField(this.fields.child1DOB.month, '08');
		I.fillField(this.fields.child1DOB.year, '2015');
		I.selectOption(this.fields.child1GenderDropdown.selector, this.fields.child1GenderDropdown.option);
		I.selectOption(this.fields.child1SituationDropdown.selector, this.fields.child1SituationDropdown.option);
		I.fillField(this.fields.child1SituationDropdown.dateStartedStaying.day, '10');
		I.fillField(this.fields.child1SituationDropdown.dateStartedStaying.month, '07');
		I.fillField(this.fields.child1SituationDropdown.dateStartedStaying.year, '2017');
		I.fillField(this.fields.child1SituationDropdown.addressOfChild, this.fields.enteredInformation);
	},

	fillForm() {
		I.fillField(this.fields.child1FullName, 'Timothy');
		I.fillField(this.fields.child1DOB.day, '01');
		I.fillField(this.fields.child1DOB.month, '08');
		I.fillField(this.fields.child1DOB.year, '2015');
		I.selectOption(this.fields.child1GenderDropdown.selector, this.fields.child1GenderDropdown.option);
		I.selectOption(this.fields.child1SituationDropdown.selector, this.fields.child1SituationDropdown.option);
		I.fillField(this.fields.child1SituationDropdown.dateStartedStaying.day, '10');
		I.fillField(this.fields.child1SituationDropdown.dateStartedStaying.month, '07');
		I.fillField(this.fields.child1SituationDropdown.dateStartedStaying.year, '2017');
		I.fillField(this.fields.child1SituationDropdown.addressOfChild, '35 test avenue');
		I.fillField(this.fields.child1KeyDates, this.fields.enteredInformation);
		I.fillField(this.fields.child1CareAndContactPlan, this.fields.enteredInformation);
		I.click(this.fields.child1AdoptionNo);
		I.fillField(this.fields.child1MothersName, this.fields.enteredInformation);
		I.fillField(this.fields.child1FathersName, this.fields.enteredInformation);
		I.selectOption(this.fields.child1FatherResponsibleDropdown.selector, this.fields.child1FatherResponsibleDropdown.option);
		I.fillField(this.fields.child1SocialWorkerName, this.fields.enteredInformation);
		I.fillField(this.fields.child1SocialWorkerTel, this.fields.enteredInformation);
		I.click(this.fields.child1ContactHiddenNo);
		I.click(this.fields.child1LitigationNo);
		I.click(this.fields.addChildButton);
		I.fillField(this.fields.child2FullName, 'Susan');
		I.fillField(this.fields.child2DOB.day, '01');
		I.fillField(this.fields.child2DOB.month, '07');
		I.fillField(this.fields.child2DOB.year, '2016');
		I.selectOption(this.fields.child2GenderDropdown.selector, this.fields.child2GenderDropdown.option);
		I.selectOption(this.fields.child2SituationDropdown.selector, this.fields.child2SituationDropdown.option);
		I.fillField(this.fields.child2SituationDropdown.dateStartedStaying.day, '10');
		I.fillField(this.fields.child2SituationDropdown.dateStartedStaying.month, '3');
		I.fillField(this.fields.child2SituationDropdown.dateStartedStaying.year, '2018');
		I.fillField(this.fields.child2SituationDropdown.addressOfChild, '35 test avenue');
		I.fillField(this.fields.child2KeyDates, this.fields.enteredInformation);
		I.fillField(this.fields.child2CareAndContactPlan, this.fields.enteredInformation);
		I.click(this.fields.child2AdoptionNo);
		I.fillField(this.fields.child2MothersName, this.fields.enteredInformation);
		I.fillField(this.fields.child2FathersName, this.fields.enteredInformation);
		I.selectOption(this.fields.child2FatherResponsibleDropdown.selector, this.fields.child2FatherResponsibleDropdown.option);
		I.fillField(this.fields.child2SocialWorkerName, this.fields.enteredInformation);
		I.fillField(this.fields.child2SocialWorkerTel, this.fields.enteredInformation);
		I.click(this.fields.child2ContactHiddenNo);
		I.click(this.fields.child2LitigationNo);
	}



};
