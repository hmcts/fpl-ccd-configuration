const I = actor();

module.exports = {
  fields: function (index) {
    return {
      hearingDetails: {
        hearingType: {
          caseManagement: `#hearingDetails_${index}_hearingType-CASE_MANAGEMENT`,
        },
        venue: `#hearingDetails_${index}_venue`,
        date: {
          day: `#hearingDetails_${index}_hearingDate-day`,
          month: `#hearingDetails_${index}_hearingDate-month`,
          year: `#hearingDetails_${index}_hearingDate-year`,
        },
        preHearingAttendance: `#hearingDetails_${index}_preHearingAttendance`,
        hearingTime: `#hearingDetails_${index}_hearingTime`,

        hearingNeedsBooked: {
          interpreter: `#hearingDetails_${index}_hearingNeededDetails-INTERPRETER`,
          welsh: `#hearingDetails_${index}_hearingNeededDetails-SPOKEN_OR_WRITTEN_WELSH`,
          somethingElse: `#hearingDetails_${index}_hearingNeededDetails-SOMETHING_ELSE`,
        },
        giveDetails: `#hearingDetails_${index}_hearingNeededGiveDetails`,
        judgeTitle: `#hearingDetails_${index}_judgeTitle`,
        judgeFullName: `#hearingDetails_${index}_judgeFullName`,
      },
    };
  },

  addHearingButton: '//*[@id="hearingDetails"]/div/button[1]',

  addHearing() {
    I.click(this.addHearingButton);
  },

  async enterHearingDetails(hearingDetails) {
    const elementIndex = await this.getActiveElementIndex();

    I.click(this.fields(elementIndex).hearingDetails.hearingType);
    await I.fillField(this.fields(elementIndex).hearingDetails.venue, hearingDetails.venue);
    await I.fillField(this.fields(elementIndex).hearingDetails.date.day, hearingDetails.date.day);
    I.fillField(this.fields(elementIndex).hearingDetails.date.month, hearingDetails.date.month);
    I.fillField(this.fields(elementIndex).hearingDetails.date.year, hearingDetails.date.year);
    I.fillField(this.fields(elementIndex).hearingDetails.preHearingAttendance, hearingDetails.preHearingAttendance);
    I.fillField(this.fields(elementIndex).hearingDetails.hearingTime, hearingDetails.hearingTime);
    I.click(this.fields(elementIndex).hearingDetails.hearingNeedsBooked.interpreter);
    I.click(this.fields(elementIndex).hearingDetails.hearingNeedsBooked.welsh);
    I.click(this.fields(elementIndex).hearingDetails.hearingNeedsBooked.somethingElse);
    I.fillField(this.fields(elementIndex).hearingDetails.giveDetails, hearingDetails.giveDetails);
    I.fillField(this.fields(elementIndex).hearingDetails.judgeTitle, hearingDetails.judgeTitle);
    I.fillField(this.fields(elementIndex).hearingDetails.judgeFullName, hearingDetails.fullName);
  },

  async getActiveElementIndex() {
    return await I.grabNumberOfVisibleElements('//button[text()="Remove"]') - 1;
  },
};
