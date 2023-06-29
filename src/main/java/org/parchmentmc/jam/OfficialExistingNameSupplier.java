package org.parchmentmc.jam;

import com.ldtteam.jam.spi.asm.ClassData;
import com.ldtteam.jam.spi.asm.FieldData;
import com.ldtteam.jam.spi.asm.MethodData;
import com.ldtteam.jam.spi.asm.ParameterData;
import com.ldtteam.jam.spi.name.IExistingNameSupplier;
import net.minecraftforge.srgutils.IMappingFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

public class OfficialExistingNameSupplier implements IExistingNameSupplier {
    private final IMappingFile mojToObf;

    public OfficialExistingNameSupplier(@SuppressWarnings("unused") final Path obfuscatedToSrgFile, final Path obfuscatedToRuntimeFile) {
        final IMappingFile mojToObf;
        try (InputStream stream = Files.newInputStream(obfuscatedToRuntimeFile)) {
            mojToObf = IMappingFile.load(stream);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to load obfuscation mapping information", e);
        }

        this.mojToObf = mojToObf;
    }

    @Override
    public Optional<String> getClassName(final ClassData classData) {
        final String remapped = this.mojToObf.remapClass(classData.node().name);
        if (Objects.equals(remapped, classData.node().name)) {
            return Optional.empty();
        }

        return Optional.of(remapped);
    }

    @Override
    public Optional<String> getFieldName(final FieldData fieldData) {
        final Optional<String> remapped = Optional.ofNullable(this.mojToObf.getClass(fieldData.owner().node().name))
                .map(c -> c.remapField(fieldData.node().name));

        return remapped.filter(name -> !Objects.equals(name, fieldData.node().name));
    }

    @Override
    public Optional<String> getMethodName(final MethodData methodData) {
        final Optional<String> remapped = Optional.ofNullable(mojToObf.getClass(methodData.owner().node().name))
                .map(c -> c.remapMethod(methodData.node().name, methodData.node().desc));

        return remapped.filter(name -> !Objects.equals(name, methodData.node().name));
    }

    @Override
    public Optional<String> getParameterName(final ParameterData parameterData) {
        final Optional<String> remapped = Optional.ofNullable(mojToObf.getClass(parameterData.classOwner().node().name))
                .map(c -> c.getMethod(parameterData.owner().node().name, parameterData.owner().node().desc))
                .map(m -> m.remapParameter(parameterData.index(), parameterData.node().name));

        return remapped.filter(name -> !Objects.equals(name, parameterData.node().name));
    }
}
