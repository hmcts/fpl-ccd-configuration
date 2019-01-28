/* global locate */

const I = actor();

module.exports = {

  fields: {
    physicalHarm: {
      yes: '#risks_physicalHarm-Yes',
      pastHarm: locate('input').withAttr({id: 'risks_physicalHarmOccurrences-PAST_HARM'}),
    },
    emotionalHarmNo: '#risks_emotionalHarm-No',
    sexualAbuseNo: '#risks_sexualAbuse-No',
    neglect: {
      yes: '#risks_neglect-Yes',
      pastHarm: locate('input').withAttr({id: 'risks_neglectOccurrences-PAST_HARM'}),
      futureHarm: locate('input').withAttr({id: 'risks_neglectOccurrences-FUTURE_RISK_OF_HARM'}),
    },
  },

  completePhysicalHarm() {
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
