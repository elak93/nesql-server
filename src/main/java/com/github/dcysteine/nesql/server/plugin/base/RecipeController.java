package com.github.dcysteine.nesql.server.plugin.base;

import com.github.dcysteine.nesql.server.common.SearchResultsLayout;
import com.github.dcysteine.nesql.server.common.robot.RecipeSearchHelper;
import com.github.dcysteine.nesql.server.common.util.ParamUtil;
import com.github.dcysteine.nesql.server.plugin.base.display.recipe.DisplayRecipe;
import com.github.dcysteine.nesql.server.plugin.base.display.BaseDisplayFactory;
import com.github.dcysteine.nesql.server.plugin.base.spec.RecipeSpec;
import com.github.dcysteine.nesql.server.common.service.SearchService;
import com.github.dcysteine.nesql.sql.base.recipe.Recipe;
import com.github.dcysteine.nesql.sql.base.recipe.RecipeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;

@Controller
@RequestMapping(path = "/base/recipe")
public class RecipeController {
    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private BaseDisplayFactory baseDisplayFactory;

    @Autowired
    private SearchService searchService;

    @GetMapping(path = "/view/{recipe_id}")
    public String view(@PathVariable(name = "recipe_id") String id, Model model) {
        Optional<Recipe> recipeOptional = recipeRepository.findById(id);
        if (recipeOptional.isEmpty()) {
            return "not_found";
        }
        Recipe recipe = recipeOptional.get();
        DisplayRecipe displayRecipe = baseDisplayFactory.buildDisplayRecipe(recipe);

        model.addAttribute("recipe", recipe);
        model.addAttribute("displayRecipe", displayRecipe);
        return "plugin/base/recipe/view";
    }

    @GetMapping(path = "/search")
    public String search(
            @RequestParam(required = false) Optional<String> recipeCategory,
            @RequestParam(required = false) Optional<String> recipeType,
            @RequestParam(required = false) Optional<String> recipeTypeId,
            @RequestParam(required = false) Optional<String> inputItemName,
            @RequestParam(required = false) Optional<String> inputItemModId,
            @RequestParam(required = false) Optional<String> inputItemId,
            @RequestParam(required = false) Optional<String> inputItemGroupId,
            @RequestParam(required = false) Optional<String> inputFluidName,
            @RequestParam(required = false) Optional<String> inputFluidModId,
            @RequestParam(required = false) Optional<String> inputFluidId,
            @RequestParam(required = false) Optional<String> inputFluidGroupId,
            @RequestParam(required = false) Optional<String> outputItemName,
            @RequestParam(required = false) Optional<String> outputItemModId,
            @RequestParam(required = false) Optional<String> outputItemId,
            @RequestParam(required = false) Optional<String> outputFluidName,
            @RequestParam(required = false) Optional<String> outputFluidModId,
            @RequestParam(required = false) Optional<String> outputFluidId,
            @RequestParam(defaultValue = "1") int page,
            Model model) {
        List<Specification<Recipe>> specs = new ArrayList<>();
        specs.add(ParamUtil.buildStringSpec(recipeCategory, RecipeSpec::buildRecipeCategorySpec));
        specs.add(ParamUtil.buildStringSpec(recipeType, RecipeSpec::buildRecipeTypeSpec));
        specs.add(ParamUtil.buildStringSpec(recipeTypeId, RecipeSpec::buildRecipeTypeIdSpec));
        specs.add(ParamUtil.buildStringSpec(inputItemName, RecipeSpec::buildInputItemNameSpec));
        specs.add(ParamUtil.buildStringSpec(inputItemModId, RecipeSpec::buildInputItemModIdSpec));
        specs.add(ParamUtil.buildStringSpec(inputItemId, RecipeSpec::buildInputItemIdSpec));
        specs.add(
                ParamUtil.buildStringSpec(inputItemGroupId, RecipeSpec::buildInputItemGroupIdSpec));
        specs.add(ParamUtil.buildStringSpec(inputFluidName, RecipeSpec::buildInputFluidNameSpec));
        specs.add(ParamUtil.buildStringSpec(inputFluidModId, RecipeSpec::buildInputFluidModIdSpec));
        specs.add(ParamUtil.buildStringSpec(inputFluidId, RecipeSpec::buildInputFluidIdSpec));
        specs.add(
                ParamUtil.buildStringSpec(
                        inputFluidGroupId, RecipeSpec::buildInputFluidGroupIdSpec));
        specs.add(ParamUtil.buildStringSpec(outputItemName, RecipeSpec::buildOutputItemNameSpec));
        specs.add(ParamUtil.buildStringSpec(outputItemModId, RecipeSpec::buildOutputItemModIdSpec));
        specs.add(ParamUtil.buildStringSpec(outputItemId, RecipeSpec::buildOutputItemIdSpec));
        specs.add(ParamUtil.buildStringSpec(outputFluidName, RecipeSpec::buildOutputFluidNameSpec));
        specs.add(
                ParamUtil.buildStringSpec(outputFluidModId, RecipeSpec::buildOutputFluidModIdSpec));
        specs.add(ParamUtil.buildStringSpec(outputFluidId, RecipeSpec::buildOutputFluidIdSpec));

        // TODO see if we can re-add sorting.
        //  Will probably require rewriting the queries to not use Specification.
        PageRequest pageRequest = searchService.buildPageRequest(page, SearchResultsLayout.RECIPE);
        searchService.handleSearch(
                pageRequest, model, recipeRepository, Specification.allOf(specs),
                baseDisplayFactory::buildDisplayRecipe);
        return "plugin/base/recipe/search";
    }
}
