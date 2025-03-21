package com.github.dcysteine.nesql.server.plugin.forge;

import com.github.dcysteine.nesql.server.common.SearchResultsLayout;
import com.github.dcysteine.nesql.server.common.service.SearchService;
import com.github.dcysteine.nesql.server.common.util.ParamUtil;
import com.github.dcysteine.nesql.server.plugin.forge.display.ForgeDisplayFactory;
import com.github.dcysteine.nesql.server.plugin.forge.spec.FluidContainerSpec;
import com.github.dcysteine.nesql.sql.forge.FluidContainer;
import com.github.dcysteine.nesql.sql.forge.FluidContainerRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping(path = "/forge/fluidcontainer")
public class FluidContainerController {
    @Autowired
    private FluidContainerRepository fluidContainerRepository;

    @Autowired
    private ForgeDisplayFactory forgeDisplayFactory;

    @Autowired
    private SearchService searchService;

    // No view mapping - we redirect to Item's view.

    @GetMapping(path = "/search")
    public String search(
            @RequestParam(required = false) Optional<String> itemName,
            @RequestParam(required = false) Optional<String> itemModId,
            @RequestParam(required = false) Optional<String> fluidName,
            @RequestParam(required = false) Optional<String> fluidModId,
            @RequestParam(required = false) Optional<Integer> minAmount,
            @RequestParam(required = false) Optional<Integer> maxAmount,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) Optional<Integer> viewMode,
            Model model,
            HttpServletRequest request,
            HttpServletResponse response) {
        List<Specification<FluidContainer>> specs = new ArrayList<>();
        specs.add(ParamUtil.buildStringSpec(itemName, FluidContainerSpec::buildFilledItemNameSpec));
        specs.add(
                ParamUtil.buildStringSpec(itemModId, FluidContainerSpec::buildFilledItemModIdSpec));
        specs.add(ParamUtil.buildStringSpec(fluidName, FluidContainerSpec::buildFluidNameSpec));
        specs.add(ParamUtil.buildStringSpec(fluidModId, FluidContainerSpec::buildFluidModIdSpec));
        specs.add(ParamUtil.buildSpec(minAmount, FluidContainerSpec::buildMinAmountSpec));
        specs.add(ParamUtil.buildSpec(maxAmount, FluidContainerSpec::buildMaxAmountSpec));

        PageRequest pageRequest =
                searchService.buildPageRequest(
                        page, SearchResultsLayout.GRID, FluidContainerSpec.DEFAULT_SORT);

        List<String> tableHeaders = Arrays.asList("#", "本地化名称", "模组", "内部名称", "物品ID", "物品耐久", "操作");
        List<String> tableColumns = Arrays.asList("icon", "localizedName", "modId", "internalName", "itemId", "itemDamage", "itemActions");
        int finalViewMode = searchService.handleViewMode("fluidcontainer", viewMode, request, response, model, tableHeaders, tableColumns);

        if (finalViewMode == 0) {
            searchService.handleSearch(
                    pageRequest, model, fluidContainerRepository, Specification.allOf(specs),
                    forgeDisplayFactory::buildDisplayFluidContainerIcon);
        } else {
            searchService.handleSearch(
                    pageRequest, model, fluidContainerRepository, Specification.allOf(specs),
                    forgeDisplayFactory::buildDisplayFluidContainerWithTable);
        }
        return "plugin/forge/fluidcontainer/search";
    }
}
