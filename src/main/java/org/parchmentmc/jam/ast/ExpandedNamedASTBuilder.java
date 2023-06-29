package org.parchmentmc.jam.ast;

import com.google.common.collect.BiMap;
import com.ldtteam.jam.ast.NamedASTBuilder;
import com.ldtteam.jam.ast.NamedClassBuilder;
import com.ldtteam.jam.ast.NamedClassBuilder.ClassNamingInformation;
import com.ldtteam.jam.ast.NamedFieldBuilder;
import com.ldtteam.jam.ast.NamedFieldBuilder.FieldNamingInformation;
import com.ldtteam.jam.ast.NamedMethodBuilder;
import com.ldtteam.jam.ast.NamedMethodBuilder.MethodNamingInformation;
import com.ldtteam.jam.ast.NamedParameterBuilder;
import com.ldtteam.jam.ast.NamedParameterBuilder.ParameterNamingInformation;
import com.ldtteam.jam.mcpconfig.TSRGNamedASTBuilder;
import com.ldtteam.jam.mcpconfig.TSRGRemapper;
import com.ldtteam.jam.spi.asm.IASMData;
import com.ldtteam.jam.spi.ast.metadata.IMetadataAST;
import com.ldtteam.jam.spi.ast.named.builder.INamedASTBuilder;
import com.ldtteam.jam.spi.ast.named.builder.INamedClassBuilder;
import com.ldtteam.jam.spi.ast.named.builder.INamedFieldBuilder;
import com.ldtteam.jam.spi.ast.named.builder.INamedMethodBuilder;
import com.ldtteam.jam.spi.ast.named.builder.INamedParameterBuilder;
import com.ldtteam.jam.spi.ast.named.builder.factory.INamedASTBuilderFactory;
import com.ldtteam.jam.spi.name.IExistingNameSupplier;
import com.ldtteam.jam.spi.name.INameProvider;
import com.ldtteam.jam.spi.name.INotObfuscatedFilter;
import com.ldtteam.jam.spi.name.IRemapper;

import java.nio.file.Path;
import java.util.Optional;

public class ExpandedNamedASTBuilder implements INamedASTBuilderFactory {
    public static final String[] DONT_OBFUSCATE_ANNOTATIONS = TSRGNamedASTBuilder.DONT_OBFUSCATE_ANNOTATIONS;

    private final IRemapper officialToObfuscatedRemapper;
    private final IRemapper obfuscatedToOfficialRemapper;

    public ExpandedNamedASTBuilder(Path inputMappingPath, IMetadataAST metadata) {
        this.officialToObfuscatedRemapper = TSRGRemapper.createOfficialToObfuscated(inputMappingPath, metadata);
        this.obfuscatedToOfficialRemapper = TSRGRemapper.createObfuscatedToOfficial(inputMappingPath, metadata);
    }

    @Override
    public INamedASTBuilder create(BiMap<IASMData, String> biMap, BiMap<String, Optional<IExistingNameSupplier>> biMap1) {
        final INameProvider<ClassNamingInformation> classNameProvider = (classNamingInfo) ->
                classNamingInfo.target().node().name;
        final INameProvider<FieldNamingInformation> fieldNameProvider = (fieldNamingInfo) ->
                fieldNamingInfo.target().node().name;
        final INameProvider<MethodNamingInformation> methodNameProvider = (methodNamingInfo) ->
                methodNamingInfo.target().node().name;
        final INameProvider<ParameterNamingInformation> parameterNameProvider = (paramNamingInfo) ->
                "$$" + paramNamingInfo.target().index();

        final INamedParameterBuilder parameterBuilder = NamedParameterBuilder.create(
                officialToObfuscatedRemapper,
                parameterNameProvider
        );

        final INamedMethodBuilder methodBuilder = new ExpandedNamedMethodBuilder(NamedMethodBuilder.create(
                officialToObfuscatedRemapper,
                obfuscatedToOfficialRemapper,
                methodNameProvider,
                parameterBuilder,
                INotObfuscatedFilter.notObfuscatedClassIfAnnotatedBy(DONT_OBFUSCATE_ANNOTATIONS),
                INotObfuscatedFilter.notObfuscatedMethodIfAnnotatedBy(DONT_OBFUSCATE_ANNOTATIONS)
        ));

        final INamedFieldBuilder fieldBuilder = new ExpandedNamedFieldBuilder(NamedFieldBuilder.create(
                officialToObfuscatedRemapper,
                fieldNameProvider,
                INotObfuscatedFilter.notObfuscatedClassIfAnnotatedBy(DONT_OBFUSCATE_ANNOTATIONS),
                INotObfuscatedFilter.notObfuscatedFieldIfAnnotatedBy(DONT_OBFUSCATE_ANNOTATIONS)
        ));

        final INamedClassBuilder classBuilder = NamedClassBuilder.create(
                officialToObfuscatedRemapper,
                classNameProvider,
                fieldBuilder,
                methodBuilder,
                INotObfuscatedFilter.notObfuscatedClassIfAnnotatedBy(DONT_OBFUSCATE_ANNOTATIONS),
                INotObfuscatedFilter.notObfuscatedMethodIfAnnotatedBy(DONT_OBFUSCATE_ANNOTATIONS)
        );

        return NamedASTBuilder.create(officialToObfuscatedRemapper, obfuscatedToOfficialRemapper, classBuilder);
    }
}
