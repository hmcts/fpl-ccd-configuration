local base = import 'lib/base.libsonnet';
local constants = import 'lib/constants.libsonnet';

local CaseEventToField(CaseEventID, CaseFieldID, DisplayContext, CallBackURLMidEvent = '', PageFieldDisplayOrder = 1) = base.LiveDates + {
  CaseTypeID: constants.CaseType,
  CaseEventID: CaseEventID,
  CaseFieldID: CaseFieldID,
  PageFieldDisplayOrder: PageFieldDisplayOrder,
  DisplayContext: DisplayContext,
  PageID: 1,
  ShowSummaryChangeOption: "Y",
  PageDisplayOrder: 1,
  PageColumnNumber: 1,
  CallBackURLMidEvent: CallBackURLMidEvent
};

[
  CaseEventToField('hearingBookingDetails' + suffix, 'hearingDetails', "MANDATORY", "${CCD_DEF_CASE_SERVICE_BASE_URL}/callback/add-hearing-bookings/mid-event") for suffix in ['', 'Gatekeeping', '-PREPARE_FOR_HEARING']
] + std.flattenArrays([
  [
    CaseEventToField('createNoticeOfProceedings' + suffix, 'proceedingLabel', 'READONLY', PageFieldDisplayOrder = 1),
    CaseEventToField('createNoticeOfProceedings' + suffix, 'noticeOfProceedings', 'READONLY', PageFieldDisplayOrder = 2)
  ] for suffix in ['', '-PREPARE_FOR_HEARING']
])
