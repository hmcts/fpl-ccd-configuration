const {I} = inject();

module.exports = {
  fields:
      {
        placementApplicationList: '#placementList',
        hearingPage: {
          dateTime: '#placementNoticeDateTime',
          duration: '#placementNoticeDuration',
          venue: '#placementNoticeVenue',
        },
      },

  selectPlacementApplication (childName) {
    I.selectOption(this.fields.placementApplicationList, childName);
  },

  async enterHearingDetails(dateTime) {
    await I.fillDateAndTime(dateTime, this.fields.hearingPage.dateTime);
    await I.fillField(this.fields.hearingPage.duration, '1');
    await I.selectOption(this.fields.hearingPage.venue, 'Aberdeen Tribunal Hearing Centre');
  },
};
