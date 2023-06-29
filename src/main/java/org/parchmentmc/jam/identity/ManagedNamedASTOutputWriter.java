package org.parchmentmc.jam.identity;

import com.ldtteam.jam.spi.ast.named.INamedAST;
import com.ldtteam.jam.spi.ast.named.INamedClass;
import com.ldtteam.jam.spi.ast.named.INamedField;
import com.ldtteam.jam.spi.ast.named.INamedMethod;
import com.ldtteam.jam.spi.ast.named.INamedParameter;
import com.ldtteam.jam.spi.configuration.MetadataWritingConfiguration;
import com.ldtteam.jam.spi.writer.INamedASTOutputWriter;
import org.parchmentmc.feather.mapping.ImmutableVersionedMappingDataContainer;
import org.parchmentmc.feather.mapping.MappingDataBuilder;
import org.parchmentmc.feather.mapping.MappingDataBuilder.MutableClassData;
import org.parchmentmc.feather.mapping.MappingDataBuilder.MutableFieldData;
import org.parchmentmc.feather.mapping.MappingDataBuilder.MutableMethodData;
import org.parchmentmc.feather.mapping.MappingDataBuilder.MutableParameterData;
import org.parchmentmc.feather.mapping.VersionedMappingDataContainer;
import org.parchmentmc.jam.ast.ExpandedNamedFieldBuilder.ExpandedNamedField;
import org.parchmentmc.jam.ast.ExpandedNamedMethodBuilder.ExpandedNamedMethod;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class ManagedNamedASTOutputWriter implements INamedASTOutputWriter {
    private static final String OUTPUT_FILENAME = "output.json";

    private final IdentityManager identityManager;

    public ManagedNamedASTOutputWriter(IdentityManager identityManager) {
        this.identityManager = identityManager;
    }

    @Override
    public void write(Path outputDirectory, MetadataWritingConfiguration metadataWritingConfiguration, INamedAST ast) {

        final MappingDataBuilder builder = MappingDataBuilder.copyOf(identityManager.loadedData);
        builder.clearClasses();

        // We write each member to the builder, and we check if a member's ID is stored in our identity map
        // If it is in the identity map, we know it has some data, so copy it over

        for (INamedClass namedClass : ast.classes()) {
            final MutableClassData classData = builder.createClass(namedClass.identifiedName());

            if (identityManager.identityToRetainedData.get(namedClass.id()) instanceof MutableClassData retainedClassData
                    && !retainedClassData.getJavadoc().isEmpty()) {
                classData.addJavadoc(retainedClassData.getJavadoc());
            }

            // Fields
            for (INamedField namedField : namedClass.fields()) {
                if (!(namedField instanceof ExpandedNamedField expandedNamedField)) {
                    throw new RuntimeException("Expected named field with ID " + namedField.id()
                            + " to be of type ExpandedNamedField, got " + namedField);
                }
                final MutableFieldData fieldData = classData.createField(
                        namedField.identifiedName(), expandedNamedField.remappedDescriptor());

                if (identityManager.identityToRetainedData.get(namedField.id()) instanceof MutableFieldData retainedFieldData
                        && !retainedFieldData.getJavadoc().isEmpty()) {
                    fieldData.addJavadoc(retainedFieldData.getJavadoc());
                }
            }

            // Methods
            for (INamedMethod namedMethod : namedClass.methods()) {
                if (!(namedMethod instanceof ExpandedNamedMethod expandedNamedMethod)) {
                    throw new RuntimeException("Expected named method with ID " + namedMethod.id()
                            + " to be of type ExpandedNamedMethod, got " + namedMethod);
                }
                final MutableMethodData methodData = classData.createMethod(
                        expandedNamedMethod.identifiedName(), expandedNamedMethod.remappedDescriptor());

                if (identityManager.identityToRetainedData.get(namedMethod.id()) instanceof MutableMethodData retainedMethodData
                        && !retainedMethodData.getJavadoc().isEmpty()) {
                    methodData.addJavadoc(retainedMethodData.getJavadoc());
                }

                // Method Parameters
                for (INamedParameter namedParam : namedMethod.parameters()) {
                    final MutableParameterData paramData = methodData.createParameter((byte) IdentityUtilities.calculateJVMSlot(
                            (byte) namedParam.index(),
                            expandedNamedMethod.remappedDescriptor(),
                            namedMethod.isStatic()));

                    if (identityManager.identityToRetainedData.get(namedParam.id()) instanceof MutableParameterData retainedParamData
                            && (retainedParamData.getName() != null || retainedParamData.getJavadoc() != null)) {
                        paramData.setName(retainedParamData.getName())
                                .setJavadoc(retainedParamData.getJavadoc());
                    }
                }
            }
        }
        IdentityUtilities.removeUndocumented(builder);

        final VersionedMappingDataContainer mappingData = new ImmutableVersionedMappingDataContainer(
                VersionedMappingDataContainer.CURRENT_FORMAT, builder.getPackages(), builder.getClasses()
        );

        final Path outputFile = outputDirectory.resolve(OUTPUT_FILENAME);
        try (Writer writer = Files.newBufferedWriter(outputFile)) {
            IdentityUtilities.GSON.toJson(mappingData, VersionedMappingDataContainer.class, writer);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to write output to " + outputFile, e);
        }

    }
}
