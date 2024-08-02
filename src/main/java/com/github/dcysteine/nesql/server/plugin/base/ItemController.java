package com.github.dcysteine.nesql.server.plugin.base;

import com.github.dcysteine.nesql.server.common.SearchResultsLayout;
import com.github.dcysteine.nesql.server.common.util.ParamUtil;
import com.github.dcysteine.nesql.server.plugin.base.display.item.DisplayItem;
import com.github.dcysteine.nesql.server.plugin.base.spec.ItemSpec;
import com.github.dcysteine.nesql.server.plugin.base.display.BaseDisplayFactory;
import com.github.dcysteine.nesql.server.common.service.SearchService;
import com.github.dcysteine.nesql.sql.base.item.Item;
import com.github.dcysteine.nesql.sql.base.item.ItemRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping(path = "/base/item")
public class ItemController {
    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private BaseDisplayFactory baseDisplayFactory;

    @Autowired
    private SearchService searchService;

    @GetMapping(path = "/view/{item_id}")
    public String view(@PathVariable(name = "item_id") String id, Model model) {
        Optional<Item> itemOptional = itemRepository.findById(id);
        if (itemOptional.isEmpty()) {
            return "not_found";
        }
        Item item = itemOptional.get();
        DisplayItem displayItem = baseDisplayFactory.buildDisplayItem(item);

        model.addAttribute("item", item);
        model.addAttribute("displayItem", displayItem);
        return "plugin/base/item/view";
    }

    @GetMapping(path = "/search")
    public String search(
            @RequestParam(required = false) Optional<String> localizedName,
            @RequestParam(required = false) Optional<String> modId,
            @RequestParam(required = false) Optional<String> internalName,
            @RequestParam(required = false) Optional<Integer> itemDamage,
            @RequestParam(required = false) Optional<String> tooltip,
            @RequestParam(required = false) Optional<String> nbt,
            @RequestParam(required = false) Optional<String> itemGroupId,
            @RequestParam(required = false) Optional<String> recipeId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) Optional<Integer> viewMode,
            Model model,
            HttpServletRequest request,
            HttpServletResponse response) {
        List<Specification<Item>> specs = new ArrayList<>();
        specs.add(ParamUtil.buildStringSpec(localizedName, ItemSpec::buildLocalizedNameSpec));
        specs.add(ParamUtil.buildStringSpec(modId, ItemSpec::buildModIdSpec));
        specs.add(ParamUtil.buildStringSpec(internalName, ItemSpec::buildInternalNameSpec));
        specs.add(ParamUtil.buildSpec(itemDamage, ItemSpec::buildItemDamageSpec));
        specs.add(ParamUtil.buildStringSpec(tooltip, ItemSpec::buildTooltipSpec));
        specs.add(ParamUtil.buildStringSpec(nbt, ItemSpec::buildNbtSpec));
        specs.add(ParamUtil.buildStringSpec(itemGroupId, ItemSpec::buildItemGroupSpec));
        specs.add(ParamUtil.buildStringSpec(recipeId, ItemSpec::buildRecipeInputSpec));

        PageRequest pageRequest =
                searchService.buildPageRequest(
                        page, SearchResultsLayout.GRID, ItemSpec.DEFAULT_SORT);

        List<String> tableHeaders = Arrays.asList("#", "本地化名称", "模组", "内部名称", "物品ID", "物品耐久", "操作");
        List<String> tableColumns = Arrays.asList("icon", "localizedName", "modId", "internalName", "itemId", "itemDamage", "itemActions");
        int finalViewMode = searchService.handleViewMode("item", viewMode, request, response, model, tableHeaders, tableColumns);

        if (finalViewMode == 0) {
            searchService.handleSearch(
                    pageRequest, model, itemRepository, Specification.allOf(specs),
                    baseDisplayFactory::buildDisplayItemIcon);
        } else {
            searchService.handleSearch(
                    pageRequest, model, itemRepository, Specification.allOf(specs),
                    baseDisplayFactory::buildDisplayItemWithTable);
        }

        return "plugin/base/item/search";
    }
}
