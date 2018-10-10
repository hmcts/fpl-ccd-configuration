
const I = actor();

module.exports = {

	fields: {
		physicalHarm: {
			yes: '#riskHarm_enterRisk_physicalHarmQuestion-Yes',
			pastHarm: locate('input').withAttr({id: 'riskHarm_enterRisk_physicalHarmMultiSelect-Past harm'}),
		},
		emotionalHarmNo: '#riskHarm_enterRisk_emotionalHarmQuestion-No',
		sexualAbuseNo: '#riskHarm_enterRisk_sexualAbuseQuestion-No',
		neglect: {
			yes: '#riskHarm_enterRisk_neglectQuestion-Yes',
			pastHarm: locate('input').withAttr({id: 'riskHarm_enterRisk_neglectMultiSelect-Past harm'}),
			futureHarm: locate('input').withAttr({id: 'riskHarm_enterRisk_neglectMultiSelect-Future risk of harm'})
		}
	},

	completePhyiscalHarm() {
		I.click(this.fields.physicalHarm.yes);
		I.checkOption(this.fields.physicalHarm.pastHarm);
	},

	completeEmotionalHarm() {
		I.click(this.fields.emotionalHarmNo);
	},

	completeSexualAbuse() {
		I.click(this.fields.sexualAbuseNo);
	},

	completeNeglect() {
		I.click(this.fields.neglect.yes);
		I.checkOption(this.fields.neglect.pastHarm);
		I.checkOption(this.fields.neglect.futureHarm);
	}
};
