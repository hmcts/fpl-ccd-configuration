import * as dotenv from 'dotenv';
dotenv.config();

export const testConfig = {
  waEnabled: process.env.WA_ENABLED === 'false' || false,
}
