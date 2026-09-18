# 93 Services Offline App

**Designed & Developed by TECHART (PTY) LTD**

Production-ready Android source package for 93 Services. The app is designed for a single company and does not require registration, login or internet access for normal use.

## User workflow

1. Open app → 93 Services logo splash.
2. Choose **Create Invoice** or **Create Quote**.
3. Open the compact multi-select **Services** dropdown and select any number of standard services.
4. Use **Add Manual Item** whenever a custom line item is needed.
5. Continue to the document editor; selected services are already loaded.
6. Enter quantities and unit prices. Line totals calculate automatically as **Qty × Unit Price**.
7. Press **+ Add Item** as many times as necessary — no fixed line-item limit.
8. Select/save a customer, add job/reference and optional vehicle registration.
9. Apply a discount and optionally VAT.
10. For invoices, record payment status and amount paid.
11. Generate the PDF, open it, save it to a chosen location, or share it through the Android share sheet.

## Convenience features

- Saved customer picker for repeat clients.
- Editable services and default unit prices.
- Add and delete services without changing the app code.
- Quote history can be reused or converted to a new invoice.
- History can reopen/share PDFs; missing old PDF files can be regenerated from saved document data.
- Automatic document numbering with separate invoice and quote counters.
- Custom JSON backup/restore for services, customers, history, settings and numbering.
- VAT defaults are configurable; VAT is optional and disabled by default.
- Generated PDFs are copied into **Downloads/93 Services** on Android 10+ and can also be saved via the system document picker.
- The app does not request the INTERNET permission.

## Company data in the PDF

93 SERVICES (PTY) LTD
REG: 2019/238585/07
SASOL MVUDI PARK, THOHOYANDOU, 0950
Cell: 079 319 9611
Email: 93eventtravel@gmail.com

Payment details are included from the supplied 93 Services document.

## Branding

The supplied current 93 Services logo is used for the splash screen, home screen and Android launcher icon.

## Build configuration

- Android Gradle Plugin: 9.4.0
- Gradle: 9.6.0
- Android compile/target SDK: 36
- Minimum Android version: API 26
- Java source/target: 17
- Release signing material: `93_services_release.keystore` + `keystore.properties`

AGP 9.4.0 supports Gradle 9.6.0 as its minimum/default version and SDK Build Tools 36.0.0. See Android's official compatibility notes.

## Build the signed production APK

### Android Studio
Open this folder as an Android Studio project, allow Gradle sync, then choose **Build → Generate App Bundles or APKs → Generate APK** and select the release variant if prompted.

### Command line
A machine with Android SDK/Build Tools and Gradle 9.6.0 is required. Run:

`./build_release.sh`

The script copies the release APK to:

`delivery/93_Services_Offline_App.apk`

On Windows use `BUILD_APK.bat` from a machine with the Android build toolchain.

## Distribution

Attach `93_Services_Offline_App.apk` to the email after the APK has been built. Android may show its normal security/install confirmation. No registration or account is needed.

Some email services block APK attachments; a ZIP containing the APK is the practical fallback.

## Important signing-key note

Keep `93_services_release.keystore` and `keystore.properties` securely. They are needed to produce future updates with the same signing identity.

## Current build note

The source package is complete, but the current ChatGPT execution environment does not have the Android SDK/Build Tools or an installed Gradle distribution, and outbound package downloads are unavailable here. Therefore this environment cannot truthfully claim to have produced or tested the final APK binary. The included project is set up for the one remaining build step on an Android Studio/CI machine.
