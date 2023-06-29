package org.parchmentmc.jam.blackstone;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ldtteam.jam.spi.ast.metadata.IMetadataAST;
import com.ldtteam.jam.spi.metadata.IMetadataASTBuilder;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.parchmentmc.feather.io.gson.SimpleVersionAdapter;
import org.parchmentmc.feather.io.gson.metadata.MetadataAdapterFactory;
import org.parchmentmc.feather.metadata.SourceMetadata;
import org.parchmentmc.feather.named.Named;
import org.parchmentmc.feather.util.SimpleVersion;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

public class BlackstoneMetadataASTBuilder implements IMetadataASTBuilder {
    private static final String JSON_DATA_FILE_NAME = "merged.json";

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(SimpleVersion.class, new SimpleVersionAdapter())
            .registerTypeAdapterFactory(new MetadataAdapterFactory())
            .create();
    private final Path inputPath;
    @MonotonicNonNull
    private SourceMetadataWrapper metadata;

    public BlackstoneMetadataASTBuilder(final Path inputPath) {
        this.inputPath = inputPath;
    }

    @Override
    public IMetadataAST ast() {
        if (metadata == null) {
            metadata = loadMetadata(inputPath);
        }
        return metadata;
    }

    private static SourceMetadataWrapper loadMetadata(final Path inputPath) {
        try (FileSystem fileSystem = FileSystems.newFileSystem(inputPath)) {

            final Path jsonDataPath = fileSystem.getPath(JSON_DATA_FILE_NAME);
            if (!Files.exists(jsonDataPath)) {
                throw new IOException("Metadata file " + JSON_DATA_FILE_NAME + " is missing");
            }

            try (Reader reader = Files.newBufferedReader(jsonDataPath)) {
                return new SourceMetadataWrapper(GSON.fromJson(reader, SourceMetadata.class), BlackstoneMetadataASTBuilder::extractMojangName);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read Blackstone metadata from " + inputPath, e);
        }
    }

    private static @Nullable String extractMojangName(Named name) {
        return name.getObfuscatedName().orElse(null);
    }
}
