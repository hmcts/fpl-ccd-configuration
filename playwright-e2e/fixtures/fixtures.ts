import { mergeTests, mergeExpects } from '@playwright/test';
import { test as create } from './create-fixture';
import { test as a11yTest, expect as a11yExpect } from './axe-fixture';

export const test = mergeTests(create, a11yTest);
export const expect = mergeExpects(a11yExpect);