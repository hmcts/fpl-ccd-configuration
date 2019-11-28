const { I } = inject();
const judgeAndLegalAdvisor = require('../../fragments/judgeAndLegalAdvisor');

module.exports = {
  fields: function (index) {
    return {
      hearingBooking: {
        type: {
          caseManagement: `#hearingDetails_${index}_type-CASE_MANAGEMENT`,
        },
        venue: `#hearingDetails_${index}_venue`,
        startDate: {
          second: `#hearingDetails_${index}_startDate-second`,
          minute: `#hearingDetails_${index}_startDate-minute`,
          hour: `#hearingDetails_${index}_startDate-hour`,
          day: `#hearingDetails_${index}_startDate-day`,
          month: `#hearingDetails_${index}_startDate-month`,
          year: `#hearingDetails_${index}_startDate-year`,
        },
        endDate: {
          second: `#hearingDetails_${index}_endDate-second`,
          minute: `#hearingDetails_${index}_endDate-minute`,
          hour: `#hearingDetails_${index}_endDate-hour`,
          day: `#hearingDetails_${index}_endDate-day`,
          month: `#hearingDetails_${index}_endDate-month`,
          year: `#hearingDetails_${index}_endDate-year`,
        },
        hearingNeedsBooked: {
          interpreter: `#hearingDetails_${index}_hearingNeedsBooked-INTERPRETER`,
          welsh: `#hearingDetails_${index}_hearingNeedsBooked-SPOKEN_OR_WRITTEN_WELSH`,
          somethingElse: `#hearingDetails_${index}_hearingNeedsBooked-SOMETHING_ELSE`,
        },
        giveDetails: `#hearingDetails_${index}_hearingNeedsDetails`,
      },
    };
  },

  async enterHearingDetails(hearingDetails) {
    const elementIndex = await this.getActiveElementIndex();

    I.click(this.fields(elementIndex).hearingBooking.type);
    I.selectOption(this.fields(elementIndex).hearingBooking.venue, hearingDetails.venue);
    I.fillField(this.fields(elementIndex).hearingBooking.startDate.second, hearingDetails.startDate.second);
    I.fillField(this.fields(elementIndex).hearingBooking.startDate.minute, hearingDetails.startDate.minute);
    I.fillField(this.fields(elementIndex).hearingBooking.startDate.hour, hearingDetails.startDate.hour);
    I.fillField(this.fields(elementIndex).hearingBooking.startDate.day, hearingDetails.startDate.day);
    I.fillField(this.fields(elementIndex).hearingBooking.startDate.month, hearingDetails.startDate.month);
    I.fillField(this.fields(elementIndex).hearingBooking.startDate.year, hearingDetails.startDate.year);
    I.fillField(this.fields(elementIndex).hearingBooking.endDate.second, hearingDetails.endDate.second);
    I.fillField(this.fields(elementIndex).hearingBooking.endDate.minute, hearingDetails.endDate.minute);
    I.fillField(this.fields(elementIndex).hearingBooking.endDate.hour, hearingDetails.endDate.hour);
    I.fillField(this.fields(elementIndex).hearingBooking.endDate.day, hearingDetails.endDate.day);
    I.fillField(this.fields(elementIndex).hearingBooking.endDate.month, hearingDetails.endDate.month);
    I.fillField(this.fields(elementIndex).hearingBooking.endDate.year, hearingDetails.endDate.year);
    I.click(this.fields(elementIndex).hearingBooking.hearingNeedsBooked.interpreter);
    I.click(this.fields(elementIndex).hearingBooking.hearingNeedsBooked.welsh);
    I.click(this.fields(elementIndex).hearingBooking.hearingNeedsBooked.somethingElse);
    I.fillField(this.fields(elementIndex).hearingBooking.giveDetails, hearingDetails.giveDetails);
    this.enterJudgeAndLegalAdvisor(hearingDetails.judgeAndLegalAdvisor.judgeLastName, hearingDetails.judgeAndLegalAdvisor.legalAdvisorName);
  },

  async enterJudgeAndLegalAdvisor(judgeLastName, legalAdvisorName) {
    const elementIndex = await this.getActiveElementIndex();
    const complexTypeAppender = `hearingDetails_${elementIndex}_`;
    judgeAndLegalAdvisor.selectJudgeTitle(complexTypeAppender);
    judgeAndLegalAdvisor.enterJudgeLastName(judgeLastName, complexTypeAppender);
    judgeAndLegalAdvisor.enterLegalAdvisorName(legalAdvisorName, complexTypeAppender);
  },

  async getActiveElementIndex() {
    return await I.grabNumberOfVisibleElements('//button[text()="Remove"]') - 1;
  },
};
