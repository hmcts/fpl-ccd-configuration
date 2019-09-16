const I = actor();

module.exports = {
  fields: function (index) {
    return {
      hearingBooking: {
        type: {
          caseManagement: `#hearingDetails_${index}_type-CASE_MANAGEMENT`,
        },
        venue: `#hearingDetails_${index}_venue`,
        date: {
          day: `#hearingDetails_${index}_date-day`,
          month: `#hearingDetails_${index}_date-month`,
          year: `#hearingDetails_${index}_date-year`,
        },
        preHearingAttendance: `#hearingDetails_${index}_preHearingAttendance`,
        time: `#hearingDetails_${index}_time`,
        hearingNeedsBooked: {
          interpreter: `#hearingDetails_${index}_hearingNeedsBooked-INTERPRETER`,
          welsh: `#hearingDetails_${index}_hearingNeedsBooked-SPOKEN_OR_WRITTEN_WELSH`,
          somethingElse: `#hearingDetails_${index}_hearingNeedsBooked-SOMETHING_ELSE`,
        },
        giveDetails: `#hearingDetails_${index}_hearingNeedsDetails`,
        judgeTitle: `#hearingDetails_${index}_judgeTitle`,
        judgeName: `#hearingDetails_${index}_judgeName`,
      },
    };
  },
  addHearingButton: 'Add new',

  addHearing() {
    I.click(this.addHearingButton);
    I.wait(1);
  },

  async enterHearingDetails(hearingDetails) {
    const elementIndex = await this.getActiveElementIndex();

    I.click(this.fields(elementIndex).hearingBooking.type);
    I.fillField(this.fields(elementIndex).hearingBooking.venue, hearingDetails.venue);
    I.fillField(this.fields(elementIndex).hearingBooking.date.day, hearingDetails.date.day);
    I.fillField(this.fields(elementIndex).hearingBooking.date.month, hearingDetails.date.month);
    I.fillField(this.fields(elementIndex).hearingBooking.date.year, hearingDetails.date.year);
    I.fillField(this.fields(elementIndex).hearingBooking.preHearingAttendance, hearingDetails.preHearingAttendance);
    I.fillField(this.fields(elementIndex).hearingBooking.time, hearingDetails.time);
    I.click(this.fields(elementIndex).hearingBooking.hearingNeedsBooked.interpreter);
    I.click(this.fields(elementIndex).hearingBooking.hearingNeedsBooked.welsh);
    I.click(this.fields(elementIndex).hearingBooking.hearingNeedsBooked.somethingElse);
    I.fillField(this.fields(elementIndex).hearingBooking.giveDetails, hearingDetails.giveDetails);
    I.fillField(this.fields(elementIndex).hearingBooking.judgeTitle, hearingDetails.judgeTitle);
    I.fillField(this.fields(elementIndex).hearingBooking.judgeName, hearingDetails.lastName);
  },

  async getActiveElementIndex() {
    return await I.grabNumberOfVisibleElements('//button[text()="Remove"]') - 1;
  },
};
