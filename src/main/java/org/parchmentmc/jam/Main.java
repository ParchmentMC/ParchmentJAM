package org.parchmentmc.jam;

import com.ldtteam.jam.mcpconfig.JammerRuntime;
import com.ldtteam.jam.mcpconfig.TSRGMappingRuntimeConfiguration;
import com.ldtteam.jam.mcpconfig.TSRGRemapper;
import com.ldtteam.jam.mcpconfig.TSRGStatisticsWriter;
import com.ldtteam.jam.spi.configuration.MappingConfiguration;
import com.ldtteam.jam.spi.configuration.MappingRuntimeConfiguration;
import org.parchmentmc.jam.ast.ExpandedNamedASTBuilder;
import org.parchmentmc.jam.blackstone.BlackstoneMetadataASTBuilder;
import org.parchmentmc.jam.identity.IdentityManager;
import org.parchmentmc.jam.identity.ManagedExistingIdentitySupplier;
import org.parchmentmc.jam.identity.ManagedNamedASTOutputWriter;
import org.parchmentmc.jam.identity.ManagedNewIdentitySupplier;

import java.util.Locale;

public class Main {
    public static void main(String[] args) {
        // No surprises with string formatting and machine locale, thank you
        Locale.setDefault(Locale.Category.FORMAT, Locale.ROOT);

        final IdentityManager identityManager = new IdentityManager();

        final JammerRuntime runtime = new JammerRuntime(
                TSRGRemapper::createObfuscatedToOfficial,
                (existingIdentifiers, existingMappings) -> ManagedExistingIdentitySupplier.load(identityManager, existingIdentifiers),
                OfficialExistingNameSupplier::new,
                existingIdentifiers -> new ManagedNewIdentitySupplier(identityManager),
                ExpandedNamedASTBuilder::new,
                BlackstoneMetadataASTBuilder::new,
                () -> new ManagedNamedASTOutputWriter(identityManager),
                Main::createRuntimeConfiguration,
                TSRGStatisticsWriter::create
        );

        runtime.run(args);
    }

    public static MappingRuntimeConfiguration createRuntimeConfiguration(MappingConfiguration mappingConfiguration) {
        final MappingRuntimeConfiguration parent = TSRGMappingRuntimeConfiguration.create(mappingConfiguration);

        return new MappingRuntimeConfiguration(
                parent.classMapper(),
                parent.methodMapper(),
                parent.fieldMapper(),
                parent.parameterMapper()
        );
    }
}