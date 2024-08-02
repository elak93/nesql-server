package com.github.dcysteine.nesql.server.plugin.forge.display;

import com.github.dcysteine.nesql.server.common.display.DetailedItem;
import com.github.dcysteine.nesql.server.common.display.Icon;
import com.github.dcysteine.nesql.server.common.service.DisplayService;
import com.github.dcysteine.nesql.server.common.util.MinecraftUtil;
import com.github.dcysteine.nesql.server.common.util.NumberUtil;
import com.github.dcysteine.nesql.server.plugin.base.display.item.DisplayItem;
import com.github.dcysteine.nesql.sql.forge.FluidContainer;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class DisplayFluidContainer implements Comparable<DisplayFluidContainer> {
    /** We delegate view to {@code Item}, so this method is not currently used. */
    public static DisplayFluidContainer create(
            FluidContainer fluidContainer, DisplayService service) {
        return new AutoValue_DisplayFluidContainer(
                fluidContainer, buildIcon(fluidContainer, service));
    }

    public static Icon buildIcon(FluidContainer fluidContainer, DisplayService service) {
        return DisplayItem.buildIcon(fluidContainer.getContainer(), service).toBuilder()
                .setBottomLeftImage(fluidContainer.getFluidStack().getFluid().getImageFilePath())
                .setBottomRight(
                        NumberUtil.formatCompact(fluidContainer.getFluidStack().getAmount()))
                .build();
    }

    public static DetailedItem buildTable(FluidContainer fluidContainer, DisplayService service) {
        var item = fluidContainer.getContainer();

        String giveCommand = MinecraftUtil.buildGiveCommand(item, 1);
        String giveStackCommand = item.getMaxStackSize() > 1 ?
                MinecraftUtil.buildGiveCommand(item, item.getMaxStackSize()) : null;

        return DetailedItem.builder()
                .setIcon(buildIcon(fluidContainer, service))
                .setLocalizedName(item.getLocalizedName())
                .setModId(item.getModId())
                .setInternalName(item.getInternalName())
                .setItemId(item.getItemId())
                .setItemDamage(item.getItemDamage())
                .setGiveCommand(giveCommand)
                .setGiveStackCommand(giveStackCommand)
                .build();
    }

    public abstract FluidContainer getFluidContainer();
    public abstract Icon getIcon();

    @Override
    public int compareTo(DisplayFluidContainer other) {
        return getFluidContainer().compareTo(other.getFluidContainer());
    }
}