# ParchmentJAM

**ParchmentJAM** is the [JAMMER or JarAwareMapping][jammer] integration for the Parchment mappings project. This is used
in automated migration of data from one Minecraft version to another.

## Differences from jam-mcpconfig

While this project is based (and indeed has a dependency) on `jam-mcpconfig`, the official JAMMER integration for
[MCPConfig][mcpconfig], there are differences in the inputs and outputs as they've been changed in accordance with the
environment of Parchment mappings:

1. The existing identifiers input (`--existingIdentifiers`) is in
   the [`VersionedMappingDataContainer` JSON format][mdc].
2. The metadata input (for both existing versions and the input version) is a [Blackstone][blackstone] export ZIP
   (containing a file `merged.json` with the metadata information in JSON format).
3. The output AST is written as a single-file `VersionedMappingDataContainer` to the output directory, named as
   `output.json`.

Additionally, this project mostly assumes that you are passing in a singular existing version to JAMMER. While more than
one existing version can be passed to JAMMER, migration of data from all existing versions may not happen properly. For
migrating multiple versions, it is recommended to run JAMMER between each version to migrate from one version to the
immediate next.

## Usage

> **Note:** This usage only summarizes the command-line arguments used directly by Parchment itself, which are the
> inputs. For information on other command-line arguments, please consult the JAMMER project's documentation/code (in
> the `JammerRuntime` class) directly.

The main class is at `org.parchmentmc.jam.Main`. Users may avail of the `-all` classifier JAR, which has all necessary
runtime dependencies shaded in.

Broadly speaking, there are two types of data inputs: data for **existing versions**, and data for the **input version
**. For clarity, what JAMMER calls the input version we will call the **target version**.

The command-line arguments for the data inputs for versions must all be present. Particularly, the number of invocations
of each command-line argument for existing versions must match; if `--existingNames` is invoked twice, all other
arguments for existing versions must also be invoked twice.

The command-line arguments for existing versions are _order-sensitive_; for example, if two existing versions were
defined in order as `1.19.3` and `1.19.4`, then the first invocation of `--existingJars` corresponds to the 1.19.3
version, and the second to the 1.19.4 version.

| Type              | Description                                                                                                         | Existing version equivalent    | Target version equivalent |
|-------------------|---------------------------------------------------------------------------------------------------------------------|--------------------------------|---------------------------|
| Version name      | The name of the (Minecraft) version.                                                                                | `--existingNames`, `-en`       | `--inputName`, `-in`      | 
| Input JAR         | The obfuscated client-side JAR. Available at the `client` download key in the manifest.                             | `--existingJars`, `-ej`        | `--inputJar`, `-ij`       |
| Input mappings    | The client-side obfuscation mapping log (for the client-side JAR). Available at the `client_mappings` download key. | `--existingMappings`, `-em`    | `--inputMapping`, `-im`   |
| Input metadata    | The metadata information, as a Blackstone export ZIP.                                                               | `--existingMetadata`, `-emd`   | `--inputMetadata`, `-imd` |
| Input identifiers | The identifiers information for the version, as a versioned MDC JSON file.                                          | `--existingIdentifiers`, `-ei` | _Not applicable_          |

The `--outputPath` (`-o`) argument specifies the output directory for JAMMER. After running, it will contain an
`output.json` file, which is the migrated identifiers data from the existing version(s) to the target version, in the
versioned MDC JSON format.

Additionally, the `--writeStatisticsToDisk` (`-wsd`) and `--writeStatisticsToLog` (`-wsl`) arguments specify whether to
write statistics information to the disk (as `statistics.md` in the output directory) or to the log (standard output, by
default) respectively.

(Because the identifiers data does not contain a way to express it, the lambda meta information will not be written even
if `--writeLambdaMetaInformation`/`-wli` is set to `true`.)

## License

This project is licensed under the MIT License. Consult the `LICENSE.txt` file for details.

[jammer]: https://github.com/marchermans/JarAwareMapping

[mcpconfig]: https://github.com/MinecraftForge/MCPConfig

[mdc]: https://github.com/ParchmentMC/Feather/blob/main/docs/specs/MappingDataContainer.md

[blackstone]: https://github.com/ParchmentMC/Blackstone
