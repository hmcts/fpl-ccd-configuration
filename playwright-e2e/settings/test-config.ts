import * as dotenv from 'dotenv';
dotenv.config();

export const testConfig = {
  waEnabled: process.env.WA_ENABLED === 'true' || false,
    teardownAMRoleAssignments: [
        '[LASOLICITOR]',
        '[SOLICITORA]',
        '[CHILDSOLICITORA]'
    ],
    daysOlderThan: process.env.DAYS_OLDER_THAN || 4,
   TEST_DATA_SETUP_TIMEOUT_MS: 2000,
}
