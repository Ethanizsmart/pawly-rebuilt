# Coverage against the original 25 registrations

All descriptions below refer to code present. Runtime checks confirmed mod loading, with user visual confirmation of Fullbright, Clip Perspective and Swing Speed; other modules remain unverified. There are 24 module registrations plus shared settings/UI/HUD. Counting registrations is not a completeness percentage. Original behavior inside encrypted modules remains uncertain.

| Original module | Rewritten behavior present | Remaining work / differences |
|---|---|---|
| Spawner Protect | Player/friend proximity check, alerts, configurable command, mining reachable spawners | Full recovery/market flow absent; response speed controls aiming only |
| Netherite Finder | Incremental loaded-block scanner for debris, projected markers, alerts | No original probing/prediction engine; server-hidden blocks cannot be inferred by this scanner |
| Easy Place | Delegates held-use placement to Litematica | Dependency required; original additional tweaks not all reconstructed |
| Fast Villager Trade | Offer filtering/cycling, selection before result click, synchronized menu actions | Walk-by interaction absent; server confirmation and inventory saturation need gameplay tests |
| Schematic Builder | Selected schematic comparison, bounded scan, inventory equip, placement, optional replacement and path movement | Missing purchase/order/restock workflows, comprehensive state orientation, support planning, block-entity data and recovery journal |
| Auto Area Miner | Configured cuboid, top-down scan, bounded selection, movement/mining | Original registration was paused for development; this is a newly defined basic implementation |
| Auto Order Drop | Configured menu-title/item filter with gated whole-stack drops | Original order workflow not fully traced; no equivalence claim |
| Lag Compensation | **Not implemented** | Original menu/eating/riptide prediction helpers and server gating require further reconstruction; no placeholder module advertised as working |
| Fake Lag | Bounded outgoing gameplay-packet delay, FIFO ordering, connection checks and flush | Timing, callbacks and disable/disconnect behavior require runtime verification |
| Coord Faker | Offsets matching F3 XYZ/Block display lines | Does not replace every coordinate-bearing UI or other mods' overlays |
| Exploit Preventer | Resource-pack URL checks, optional pack rejection, distant sign-editor filtering | Original full prevention surface not reconstructed |
| Chat Macro | Single key-triggered message sequence, tick delay, input handling | Multiple named macro groups and independent bindings absent |
| Self Destruct | One-shot stop of all controllers/hooks, resumable via menu | Does not unload transformed classes or delete files; module preferences retained |
| Break Reset | Resets client mining state following successful block destruction | Original advanced mining interactions not established |
| Identity Changer | Rendered name substitution, online player's skin, local balance setting for Fake Pay | Scoreboard money/stats substitution and arbitrary offline skin retrieval absent |
| Name Display | Requests own nametag rendering in third person | Final render-state/culling behavior untested |
| Render Tooltips | Text container-content preview, curse marker | Graphical slot/map previews absent |
| Clip Perspective | Bypasses third-person camera collision distance clamp | User confirmed camera passes through walls; broader compatibility untested |
| Freelook | Independent mouse rotation and third-person camera | Exact original controls/perspective options not replicated |
| Freecam | Independent camera movement, held movement option, return on damage | Picture-in-picture Client Eyes, mining continuation and full option set absent |
| Block ESP | Configurable loaded-block scan and projected markers | Original 3D boxes/outlines and advanced filters absent |
| Region Map | Position in a 50,000-block grid | Original region names, continent assignments and media absent |
| Swing Speed | Local swing duration multiplier | User confirmed speed changes; broader compatibility untested |
| Fullbright | Raises ambient light in actual lightmap update and brightness helper | User confirmed brightness changes; shader/mod interactions and darkness behavior untested |
| Fake Pay | Intercepts configured pay command, local feedback and local balance calculation | Original sound/ping behavior absent; no real payment is sent by the intercepted command |

## Changes in this continuation

- Fixed trade selection ordering and invalidated selection when its settings change.
- Added actual tooltip callbacks for container contents and cursed enchantments.
- Corrected Fullbright to hook the lightmap update after inspecting Minecraft bytecode.
- Added Self Destruct as a one-shot stop action and halted the tick loop after stopping.
- Stopped spawner mining when a threat disappears or a screen interrupts control.
- Allowed navigation to retry after an unsuccessful path search.
- Validated mining selection coordinates before size arithmetic.
- Preserved unreadable/invalid config files by refusing to overwrite them.
- Added 12 automated tests, including static checks of all current injection selectors and shadows.

## Verification still required

First validate startup with and without Litematica, menu/settings persistence and render hooks. Then test each controller in a disposable local world, including interruption, full inventory, missing materials, unavailable chunks and competing modules. Multiplayer timing and server-specific automation require their own tests. These checks have not been performed.
