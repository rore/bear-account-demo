# CEL-Java 0.14.0 -> CEL-ES 0.6.1 interoperability spike (temporary branch)

A temporary public CI branch in `rore/bear-account-demo`. No changes to the default branch.

The exact corrected harness is stored in `harness.tar.xz` (SHA-256: `8f492dfc590aa172790ed66db9cf3860b363942e39391fb9b69d766f32f33d2c`). It was copied from the earlier CEL interoperability spike, with CEL-Java pinned to published `0.14.0`, manifest checks updated accordingly, and npm installation adapted to the existing package without a lock file.

Reproduce on Java 21 + Node 22 + Maven + npm:

```sh
tar -xJf harness.tar.xz
bash spike/run-real-libraries.sh
```

The script stops on the first Java compile/evaluation discrepancy or first Java->Node semantic divergence. Tests first cover Boolean arithmetic, typed protobuf field access, and conditional expressions, then the existing 25-case fixture set. It records Java and Node results and basic performance if all cases pass.

The harness uses Java as the authoritative type checker. Node verifies a signed manifest and consumes the checked protobuf, rather than relying on CEL-ES's incomplete independent type checker.

CI workflow: [CEL parity](.github/workflows/cel-interop.yml). Artifacts `cel-cross-language-results` include test output and CI log for each run.
