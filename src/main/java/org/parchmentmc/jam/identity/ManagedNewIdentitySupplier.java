package org.parchmentmc.jam.identity;

import com.ldtteam.jam.spi.asm.ClassData;
import com.ldtteam.jam.spi.asm.FieldData;
import com.ldtteam.jam.spi.asm.MethodData;
import com.ldtteam.jam.spi.asm.ParameterData;
import com.ldtteam.jam.spi.identification.INewIdentitySupplier;

import static org.parchmentmc.jam.identity.IdentityManager.classKey;
import static org.parchmentmc.jam.identity.IdentityManager.fieldKey;
import static org.parchmentmc.jam.identity.IdentityManager.methodKey;
import static org.parchmentmc.jam.identity.IdentityManager.paramKey;

public class ManagedNewIdentitySupplier implements INewIdentitySupplier {
    private final IdentityManager identityManager;

    public ManagedNewIdentitySupplier(IdentityManager identityManager) {
        this.identityManager = identityManager;
    }

    @Override
    public int getClassIdentity(ClassData classData) {
        return identityManager.assignIdentity(classKey(classData.node().name));
    }

    @Override
    public int getFieldIdentity(FieldData fieldData) {
        return identityManager.assignIdentity(fieldKey(fieldData.owner().node().name, fieldData.node().name, fieldData.node().desc));
    }

    @Override
    public int getMethodIdentity(MethodData methodData) {
        return identityManager.assignIdentity(methodKey(methodData.owner().node().name, methodData.node().name, methodData.node().desc));
    }

    @Override
    public int getParameterIdentity(ParameterData parameterData) {
        return identityManager.assignIdentity(paramKey(parameterData.classOwner().node().name,
                parameterData.owner().node().name, parameterData.owner().node().desc,
                IdentityUtilities.calculateJVMSlot(parameterData)));
    }
}
