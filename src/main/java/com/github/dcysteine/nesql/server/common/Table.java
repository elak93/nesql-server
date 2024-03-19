package com.github.dcysteine.nesql.server.common;

import com.github.dcysteine.nesql.sql.Identifiable;
import com.github.dcysteine.nesql.sql.Plugin;
import com.google.common.collect.ImmutableListMultimap;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;

/** Enum for organizing code relating to tables. */
public enum Table {
    ITEM(Plugin.BASE, "物品", "item"),
    FLUID(Plugin.BASE, "流体", "fluid"),
    ITEM_GROUP(Plugin.BASE, "物品组", "itemgroup"),
    FLUID_GROUP(Plugin.BASE, "流体组", "fluidgroup"),
    RECIPE(Plugin.BASE, "配方", "recipe"),
    RECIPE_TYPE(Plugin.BASE, "配方类型", "recipetype", "minRecipeCount", "1"),
    /** Advanced recipe search. Does not have a {@code view} page. */
    ADVANCED_RECIPE_SEARCH(Plugin.BASE, "高级配方", "advrecipe"),

    /** This table uses {@code ItemGroup}'s {@code view} page. */
    ORE_DICTIONARY(Plugin.FORGE, "矿物词典", "oredictionary"),
    // Fluid Block table is omitted.
    /** This table uses {@code Item}'s {@code view} page. */
    FLUID_CONTAINER(Plugin.FORGE, "流体容器", "fluidcontainer"),

    // GregTech Recipe table is omitted.

    ASPECT(Plugin.THAUMCRAFT, "神秘时代要素", "aspect"),
    /** This table uses {@code Item}'s {@code view} page. */
    ASPECT_ENTRY(Plugin.THAUMCRAFT, "神秘时代要素表", "aspectentry"),

    QUEST(Plugin.QUEST, "任务", "quest"),
    // Task and Reward tables are omitted.
    QUEST_LINE(Plugin.QUEST, "任务线", "questline"),
    ;

    public static final ImmutableListMultimap<Plugin, Table> TABLES =
            Arrays.stream(values())
                    .collect(
                            ImmutableListMultimap.toImmutableListMultimap(
                                    Table::getPlugin,
                                    Function.identity()));

    /** The plugin that owns this table. */
    private final Plugin plugin;

    /** Human-readable display name. */
    private final String name;

    /** URL segment for this table. */
    private final String path;

    /** Default URL parameters for search endpoint. */
    private final String[] defaultParams;

    Table(Plugin plugin, String name, String path, String... defaultParams) {
        this.plugin = plugin;
        this.name = name;
        this.path = path;
        this.defaultParams = defaultParams;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return String.format("%s/%s", plugin.getName(), path);
    }

    /** Returns the URL without the leading '~' needed by Thymeleaf. */
    public String getViewUrlNoPrefix(Identifiable<?> entity) {
        return String.format("/%s/view/%s", getPath(), entity.getId());
    }

    public String getViewUrl(Identifiable<?> entity) {
        return "~" + getViewUrlNoPrefix(entity);
    }

    public String getSearchUrl() {
        return getSearchUrl(defaultParams);
    }

    public String getSearchUrl(Map<String, String> params) {
        UriComponentsBuilder builder =
                UriComponentsBuilder.fromUriString(String.format("~/%s/search", getPath()));
        params.forEach(builder::queryParam);
        return builder.toUriString();
    }

    public String getSearchUrl(String... params) {
        if (params.length % 2 != 0) {
            throw new IllegalArgumentException(
                    "params must have even length!\n" + Arrays.toString(params));
        }

        UriComponentsBuilder builder =
                UriComponentsBuilder.fromUriString(String.format("~/%s/search", getPath()));
        for (int i = 0; i < params.length; i += 2) {
            builder.queryParam(params[i], params[i + 1]);
        }
        return builder.toUriString();
    }
}
