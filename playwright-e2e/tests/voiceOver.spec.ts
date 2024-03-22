import { voiceOverTest as test } from "@guidepup/playwright";
import { expect } from "@playwright/test";
import {Console} from "node:inspector";

test.describe("Playwright VoiceOver", () => {
  test("I can navigate the Guidepup Github page", async ({
                                                           page,
                                                           voiceOver,
                                                         }) => {
    // Navigate to Guidepup GitHub page
    await page.goto("https://idam-web-public.aat.platform.hmcts.net/login?client_id=xuiwebapp&redirect_uri=https://manage-case.aat.platform.hmcts.net/oauth2/callback&state=UgCFeohnrdQSjKu5kvIwxKUi6bvLTcfQ9EpZmFfHTR4&nonce=ZSaMIxZKUcjWZ7lxwQiHKXH3H1gkta4urp76lY0_UTg&response_type=code&scope=profile%20openid%20roles%20manage-user%20create-user%20search-user&prompt=", {
      waitUntil: "load",
    });

    // Wait for page to be ready
    await expect(page.locator('header[role="banner"]')).toBeVisible();
    // Interact with the page
    await voiceOver.navigateToWebContent();

    // Move across the page menu to the Guidepup heading using VoiceOver
    // while ((await voiceOver.itemText()) !== "heading 1") {
    //   await voiceOver.perform(voiceOver.keyboardCommands.findNextControl);
    // }
    // Move through several items.
    for (let i = 0; i < 30; i++) {
      await voiceOver.next();
    }
    console.log(JSON.stringify(await voiceOver.spokenPhraseLog()));

    // Assert that the spoken phrases are as expected
    expect(JSON.stringify(await voiceOver.spokenPhraseLog())).toContain("Sign in");
  });
});
