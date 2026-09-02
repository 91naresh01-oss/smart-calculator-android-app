import time
import os
from playwright.sync_api import sync_playwright

screenshot_dir = r"C:\Users\91nar\.gemini\antigravity-ide\brain\ccc46353-73d3-490d-a029-2498de4deb09"

def run():
    with sync_playwright() as p:
        user_data = r"C:\Users\91nar\AppData\Local\Google\Chrome\User Data"
        print("Launching Chrome with user profile...")
        context = p.chromium.launch_persistent_context(
            user_data_dir=user_data,
            headless=False,
            channel="chrome",
            args=["--no-first-run", "--no-default-browser-check", "--start-maximized"]
        )
        page = context.pages[0] if context.pages else context.new_page()
        page.set_viewport_size({"width": 1280, "height": 800})
        
        target_url = "https://play.google.com/console/u/0/developers/6928195566204423162/app-list"
        print(f"Navigating to {target_url}...")
        page.goto(target_url, wait_until="networkidle", timeout=60000)
        time.sleep(3)
        
        print("Page Title:", page.title())
        print("Current URL:", page.url)
        page.screenshot(path=os.path.join(screenshot_dir, "play_step1.png"))
        print("Saved play_step1.png")
        
        # Look for Create app button
        create_btn = page.locator("button:has-text('Create app'), a:has-text('Create app')").first
        if create_btn.is_visible(timeout=10000):
            print("Clicking 'Create app' button...")
            create_btn.click()
            page.wait_for_load_state("networkidle")
            time.sleep(3)
            page.screenshot(path=os.path.join(screenshot_dir, "play_step2_form.png"))
            print("Saved play_step2_form.png. Current URL:", page.url)
            
            # Fill app name
            print("Filling App Name...")
            app_name_input = page.locator("input[aria-label*='App name'], input[name*='appName'], input[id*='app-name'], input[type='text']").first
            if app_name_input.is_visible(timeout=5000):
                app_name_input.fill("Smart Calculator: Note & Cash")
                time.sleep(1)
            
            # Select App / Game
            print("Selecting App radio button...")
            app_radio = page.locator("text='App' >> xpath=..").first
            if app_radio.is_visible(timeout=3000):
                app_radio.click()
                time.sleep(0.5)
            
            # Select Free / Paid
            print("Selecting Free radio button...")
            free_radio = page.locator("text='Free' >> xpath=..").first
            if free_radio.is_visible(timeout=3000):
                free_radio.click()
                time.sleep(0.5)
            
            # Accept Declarations Checkboxes
            print("Accepting declarations checkboxes...")
            checkboxes = page.locator("input[type='checkbox'], [role='checkbox']")
            count = checkboxes.count()
            print(f"Found {count} checkboxes")
            for i in range(count):
                cb = checkboxes.nth(i)
                try:
                    if not cb.is_checked():
                        cb.click()
                        time.sleep(0.3)
                except Exception as e:
                    print(f"Checkbox {i} click error:", e)
            
            time.sleep(1)
            page.screenshot(path=os.path.join(screenshot_dir, "play_step3_filled.png"))
            print("Saved play_step3_filled.png")
            
            # Submit Create app
            submit_btn = page.locator("button:has-text('Create app')").last
            if submit_btn.is_visible(timeout=5000):
                print("Submitting Create app...")
                submit_btn.click()
                time.sleep(5)
                page.wait_for_load_state("networkidle", timeout=30000)
                time.sleep(3)
                page.screenshot(path=os.path.join(screenshot_dir, "play_step4_created.png"))
                print("App Created Successfully! Dashboard URL:", page.url)
        else:
            print("Create app button not found on page. Check screenshot.")
            
        print("Script completed. Leaving browser open for user or closing.")
        # keep open briefly to ensure operations persist
        time.sleep(3)
        context.close()

if __name__ == "__main__":
    run()
