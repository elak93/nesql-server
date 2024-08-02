package com.github.dcysteine.nesql.server.common.display;

import com.google.auto.value.AutoValue;
import io.micrometer.common.lang.Nullable;

@AutoValue
public abstract class DetailedItem {
    public abstract Icon getIcon();

    // 本地化名称
    public abstract String getLocalizedName();

    // 模组ID
    public abstract String getModId();

    // 内部名称
    public abstract String getInternalName();

    // 物品ID
    public abstract int getItemId();

    // 物品耐久
    @Nullable
    public abstract Integer getItemDamage();

    // Give command
    @Nullable
    public abstract String getGiveCommand();

    // Give stack command
    @Nullable
    public abstract String getGiveStackCommand();

    public static Builder builder() {
        return new AutoValue_DetailedItem.Builder();
    }

    public abstract Builder toBuilder();

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder setIcon(Icon icon);

        public abstract Builder setLocalizedName(String localizedName);

        public abstract Builder setModId(String modId);

        public abstract Builder setInternalName(String internalName);

        public abstract Builder setItemId(int itemId);

        public abstract Builder setItemDamage(@Nullable Integer itemDamage);

        public abstract Builder setGiveCommand(@Nullable String giveCommand);

        public abstract Builder setGiveStackCommand(@Nullable String giveStackCommand);

        public abstract DetailedItem build();
    }
}
