# Chennai Move — Unified Mobility Prototype
## CUMTA 24-Hour Challenge — Reimagine Chennai

### Executive Summary
**Chennai Move** is a dual-persona Android mobility application engineered for the Chennai Unified Metropolitan Transport Authority (CUMTA) 24-Hour Challenge. It personalizes urban transit for two distinct demographic groups—**Elderly Commuters** and **Young Commuters**—over a single unified multi-modal journey-planning engine spanning Chennai Metro (CMRL), Metropolitan Transport Corporation (MTC) buses, and Southern Railway Suburban EMU/MRTS networks.

---

### Key Capabilities & Personas

#### 1. Elderly Mode (முதியோர் பயன்முறை)
- **High-Contrast, Large Typography**: 18sp - 24sp body text with high-contrast palettes and minimum 48dp touch targets.
- **Minimal Cognitive Overhead**: Clean cards, zero visual clutter, and direct route badges.
- **Accessibility & Step-Free Route Prioritization**: Sorts corridors by lowest walking distance (<150m), zero transfers, and verified station lifts.
- **Voice Guidance (Android TextToSpeech)**: Read-aloud button providing spoken step-by-step instructions in English and Tamil.
- **Family / Trusted-Contact Journey Sharing**: One-tap Android Intent sharing route details, ETA, and transit mode via WhatsApp/SMS.
- **Direct Transit & Emergency Helplines**: High-contrast, un-squished quick-dial hotlines for CMRL (1860-425-1515), MTC (149), and Senior Citizen Elderline (14567).

#### 2. Youth Mode (இளைஞர் பயன்முறை)
- **Fast Multi-Modal Journey Comparison**: Real-time side-by-side comparison across CMRL Metro, MTC Buses, and Suburban EMU.
- **Smart Filters**: All Modes, Metro Only, Bus Only, Suburban Rail, Cheapest, Fastest, Fewest Transfers, Least Walking.
- **Clean Border Selection**: Subtle border & radio indicator without annoying whole-card background tinting.
- **Fare Breakdown**: Transparent fares across modes (₹5 Suburban EMU vs ₹15 MTC vs ₹20-₹40 CMRL).
- **Carbon Footprint Impact**: Shows estimated CO₂ savings (kg) vs private vehicular travel.
- **Community Disruption Feed**: Live crowdsourced transit reports (delays, maintenance, crowding) with upvote verification and in-app reporting.

#### 3. Shared Architectural Core
- **In-Place Live Search**: Search by name, locality, or corridor (ECR, OMR, Guindy, Marina, Metro, Bus) directly on the Home screen without unexpected tab switching.
- **WindowInsets & Edge-to-Edge**: Fixed status bar padding to prevent overlapping with battery/clock icons.
- **Full Bilingual Support**: Instant English <-> Tamil (தமிழ்) toggle covering all stations, lines, action buttons, and instructions.
- **Dual-Mode Switching**: Seamless toggle between Elderly Mode and Youth Mode at any moment.
- **Data Transparency & Lineage**: Clear distinctions between GTFS-sourced data, calculated headway estimates, and community crowdsourced alerts.

---

### Data Sources, Lineage & Limitations
- **Repository**: [ChennaiGTFS](https://github.com/ungalsoththu/ChennaiGTFS) (MIT License) by UngalSoththu / Chennai Open Data Community.
- **CMRL Chennai Metro**: Official published timetables (chennaimetrorail.org), Blue Line (Wimco Nagar - Airport) & Green Line (Central - St. Thomas Mount). Fares reflect official distance slabs (₹10-₹50). Verified 100% elevator access.
- **Southern Railway (Chennai Division)**: Chennai Beach to Tambaram EMU mainline & MRTS lines. Unreserved Second Class fares (₹5-₹10).
- **MTC City Buses**: Key corridors (29C, 570, 21G, 11G, 18A, 102 ECR). Low-floor electric bus designations. Highlights Tamil Nadu "Vidiyal Payanam" zero-fare policy for women commuters.
- **Reported Limitations**:
  - Live GPS vehicle feeds are not available in public static GTFS; headways (5-15 mins) and durations are computed from timetables.
  - Community disruption feed operates in crowd-verified simulation mode without fake numbers.

---

### Implementation Status
- [x] Phase 0: Workspace setup, adaptive app icon, hero branding, and Gradle configuration.
- [x] Phase 1: Shared journey-planning engine with multi-modal corridors (Metro, Suburban EMU, MTC Bus).
- [x] Phase 2: Elderly Mode with large text, high contrast, step-by-step guidance, and Android TextToSpeech.
- [x] Phase 3: Youth Mode with multi-modal comparison cards, fare calculation, CO₂ savings, and disruption feed.
- [x] Phase 4: Live Community Disruption reporting dialog with upvoting.
- [x] Phase 5: Bilingual English & Tamil toggle across all screens.
- [x] Phase 6: Trusted contact sharing via Android Intent (`ACTION_SEND`).
- [x] Phase 7: Verification via `compile_applet` and Robolectric unit tests.
- [x] Phase 8: Rapido-style Home Screen with in-place live search (ECR, OMR, Metro, Bus) without tab relocation.
- [x] Phase 9: Status bar insets fix (`statusBarsPadding`), reliable Swap button, and high-contrast helpline redesign.
