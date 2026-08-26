# Offline Panel Verification Notes

## Sandbox visual check

The local panel at `http://127.0.0.1:18080` rendered the dark Ninja Control Room login screen and, after local admin authentication, rendered the grouped sidebar, dashboard metrics, status pill, a critical TCP 14444 alert and audit table. The fixture deliberately had no game process on port 14444, so the visible critical alert confirmed the dashboard's offline-state path.

The expanded desktop navigation rendered all 23 grouped modules. The inventory screen rendered its player-ID control and an explicit read-only/desynchronization warning before any player data is requested; no inventory mutation control is exposed.

After the account/item/shop-NPC extension, the local desktop panel rendered the account-creation form in the existing control-room layout. It exposed username, game-password confirmation and a confirmation-gated submit action, while account lookup remained separately available. The visible critical TCP 14444 alert remains expected in the fixture because the Java game process is intentionally not running there.

The account form was reloaded after a label correction. Its username rule now renders in Vietnamese as “3-30; chữ, số hoặc _”, and the three inputs plus confirmation-gated submit button retain their desktop layout.

The local MariaDB fixture E2E test created a game account and confirmed a `$2y$` bcrypt prefix, created an item, performed a full metadata update (type, gender, description, level, icon, part, fashion and `isUpToUp`), created a NPC store and a `store_data` row, updated it, read it through the panel API, then deleted it. Fixture records were removed after each test path.

The hardened Termux/Linux launcher passed Bash syntax checking, reinstalled production dependencies from the lockfile when its fingerprint changed, started an isolated local Node process and reached `GET /api/system/health` before declaring readiness. A separate bootstrap check temporarily removed `config.local.json`, confirmed a new local file was created from `config.properties`, then restored the fixture configuration; it did not claim a real Android-device test.

After the player-state extension, the desktop control room rendered the **Người chơi** module with an explicit offline-only warning, snapshot/audit explanation and player search entry point before any mutation control is displayed. The fixture dashboard showed audit events for the verified stats and inventory mutations; TCP 14444 remained intentionally closed because no Java game process ran in the fixture.

The player search interface was also checked with a non-matching fixture query and rendered its empty state without exposing a mutation action.

A matching fixture query rendered one offline player with a visible **Chỉnh nhân vật** action, confirming the safe edit entry point is only attached to an identified player result.

The player editor rendered the allowlisted point, skill point, EXP, slot and potential controls plus separate bag/box/equiped/fashion JSON editors. The item catalog rendered a populated type selector with per-type counts, a gender selector, search control and table results, confirming the new filtering controls are present in the desktop control room.

Bootstrap password resolver unit coverage confirms that a new panel uses the requested default password `1` only when `NSO_PANEL_ADMIN_PASSWORD` is absent; an explicit local environment override remains supported. Existing admin rows are not modified by bootstrap.

An isolated bootstrap E2E test backed up the fixture `panel_admin_users` and sessions, removed all fixture admin rows, started the panel, confirmed `admin-panel/data/first-login.txt` contained `Username: admin` and `Password: 1`, then authenticated successfully with `admin`/`1`. The script stopped the panel and restored the original fixture admin/session state and first-login file afterward.

The Termux launcher scripts pass Bash syntax checks. A local Git E2E simulated a remote fast-forward and confirmed the startup sync updated source while preserving ignored `config.properties`; it then introduced an uncommitted source change and confirmed the sync refused to overwrite it. A shallow-clone simulation also verified the fast-forward ancestry check remains valid after fetching a newer remote commit. MariaDB lifecycle behavior is validated by script review and Bash syntax in this Linux fixture; no claim is made that the Android process/Aria lock behavior was executed on the user’s device.

A MariaDB runtime E2E then used an isolated temporary datadir/socket/port. It started the hardened launcher twice and confirmed the second run reused the healthy safe/daemon PID instead of starting a second instance. It next injected a fake stale process whose command line matched the same datadir, confirmed the launcher terminated it after the bounded wait, and confirmed MariaDB started and answered `mariadb-admin ping` afterward. This validates the lifecycle logic in Linux; Android-specific process scheduling remains to be checked on the user’s Termux device.

## Automated checks

`npm run check` and `npm test` passed. The fixture verified password hashing, RBAC ordering, session-token hashing, six-field cron matching, SQL-backed dashboard reads, protected writes, backup creation, job approval and scheduler audit writes. It also verified authenticated read access for inventory, event points, reward history, leaderboards, analytics and maintenance views. The fixture deliberately uses a reduced `players` table, so the panel verifies column availability before it selects optional game-schema fields.

## Platform note

The local browser check ran on desktop viewport. CSS includes a mobile drawer under 760px, single-column metric/form/module grids and 44px touch targets; the Windows/Android operational steps are documented in `WINDOWS-RUNBOOK.md` and the repository README.
