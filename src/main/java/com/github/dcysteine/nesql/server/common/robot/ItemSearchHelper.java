package com.github.dcysteine.nesql.server.common.robot;

import com.github.dcysteine.nesql.server.common.util.ParamUtil;
import com.github.dcysteine.nesql.server.plugin.base.spec.ItemSpec;
import com.github.dcysteine.nesql.server.plugin.base.spec.RecipeSpec;
import com.github.dcysteine.nesql.sql.base.item.Item;
import com.github.dcysteine.nesql.sql.base.item.ItemRepository;
import com.github.dcysteine.nesql.sql.base.item.ItemStack;
import com.github.dcysteine.nesql.sql.base.recipe.Recipe;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ItemSearchHelper {
    @Autowired
    private ItemRepository itemRepository;
    public List<Item> searchItem(String message){
        List<Item> items = itemRepository.findAll(buildSearchSpecs(message));
        return items;
    }
    public Specification<Item> buildSearchSpecs(String message) {
        List<Specification<Item>> specs = new ArrayList<>();
        Pattern pattern = Pattern.compile("([^，,\s]+)是([^，,\s]+)");
        Matcher matcher = pattern.matcher(message);
        if(!matcher.find())return null;
        matcher.reset();
        while (matcher.find()) {
            String attribute = matcher.group(1).trim();
            Optional<String> value = Optional.of(matcher.group(2).trim());
            switch (attribute) {
                case "物品名"-> specs.add(ParamUtil.buildStringSpec(value, ItemSpec::buildLocalizedNameSpec));
                case "模组"-> specs.add(ParamUtil.buildStringSpec(value, ItemSpec::buildModIdSpec));
            }
        }
        return Specification.allOf(specs);
    }
}
