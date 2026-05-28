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
}
