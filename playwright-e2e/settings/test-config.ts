import * as dotenv from 'dotenv';
dotenv.config();

export const testConfig = {
  waEnabled: process.env.WA_ENABLED === 'true' || false,
    idamClientSecret: process.env.IDAM_CLIENT_SECRET || '',
    IDAM_RETRY_ATTEMPTS:3,
   IDAM_RETRY_BASE_MS:300,
    teardownAMRoleAssignments: [
        '[LASOLICITOR]',
        '[SOLICITORA]',
        '[CHILDSOLICITORA]'
    ],
    daysOlderThan: process.env.DAYS_OLDER_THAN || 4,
   TEST_DATA_SETUP_TIMEOUT_MS: 2000,
    APPLICATION_OF_PROCEEDING: '£2,580.00', //Application of proceedings
    PLACEMENT_APPLICATION: '£570.00', //placement order
    VARIATION_DISCHARGE_OF_CARE_SUPERVISION_ORDER: '£270.00', //Variation or discharge of care and supervision order
    C1: '£270.00', //C1 application
    C2_WITH_OTHER_APPLICATION: '£270.00' , //C2 application
    COMBINE_C1_C2: '£270.00',//C1 and C2 application
    C2_WITH_NOTICE: '£195.00',//C2 with notice application
    C2_WITHOUT_NOTICE: '£62.00',
}
