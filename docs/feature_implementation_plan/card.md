# Card (step-by-step, pending unless checked)
- [x] Creation flow: Select issuer then card product; app loads the correct template based on selection (single template for now). Add minimal form for open date, statement cut, status, welcome-offer progress.
- [x] Card index page
- [x] Card face display: Show built-in card face on list/detail (placeholder asset now; replace later).
- [x] Editable fields: Allow editing all fields except issuer (fixed after creation); no validation initially.
- [x] Delete behavior: Delete card and all attached data (benefits, applications, usage, notifications) with an undo snackbar.
- [x] Welcome offer UI: Show spending progress bar and deadline (reminder integration deferred).
- [x] Card management: Update card details; confirm destructive actions and refresh state.

## Card creation search experience (implementation plan)
1) State and data: extend `CardCreateViewModel` to expose a searchable list of template cards from Room (id, issuer name, product, network, segment, payment instrument, annual fee, benefit tags). Track search query, sort mode, filter selections, and derived filtered/sorted list plus counts. Persist open date as UTC while formatting/displaying in the device's local time.
2) Layout: convert the create screen to a search-first layout with a top search text field and back action; under it, show Sort and Filter chips/buttons (with active-count badges) and a small summary row (result count, clear filters). The main area is a `LazyColumn` of card results (face thumbnail when available, issuer/product title, network/segment chips, annual fee badge); include empty/loading states.
3) Sort bottom sheet: modal with radio options for Alphabetical (A->Z product), Issuer -> Product, Annual fee (low->high, high->low), and Network; persists choice and updates the list immediately.
4) Filter bottom sheet: sections for multi-select issuer, network chips, segment (personal/business), payment instrument (credit/debit/charge), annual fee slider + toggle for "no annual fee," and benefit type/category chips (credit vs multiplier, top categories). Provide Reset and Apply actions; show active filter count on the trigger chip.
5) Card selection and creation: tapping a result immediately creates the card via the importer (prefilling issuer/product/card face, defaulting open date to current date stored in UTC and status to `pending`, leaving optional fields blank) and navigates to the Card List on success with a snackbar; on failure, stay on the search screen and show an error snackbar.
6) UX polish: debounce search input, keep keyboard dismissal consistent when opening sheets, ensure focus/IME padding does not cover chips, and make list + sheet behavior resilient to long catalogs (bounded sheet heights, remembered scroll state).
7) Testing: unit tests for viewmodel filtering/sorting combinations and debounced search; UI tests for search query filtering, sort/filter sheet interactions, empty state, and selecting a card to open/save the details form.
