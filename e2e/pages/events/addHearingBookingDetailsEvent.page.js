const I = actor();

module.exports = {
  fields: function (index) {
    return {
      hearingBooking: {
        hearingType: {
          caseManagement: `#hearingDetails_${index}_hearingType-CASE_MANAGEMENT`,
        },
        hearingVenue: `#hearingDetails_${index}_hearingVenue`,
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

    I.click(this.fields(elementIndex).hearingBooking.hearingType);
    await I.fillField(this.fields(elementIndex).hearingBooking.hearingVenue, hearingDetails.hearingVenue);
    I.fillField(this.fields(elementIndex).hearingBooking.date.day, hearingDetails.date.day);
    I.fillField(this.fields(elementIndex).hearingBooking.date.month, hearingDetails.date.month);
    I.fillField(this.fields(elementIndex).hearingBooking.date.year, hearingDetails.date.year);
    I.fillField(this.fields(elementIndex).hearingBooking.preHearingAttendance, hearingDetails.preHearingAttendance);
    I.fillField(this.fields(elementIndex).hearingBooking.hearingTime, hearingDetails.hearingTime);
    I.click(this.fields(elementIndex).hearingBooking.hearingNeedsBooked.interpreter);
    I.click(this.fields(elementIndex).hearingBooking.hearingNeedsBooked.welsh);
    I.click(this.fields(elementIndex).hearingBooking.hearingNeedsBooked.somethingElse);
    I.fillField(this.fields(elementIndex).hearingBooking.giveDetails, hearingDetails.giveDetails);
    I.fillField(this.fields(elementIndex).hearingBooking.judgeTitle, hearingDetails.judgeTitle);
    I.fillField(this.fields(elementIndex).hearingBooking.judgeFullName, hearingDetails.fullName);
  },

  async getActiveElementIndex() {
    return await I.grabNumberOfVisibleElements('//button[text()="Remove"]') - 1;
  },
};
