local base = import 'lib/base.libsonnet';
local constants = import 'lib/constants.libsonnet';

local UserProfile(UserIDAMId, WorkBasketDefaultState) = base.LiveDates + {
  UserIDAMId: UserIDAMId,
  WorkBasketDefaultJurisdiction: constants.Jurisdiction,
  WorkBasketDefaultCaseType: constants.CaseType,
  WorkBasketDefaultState: WorkBasketDefaultState
};

local openState = 'Open';
local submittedState = 'Submitted';
local gatekeepingState = 'Gatekeeping';

[
  UserProfile('damian@swansea.gov.uk', openState),
  UserProfile('kurt@swansea.gov.uk', openState),
  UserProfile('james@swansea.gov.uk', openState),
  UserProfile('sam@hillingdon.gov.uk', openState),
  UserProfile('siva@hillingdon.gov.uk', openState),
  UserProfile('hmcts-admin@example.com', submittedState),
  UserProfile('cafcass@example.com', submittedState),
  UserProfile('gatekeeper@mailnesia.com', gatekeepingState),
  UserProfile('judiciary@mailnesia.com', submittedState),
  UserProfile('fpl-system-update@mailnesia.com', submittedState),
]
