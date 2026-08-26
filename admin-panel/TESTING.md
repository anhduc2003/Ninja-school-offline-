# Offline Panel Verification Notes

## Sandbox visual check

The local panel at `http://127.0.0.1:18080` rendered the dark Ninja Control Room login screen and, after local admin authentication, rendered the grouped sidebar, dashboard metrics, status pill, a critical TCP 14444 alert and audit table. The fixture deliberately had no game process on port 14444, so the visible critical alert confirmed the dashboard's offline-state path.

## Automated checks

`npm run check` and `npm test` passed. The fixture verified password hashing, RBAC ordering, session-token hashing, six-field cron matching, SQL-backed dashboard reads, protected writes, backup creation, job approval and scheduler audit writes. It also verified authenticated read access for inventory, event points, reward history, leaderboards, analytics and maintenance views. The fixture deliberately uses a reduced `players` table, so the panel verifies column availability before it selects optional game-schema fields.

## Platform note

The local browser check ran on desktop viewport. CSS includes a mobile drawer under 760px, single-column metric/form/module grids and 44px touch targets; the Windows/Android operational steps are documented in `WINDOWS-RUNBOOK.md` and the repository README.
