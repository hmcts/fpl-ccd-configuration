local base = import 'lib/base.libsonnet';
local constants = import 'lib/constants.libsonnet';

local DefaultCaseEvent = base.LiveDates + {
  CaseTypeID: constants.CaseType,
  DisplayOrder: 1,
  SecurityClassification: 'Public',
  ShowSummary: 'Y',
  EndButtonLabel: 'Save and continue'
};

[
  DefaultCaseEvent + {
    ID: 'hearingBookingDetails' + item.suffix,
    Name: 'Add hearing details',
    Description: 'Add hearing booking details to a case',
    'PreConditionState(s)': item.state,
    PostConditionState: item.state,
    CallBackURLAboutToStartEvent: '${CCD_DEF_CASE_SERVICE_BASE_URL}/callback/add-hearing-bookings/about-to-start'
  } for item in [
      { suffix: '', state: 'Submitted' },
      { suffix: '-PREPARE_FOR_HEARING', state: 'PREPARE_FOR_HEARING' }
    ]
]
