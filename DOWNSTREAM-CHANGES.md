# Downstream source changes

Upstream: https://github.com/WaterdogPE/WaterdogPE

Base commit: `38081f7cfe3c7c8a34191219114b35517e2fab3d`.

This repository preserves upstream history and is an independent source copy, not an official upstream release. GitHub public forks cannot be made private, so it was first created as a private standalone repository.

## Changes (2026-10-27)

- TURN-aware UDP mux default and the `waterdog.nethernetIceMux` override.
- Builtin signaling can use per-peer UDP sockets instead of the mux socket.
- `waterdog.nethernetRelayOnly=true` selects ICE RELAY policy and UDP ports 20000–20999.
- Relay-only mode rejects missing ICE server configuration and enabled mux. A configured list must contain usable TURN credentials; runtime connectivity still needs validation.
- The dependency uses downstream Network coordinates `2.0.0.CR6-nekoy3-turn-SNAPSHOT` to avoid overwriting upstream artifacts.
- A regression test checks TURN/STUN defaults and explicit mux overrides in isolated class loaders.

The functional transport changes match the tested deployment source; downstream version labels, change notices, documentation and tests were added for publication. NXS/hybrid signaling retains the mux behavior required by the original implementation and is not the tested TURN-only configuration.

## Build

JDK 17 runs the build and compiles NetherNet. Network also uses a JDK 8 toolchain for its other modules; configure both toolchains or allow Gradle's configured resolver to provision them.

First build and install the paired Network source locally:

```sh
git clone https://github.com/nekoy3/Network-nethernet-turn.git
cd Network-nethernet-turn
./gradlew :transport-nethernet:test --tests '*SdpUtilTest' --tests '*ForwardedForTest' publishToMavenLocal -x javadoc
```

Then build this project:

```sh
git clone https://github.com/nekoy3/WaterdogPE-turn.git
cd WaterdogPE-turn
./gradlew test --tests '*NetherNetPropertiesMuxTest' --tests '*ProxyConfigTest' shadowJar
```

The repository contains source; no prebuilt shaded runtime JAR is released. Dependencies include separately licensed projects. In particular, Apache-2.0 and GPL-2.0 have compatibility constraints for combined distributions; these source copies do not grant a license exception. See [Apache's compatibility explanation](https://www.apache.org/licenses/GPL-compatibility.html).

## Configuration

Use `nethernet.signaling.mode: builtin`, configured TURN ICE URLs and `-Dwaterdog.nethernetRelayOnly=true` with this pinned implementation. Keep credentials outside the repository. The UDP mux default becomes false when `turn:` or `turns:` URLs are present. Forcing mux on is incompatible with the relay-only mode.

This repository implements application transport selection. The separate operating-system firewall rules, DNS refresh, traffic accounting and JAR hash checks described in the deployment are not installed by building this source. Re-run a native relay candidate probe using your own TURN credentials before deployment.

## License and attribution

Original source and copyright notices are preserved. The original `LICENSE` remains unchanged (GNU GPL version 2; source headers specify version 2). Downstream additions are under GPL-2.0-only. Modified files identify nekoy3 and the 2026-10-27 publication changes. Upstream authors retain their rights; this is an unofficial downstream source copy, with no upstream endorsement.

Source publication follows GPLv2 sections 1–2: retain copyright/license/warranty notices, provide the license, identify changed files and dates, and license the derivative source under the GPL. Binary distribution has additional requirements under section 3. This repository makes no blanket claim about redistribution of a combined binary with all dependencies.

## Review

Compare `upstream-base` with `codex/turn-relay-only` or `main`. The base points to the exact upstream commit above; complete available upstream history is preserved.

## Validation (2026-10-27)

15 selected tests passed with zero failures/errors.

- `dev.waterdog.waterdogpe.network.nethernet.NetherNetPropertiesMuxTest`: 6 tests
- `dev.waterdog.waterdogpe.utils.config.proxy.ProxyConfigTest`: 9 tests

`shadowJar` compiled successfully locally; the generated JAR is not uploaded.
