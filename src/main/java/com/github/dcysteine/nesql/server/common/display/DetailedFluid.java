package com.github.dcysteine.nesql.server.common.display;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class DetailedFluid {
    public abstract Icon getIcon();

    // 本地化名称
    public abstract String getLocalizedName();

    // 模组ID
    public abstract String getModId();

    // 内部名称
    public abstract String getInternalName();

    // 物品ID
    public abstract int getFluidId();

    // 是否气态
    public abstract boolean getGaseous();

    public static Builder builder() {
        return new AutoValue_DetailedFluid.Builder();
    }

    public abstract Builder toBuilder();

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder setIcon(Icon icon);

        public abstract Builder setLocalizedName(String localizedName);

        public abstract Builder setModId(String modId);

        public abstract Builder setInternalName(String internalName);

        public abstract Builder setFluidId(int fluidId);

        public abstract Builder setGaseous(boolean gaseous);

        public abstract DetailedFluid build();
    }
}
