package org.parchmentmc.jam.ast;

import com.ldtteam.jam.ast.NamedASTBuilder;
import com.ldtteam.jam.ast.NamedClassBuilder;
import com.ldtteam.jam.ast.NamedClassBuilder.ClassNamingInformation;
import com.ldtteam.jam.ast.NamedFieldBuilder;
import com.ldtteam.jam.ast.NamedFieldBuilder.FieldNamingInformation;
import com.ldtteam.jam.ast.NamedMethodBuilder;
import com.ldtteam.jam.ast.NamedMethodBuilder.MethodNamingInformation;
import com.ldtteam.jam.ast.NamedParameterBuilder;
import com.ldtteam.jam.ast.NamedParameterBuilder.ParameterNamingInformation;
import com.ldtteam.jam.mcpconfig.JammerRuntime.INamedASTBuilderProducer;
import com.ldtteam.jam.mcpconfig.TSRGRemapper;
import com.ldtteam.jam.spi.ast.metadata.IMetadataAST;
import com.ldtteam.jam.spi.ast.named.builder.INamedClassBuilder;
import com.ldtteam.jam.spi.ast.named.builder.INamedFieldBuilder;
import com.ldtteam.jam.spi.ast.named.builder.INamedMethodBuilder;
import com.ldtteam.jam.spi.ast.named.builder.INamedParameterBuilder;
import com.ldtteam.jam.spi.ast.named.builder.factory.INamedASTBuilderFactory;
import com.ldtteam.jam.spi.name.INameProvider;
import com.ldtteam.jam.spi.name.INotObfuscatedFilter;
import com.ldtteam.jam.spi.name.IRemapper;

import java.nio.file.Path;

public class ExpandedNamedASTBuilder implements INamedASTBuilderProducer {
    public static final String[] DONT_OBFUSCATE_ANNOTATIONS = new String[]{
            "Lcom/mojang/blaze3d/DontObfuscate;",
            "Lnet/minecraft/obfuscate/DontObfuscate;"
    };

    @Override
    public INamedASTBuilderFactory from(Path inputMappingPath, IMetadataAST metadata) {
        return (nameByLoadedASMData, remapperByName) -> {
            final IRemapper officialToObfuscatedRemapper = TSRGRemapper.createOfficialToObfuscated(inputMappingPath, metadata);
            final IRemapper obfuscatedToOfficialRemapper = TSRGRemapper.createObfuscatedToOfficial(inputMappingPath, metadata);

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
        };
    }
}
