# Chennai Move (சென்னை மூவ்)
### CUMTA 24-Hour Challenge — Reimagine Chennai

Chennai Move is a native Android application built with Kotlin and Jetpack Compose for the CUMTA 24-Hour Challenge. It delivers a personalized, unified mobility experience for both elderly citizens and young commuters across **Chennai Metro (CMRL)**, **Metropolitan Transport Corporation (MTC) buses**, and **Southern Railway Suburban EMU / MRTS**.

---

## What's New & Upgraded

### 1. 🚖 Rapido-Style Modern Homepage
- **Spacious, uncluttered layout** with generous padding and clean visual hierarchy.
- **"Where are you going?" destination search box** with quick destination chips (Guindy, CMBT Koyambedu, T. Nagar, Airport, Tambaram, Broadway).
- **Service category cards**:
  - 🚆 **CMRL Metro Express** (Fast, air-conditioned, 100% lift access, ₹10–₹50)
  - 🚌 **MTC City Bus** (Low-floor buses, Vidiyal Payanam zero-fare for women, ₹15)
  - 🚆 **Chennai Local Train** (Beach–Tambaram Suburban EMU, ₹5 economical fare)
  - 🗺️ **Official Metro Guide** (Facilities, first/last train timings, exit gates)

### 2. 📱 Phone-Based Mobile Authentication & Commuter Profile
- Mobile number login (+91) with 6-digit verification code.
- Profile management with commuter category badges (Senior Citizen 60+, Student Pass, General Commuter).
- Trusted Family Emergency Contact configuration, linked directly to the "Share Route" action.

### 3. 👴 High-Contrast Elderly Route Selection & Clarified Terminology
- **Vibrant Selection Indicator**: Replaced subtle card highlights with bold, unmistakable 3.5dp borders, warm amber fills, and a prominent `✓ CURRENT SELECTED ROUTE` badge.
- **Plain Language Transit**: Clarified "Southern Railway Suburban EMU" into **"Chennai Local Electric Train / சென்னை லோக்கல் ரயில்"** with clear explanations of low-cost tickets (₹5) and direct routes.
- **Eye-Appealing Emergency & Helpline Center**: Redesigned helpline cards with direct one-tap calling for:
  - CMRL Metro 24/7 Helpline (`1860-425-1515`)
  - MTC Bus Control Room (`149`)
  - Senior Citizen National Elderline (`14567`)

### 4. 🚇 Official Chennai Metro (CMRL) App Features
- **Station Facilities Directory**: Lifts, Escalators, 2W & 4W Parking, Restrooms, Drinking Water, and Wheelchair support across key stations (Central, Guindy, Airport, CMBT, Egmore).
- **First & Last Train Schedules**: 05:00 AM to 11:00 PM with peak 6-minute frequency.
- **Exit Gates Finder**: Direct mappings (e.g. Central Gate 1: Railway Concourse, Gate 3: GH).
- **Fare Calculator & 20% NCMC Discount**: Distance slabs (₹10 to ₹50) with 20% discount on National Common Mobility Card / Singara Chennai Card.

### 5. 🚨 Real-Life Truthful Advisories (No Fabricated Stats)
- Official notices from CMRL and MTC.
- Real commuter disruption reporting starting at 0 confirmations, verified exclusively when commuters genuinely tap "Confirm".

---

## Build & Test Commands
```bash
# Compile debug APK
gradle assembleDebug

# Run Unit & Robolectric Tests
gradle :app:testDebugUnitTest
```
