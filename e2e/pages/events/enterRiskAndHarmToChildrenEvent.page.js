const { I } = inject();

module.exports = {

  fields: {
    physicalHarm: {
      yes: '#risks_physicalHarm_Yes',
      pastHarm: locate('input').withAttr({id: 'risks_physicalHarmOccurrences-Past harm'}),
    },
    emotionalHarmNo: '#risks_emotionalHarm_No',
    sexualAbuseNo: '#risks_sexualAbuse_No',
    neglect: {
      yes: '#risks_neglect_Yes',
      pastHarm: locate('input').withAttr({id: 'risks_neglectOccurrences-Past harm'}),
      futureHarm: locate('input').withAttr({id: 'risks_neglectOccurrences-Future risk of harm'}),
    },
  },

  async completePhysicalHarm() {
    await I.runAccessibilityTest();
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
  },
};
