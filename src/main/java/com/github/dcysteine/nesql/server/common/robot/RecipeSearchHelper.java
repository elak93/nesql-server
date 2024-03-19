package com.github.dcysteine.nesql.server.common.robot;

import com.github.dcysteine.nesql.server.common.util.ParamUtil;
import com.github.dcysteine.nesql.server.plugin.base.spec.RecipeSpec;
import com.github.dcysteine.nesql.server.plugin.gregtech.GregTechRecipeSpec;
import com.github.dcysteine.nesql.sql.base.fluid.Fluid;
import com.github.dcysteine.nesql.sql.base.fluid.FluidStackWithProbability;
import com.github.dcysteine.nesql.sql.base.item.Item;
import com.github.dcysteine.nesql.sql.base.item.ItemGroup;
import com.github.dcysteine.nesql.sql.base.item.ItemStack;
import com.github.dcysteine.nesql.sql.base.item.ItemStackWithProbability;
import com.github.dcysteine.nesql.sql.base.recipe.Recipe;
import com.github.dcysteine.nesql.sql.base.recipe.RecipeRepository;
import com.github.dcysteine.nesql.sql.gregtech.GregTechRecipe;
import com.github.dcysteine.nesql.sql.gregtech.GregTechRecipeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class RecipeSearchHelper {
    @Autowired
    private GregTechRecipeRepository gregTechRecipeRepository;
    @Autowired
    private RecipeRepository recipeRepository;
    public List<Recipe> searchRecipes(String message) {
        List<Recipe> recipes = recipeRepository.findAll(buildSearchSpecs(message));
        Pattern pattern1 = Pattern.compile("精准匹配([^，,\s]+)");
        Matcher matcher1 = pattern1.matcher(message);
        if (!matcher1.find()) return recipes;
        String precise = matcher1.group(1).trim();
        String[] preciseArray=precise.split("和");
        for(String preciseString:preciseArray) {
            Pattern pattern2 = Pattern.compile(preciseString+"是([^，,\s]+)");
            Matcher matcher2 = pattern2.matcher(message);
            if (!matcher2.find()) return recipes;
            String ID = matcher2.group(1).trim();
            for (Iterator<Recipe> iterator = recipes.iterator(); iterator.hasNext(); ) {
                Recipe recipe = iterator.next();
                switch (preciseString){
                    case "输入物品"-> {
                        for (Item item : recipe.getItemInputsItems()) {
                            if (item.getLocalizedName().contains(ID) && !item.getLocalizedName().equals(ID)) {
                                iterator.remove();
                            }
                        }
                    }
                    case "输入流体"-> {
                        for (Fluid fluid : recipe.getFluidInputsFluids()) {
                            if (fluid.getLocalizedName().contains(ID) && !fluid.getLocalizedName().equals(ID)) {
                                iterator.remove();
                            }
                        }
                    }
                    case "输出物品"-> {
                        for (ItemStackWithProbability item : recipe.getItemOutputs().values()) {
                            if (item.getItem().getLocalizedName().contains(ID) && !item.getItem().getLocalizedName().equals(ID)) {
                                iterator.remove();
                            }
                        }
                    }
                    case "输出流体"->{
                        for (FluidStackWithProbability fluid : recipe.getFluidOutputs().values()) {
                            if (fluid.getFluid().getLocalizedName().contains(ID) && !fluid.getFluid().getLocalizedName().equals(ID)) {
                                iterator.remove();
                            }
                        }
                    }
                }
            }
        }
        return recipes;
    }
    public GregTechRecipe getGTRecipe(Recipe recipe) {
        Optional<GregTechRecipe> gregTechRecipeOptional =
                gregTechRecipeRepository.findOne(
                        GregTechRecipeSpec.buildRecipeIdSpec(recipe.getId()));
        if (gregTechRecipeOptional.isEmpty()) {
            return null;
        } else {
            return gregTechRecipeOptional.get();
        }
    }

    public Specification<Recipe> buildSearchSpecs(String message) {
        List<Specification<Recipe>> specs = new ArrayList<>();
        Pattern pattern = Pattern.compile("([^，,\s]+)是([^，,\s]+)");
        Matcher matcher = pattern.matcher(message);
        if(!matcher.find())return null;
        matcher.reset();
        while (matcher.find()) {
            String attribute = matcher.group(1).trim();
            Optional<String> value = Optional.of(matcher.group(2).trim());
            switch (attribute) {
                case "输入物品"-> specs.add(ParamUtil.buildStringSpec(value, RecipeSpec::buildInputItemNameSpec));
                case "输入流体"-> specs.add(ParamUtil.buildStringSpec(value, RecipeSpec::buildInputFluidNameSpec));
                case "输出物品"-> specs.add(ParamUtil.buildStringSpec(value, RecipeSpec::buildOutputItemNameSpec));
                case "输出流体"-> specs.add(ParamUtil.buildStringSpec(value, RecipeSpec::buildOutputFluidNameSpec));
                case "配方类型"-> specs.add(ParamUtil.buildStringSpec(value, RecipeSpec::buildRecipeTypeSpec));
            }
        }
        return Specification.allOf(specs);
    }
}
