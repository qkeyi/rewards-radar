# Benefit (pending unless checked)
- [x] Benefit management: Add/update/remove benefits on a card; confirm destructive actions and refresh state.
- [x] Benefit types/properties: Support two types:
  - Credit: fixed amount; refresh cadence (once/monthly/quarterly/annual); category/merchant; notes/terms. One-time credits use a checkbox; multi-use credits store each credit amount used.
  - Multiplier: decimal rate (e.g., 0.05 for 5%/5x), optional cap on spend only; refresh cadence; category/merchant; notes/terms.
- [x] Categories: Store issuer-scoped categories (e.g., "Dining (Chase)", "Online Shopping (Amex)") from a selectable list of common types (Dining, Grocery, Online Shopping, Travel, Gas, Drugstore, Streaming, Transit, Utilities) with ability to add custom types; support multi-select per benefit and only show categories for the benefit's issuer.
- [x] Usage tracking UI: Multiplier tracks spend amount and derived rewards per transaction; credits track checkbox for single-use or per-use amounts for multi-use; history with `MM/dd/yyyy hh:mm` UTC stored, local display.
