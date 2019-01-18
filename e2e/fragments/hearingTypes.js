/* global locate */

module.exports = {
  STANDARD_CASE_MANAGEMENT: locate('input').withAttr({id: 'hearing_type-STANDARD_CASE_HEARING'}),
  URGENT_PRELIMINARY: locate('input').withAttr({id: 'hearing_type-URGENT_PRELIMINARY_HEARING'}),
  CONTESTED_INTERIM: locate('input').withAttr({id: 'hearing_type-CONTESTED_INTERIM_HEARING'}),
  EMERGENCY_PROTECTION: locate('input').withAttr({id: 'hearing_type-EMERGENCY_PROTECTION_HEARING'}),
};