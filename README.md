XposedMod_ForceTranslucentKitKat
================================

Force Android apps to use the new Translucent Decor in KitKat thanks to Xposed &amp; this mod

This module for the Xposed Framework will force the apps selected to use the new Kit Kat Translucent Navbar and Status bar. When loaded, this app loads a list view of all your installed apps and you can freely select which to turn on.

Options per app include:
- Translucent Navigation Bar
- Translucent Status Bar
- Top Padding
- Bottom Padding

The padding options were added because some apps (especially those with Action Bars) will end up having useful information and buttons hidden under the now translucent system bars. The padding option forces the app to be in a smaller window so you can still enjoy both the translucent system bars & the whole view for the app.

Overall options include:
- Import/Export Settings : the settings per app can be exported and imported into a CSV file. Useful for testing things out while having a fail safe, switching devices, or sharing your values. CSV files open wonderfully in Excel and Google Docs
- Multi Edit : After selecting multi edit, you are brought to a screen with the 4 per app options, set them as you desire, then you are brought back to the main app list, select which apps you want your new config you just did applied to. It will overwrite all settings for the apps selected, unselected apps aren't changed.

The following apps work wonderful with translucent nav and status bar and padding of 0 for both top and bottom:
- Aviate
- Google Search (aka Google Now)
- Imgur
- XDA (The top nav buttons are big enough to still touch)
- Cal
- Threes

The following apps required some top padding (145 for Moto X) to achieve full functionality:
- Xposed Installer
- Gmail / Email
- Google Play Store

Some apps don't work with the padding very well and there's not much this mod can do
- Most video playing apps (VLC & MLB At Bat), while you'll use padding to avoid the screen going under the Actionbar, it will ruin the full screen video by padding that too
- Some apps that use keyboard input with a text box on the bottom (Google Plus) will not move the text box to above the keyboard, therefore you can't see what you're typing

Without saying, this will only install on API 19+ (4.4)
XDA Thread: http://forum.xda-developers.com/xposed/modules/mod-force-translucent-kitkat-t2721632
Xposed Framework : http://xposed.info/
