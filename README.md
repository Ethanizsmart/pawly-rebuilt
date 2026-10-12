# Pawly Rebuilt — development checkpoint

Independent, editable Fabric client mod for Minecraft **1.21.11**, rewritten from the feature behavior visible in the supplied Pawly material. This is **not a complete restoration or a verified equivalent of Pawly 2.0.0**. See [FEATURE_COVERAGE.md](FEATURE_COVERAGE.md) before evaluating completeness.

## Build

Use JDK 21. In PowerShell on the current workstation:

```powershell
$env:JAVA_HOME = 'D:\Android Studio\jbr'
.\gradlew.bat build
```

On another machine set `JAVA_HOME` to its JDK 21 installation. The Gradle wrapper downloads its pinned distribution; dependencies need network access on a fresh machine.

Runtime artifact: `build/libs/pawly-rebuilt-0.1.0-rebuild.jar`. The `-sources.jar` is source material, not an installable mod. Install the runtime artifact in a Fabric 1.21.11 profile with Fabric Loader 0.19.3+ and Fabric API. Easy Place and Schematic Builder additionally require compatible Litematica and MaLiLib (compile pins: 0.26.16 and 0.27.20).

Open the module menu with Right Shift or `/pawly`. `/pawly stop` stops controllers and hooks; the menu can resume them. Settings are under `config/pawly-rebuilt/settings.json`. Most modules start disabled. Exploit Preventer starts enabled. The new mod ID is `pawly_rebuilt`; existing Pawly settings are not imported.

## Verification on 2026-10-12

- `compileJava --rerun-tasks test build`: exit 0; compilation, tests, JAR remapping and source JAR remapping executed.
- 12 tests passed: settings validation/preservation/round trip, FIFO delay queue, action ownership, path search, static Mixin selector/shadow/invocation inspection against pinned Minecraft class bytes.
- Static inspection does not execute the Mixin transformer or prove callback signatures, rendering, synchronization, or gameplay behavior at runtime.
- Runtime follow-up: the Modrinth MAIN profile loaded Pawly Rebuilt successfully; a read-only JDWP session observed module state transitions. The user confirmed Fullbright, Clip Perspective and Swing Speed work visually. Remaining features and automation are not verified.
- Development readiness: **PARTIALLY READY**. Buildable source checkpoint; not release ready or feature complete.

## Structure

`PawlyClient` registers modules and lifecycle callbacks; `core` owns settings, scheduling and path search; `features` owns controllers; `mixin` owns Minecraft hooks; `ui` owns the settings screens. No original Pawly JAR, encrypted payload or authentication loader is included in the artifact or required to compile.

Toolchain pins: Java 21, Gradle 9.6.1, Fabric Loom 1.17.16, Fabric Loader 0.19.3, Fabric API 0.141.6+1.21.11. Source uses Mojang names and the runtime JAR is remapped to Intermediary. JUnit 5.12.2 and ASM 9.10.1 are test dependencies, not bundled runtime modules.

## Provenance and limits

Original input remains `D:\Goc\Pawly-1.21.11 (1).jar`, SHA-256 `786f6c243d889232ae86a9ea07aa90ffac91c3836aa98b705dc532dc0b23f3d9`. Earlier source recovery remains separately in `D:\Decompile\Pawly-recovery`; this project does not replace it. Original outer JAR: 481 classes, 24 resources, including six encrypted module payloads. Previous analysis found missing referenced types and inconsistent class relationships, so a faithful full-source recovery was not established.

Research outside this distributable project records 155 decoded string tables (one further table could not be decoded), original module specifications and Minecraft API inspection. Rewritten modules are smaller and structurally different; matching module names does not establish behavioral parity. The original author’s comments, source names, build history and encrypted implementation details cannot be inferred reliably from those names alone.

Next work: reconstruct Lag Compensation from its actual helper behavior; expand the partial modules listed in the coverage table; continue the user-authorized runtime checks for the remaining modules. The tested profile also contains original Pawly, whose log reports a separate module-registration NoSuchFieldError; validation in a profile containing only the rebuild and its dependencies remains outstanding.
