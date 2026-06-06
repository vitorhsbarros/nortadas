# Nortada App — Phase 1b User Stories (Frontend)

## US014 — Mobile Project Scaffolding
*As a developer, I want to scaffold the React Native project, so that the mobile codebase has a clean, consistent structure to build on.*

**Acceptance Criteria:**
- React Native project initialised with Expo
- Package structure matches the design defined in US006
- Project committed to the repository under `/mobile` directory
- Application starts without errors locally

---

## US015 — Beach List Screen
*As a user, I want to see a list of Portuguese beaches with a clear Nortada indicator, so that I can quickly find out where the Nortada is active.*

**Acceptance Criteria:**
- Screen displays beach name, region and Nortada status (active/inactive/out of season)
- Visual indicator is clear and colour coded (e.g. green/red/grey)
- List is scrollable
- Loading and error states are handled

---

## US016 — Beach Detail Screen
*As a user, I want to tap on a beach and see its current Nortada status in detail, so that I can get more information before heading to the beach.*

**Acceptance Criteria:**
- Screen shows beach name, region, wind speed, wind direction and Nortada status
- Data is fetched from the backend API
- Loading and error states are handled
- Back navigation works correctly
