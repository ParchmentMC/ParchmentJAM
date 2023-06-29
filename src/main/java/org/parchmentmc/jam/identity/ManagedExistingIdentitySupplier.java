package org.parchmentmc.jam.identity;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.ldtteam.jam.spi.asm.ClassData;
import com.ldtteam.jam.spi.asm.FieldData;
import com.ldtteam.jam.spi.asm.MethodData;
import com.ldtteam.jam.spi.asm.ParameterData;
import com.ldtteam.jam.spi.identification.IExistingIdentitySupplier;
import org.parchmentmc.feather.mapping.MappingDataBuilder.MutableClassData;
import org.parchmentmc.feather.mapping.MappingDataBuilder.MutableFieldData;
import org.parchmentmc.feather.mapping.MappingDataBuilder.MutableMethodData;
import org.parchmentmc.feather.mapping.MappingDataBuilder.MutableParameterData;
import org.parchmentmc.feather.mapping.VersionedMappingDataContainer;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.parchmentmc.jam.identity.IdentityManager.classKey;
import static org.parchmentmc.jam.identity.IdentityManager.fieldKey;
import static org.parchmentmc.jam.identity.IdentityManager.methodKey;
import static org.parchmentmc.jam.identity.IdentityManager.paramKey;

public class ManagedExistingIdentitySupplier implements IExistingIdentitySupplier {
    private final IdentityManager identityManager;
    private final BiMap<String, Integer> localIdentities = HashBiMap.create(1_000);

    public ManagedExistingIdentitySupplier(IdentityManager identityManager) {
        this.identityManager = identityManager;
    }

    public static IExistingIdentitySupplier load(IdentityManager identityManager, Path existingIdentifierPath) {
        final VersionedMappingDataContainer mappingData;
        try (Reader reader = Files.newBufferedReader(existingIdentifierPath)) {
            mappingData = IdentityUtilities.GSON.fromJson(reader, VersionedMappingDataContainer.class);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read mapping data container from " + existingIdentifierPath, e);
        }

        final ManagedExistingIdentitySupplier identitySupplier = new ManagedExistingIdentitySupplier(identityManager);

        // We don't assign identities to packages but we do copy them
        mappingData.getPackages().forEach(pkg -> identityManager.loadedData.createPackage(pkg.getName())
                .addJavadoc(pkg.getJavadoc()));

        // Copy classes
        mappingData.getClasses().forEach(cls -> {
            final MutableClassData classData = identityManager.loadedData.createClass(cls.getName())
                    .addJavadoc(cls.getJavadoc());
            identitySupplier.assignIdentityToData(classData, classKey(classData.getName()));

            // Copy fields
            cls.getFields().forEach(field -> {
                final MutableFieldData fieldData = classData.createField(field.getName(), field.getDescriptor())
                        .addJavadoc(field.getJavadoc());
                identitySupplier.assignIdentityToData(fieldData, fieldKey(classData.getName(), fieldData.getName(), fieldData.getDescriptor()));
            });

            // Copy methods
            cls.getMethods().forEach(method -> {
                final MutableMethodData methodData = classData.createMethod(method.getName(), method.getDescriptor())
                        .addJavadoc(method.getJavadoc());
                identitySupplier.assignIdentityToData(methodData, methodKey(classData.getName(), methodData.getName(), methodData.getDescriptor()));

                // Copy parameters
                method.getParameters().forEach(param -> {
                    final MutableParameterData paramData = methodData.createParameter(param.getIndex())
                            .setName(param.getName())
                            .setJavadoc(param.getJavadoc());
                    identitySupplier.assignIdentityToData(paramData, paramKey(classData.getName(), methodData.getName(), methodData.getDescriptor(), param.getIndex()));
                });
            });
        });

        return identitySupplier;
    }

    private void assignIdentityToData(Object data, String key) {
        localIdentities.put(key, identityManager.assignIdentityToData(data, key));
    }

    private Integer getOrAssignIdentity(String key) {
        return localIdentities.computeIfAbsent(key, k -> identityManager.getOrAssignIdentity(key));
    }

    @Override
    public int getClassIdentity(ClassData classData) {
        return getOrAssignIdentity(classKey(classData.node().name));
    }

    @Override
    public int getFieldIdentity(FieldData fieldData) {
        return getOrAssignIdentity(fieldKey(fieldData.owner().node().name, fieldData.node().name, fieldData.node().desc));
    }

    @Override
    public int getMethodIdentity(MethodData methodData) {
        return getOrAssignIdentity(methodKey(methodData.owner().node().name, methodData.node().name, methodData.node().desc));
    }

    @Override
    public int getParameterIdentity(ParameterData parameterData) {
        return getOrAssignIdentity(paramKey(parameterData.classOwner().node().name,
                parameterData.owner().node().name, parameterData.owner().node().desc,
                IdentityUtilities.calculateJVMSlot(parameterData)));
    }
}
