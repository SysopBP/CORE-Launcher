# CORE baseline device checklist

Record APK version, commit, Android/One UI version, navigation mode and whether D2 is active for each pass. Test CORE as ordinary Home before enabling optional integration.

| Test | Expected result | Result |
|---|---|---|
| Set default Home | Home opens CORE; Samsung Home remains available in settings | Pending |
| Launch and return | Apps open; Home returns reliably from full-screen apps | Pending |
| Gesture navigation | Swipe Home, Back and Recents work without black frames or freezes | Pending |
| Three-button navigation | Home, Back and Recents operate normally | Pending |
| Icons and shortcuts | Drag/drop, dock changes and long-press app shortcuts work | Pending |
| Folder persistence | Folder contents survive launcher restart and reboot | Pending |
| Widget hosting | Add/configure/resize widgets; placement survives restart | Pending |
| Search | App search works; optional permissions can be denied without crash | Pending |
| Package changes | Newly installed and removed apps update the drawer | Pending |
| Rotation/insets | No overlap with status/navigation bars or keyboard | Pending |
| Screen lock and calls | Returning after unlock/call resumes Home normally | Pending |
| D2 coexistence | D2 locks/unlocks; CORE appears after authorized unlock | Pending |
| Recovery | Switch back to Samsung Home through Android settings | Pending |

Do not claim Quickstep/system Recents support from ordinary Home tests. Assess that separately after Android 17/One UI 9 compatibility work.
