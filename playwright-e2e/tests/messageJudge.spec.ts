import { test} from '@playwright/test';
import {Apihelp} from '../utils/apiFixture';

test.describe('send and reply message',()=>{
  let apiDataSetup = new Apihelp();
  test.beforeAll(()  => {

  });

  test('CTSC admin send message to Jude',
    async () => {
      apiDataSetup.createCase('CTSCSendMessageJudge');

    });

});
