package org.parchmentmc.jam.blackstone;

import com.ldtteam.jam.spi.ast.metadata.IMetadataWithAccessInformation;
import org.parchmentmc.feather.metadata.WithSecurity;
import org.parchmentmc.feather.util.AccessFlag;

abstract class WithSecurityWrapper implements IMetadataWithAccessInformation {
    private final int security;

    protected WithSecurityWrapper(WithSecurity security) {
        this.security = security.getSecuritySpecification();
    }

    @Override
    public boolean isInterface() {
        return AccessFlag.INTERFACE.isActive(security);
    }

    @Override
    public boolean isAbstract() {
        return AccessFlag.ABSTRACT.isActive(security);
    }

    @Override
    public boolean isSynthetic() {
        return AccessFlag.SYNTHETIC.isActive(security);
    }

    @Override
    public boolean isAnnotation() {
        return AccessFlag.ANNOTATION.isActive(security);
    }

    @Override
    public boolean isEnum() {
        return AccessFlag.ENUM.isActive(security);
    }

    @Override
    public boolean isPackagePrivate() {
        return !(isPublic() || isProtected() || isPrivate());
    }

    @Override
    public boolean isPublic() {
        return AccessFlag.PUBLIC.isActive(security);
    }

    @Override
    public boolean isPrivate() {
        return AccessFlag.PRIVATE.isActive(security);
    }

    @Override
    public boolean isProtected() {
        return AccessFlag.PROTECTED.isActive(security);
    }

    @Override
    public boolean isStatic() {
        return AccessFlag.STATIC.isActive(security);
    }

    @Override
    public boolean isFinal() {
        return AccessFlag.FINAL.isActive(security);
    }
}
