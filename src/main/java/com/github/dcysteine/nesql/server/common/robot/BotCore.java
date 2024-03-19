package com.github.dcysteine.nesql.server.common.robot;

import com.github.dcysteine.nesql.server.plugin.gregtech.GregTechRecipeSpec;
import com.github.dcysteine.nesql.sql.base.fluid.Fluid;
import com.github.dcysteine.nesql.sql.base.fluid.FluidGroup;
import com.github.dcysteine.nesql.sql.base.fluid.FluidStack;
import com.github.dcysteine.nesql.sql.base.fluid.FluidStackWithProbability;
import com.github.dcysteine.nesql.sql.base.item.Item;
import com.github.dcysteine.nesql.sql.base.item.ItemGroup;
import com.github.dcysteine.nesql.sql.base.item.ItemStack;
import com.github.dcysteine.nesql.sql.base.item.ItemStackWithProbability;
import com.github.dcysteine.nesql.sql.base.recipe.Recipe;
import com.github.dcysteine.nesql.sql.gregtech.GregTechRecipe;
import com.google.common.collect.ImmutableList;
import jakarta.annotation.PostConstruct;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.BotFactory;
import net.mamoe.mirai.contact.ContactList;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.Listener;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.message.data.QuoteReply;
import net.mamoe.mirai.utils.BotConfiguration;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import xyz.cssxsh.mirai.tool.FixProtocolVersion;

import java.io.File;
import java.sql.Time;
import java.util.List;
import java.util.Optional;
import java.util.Timer;
//解除此注解启用机器人功能,记得先配置mirai
//@Component
public class BotCore {
    @Autowired
    private RecipeSearchHelper recipeSearchHelper;
    @Autowired
    private ItemSearchHelper itemSearchHelper;
    private Bot bot;
    public static BotCore botCore;
    private long time=0;

    public BotCore() {
        bot = BotFactory.INSTANCE.newBot(1234564789, "QQPassword", new BotConfiguration() {
            {
                setHeartbeatStrategy(BotConfiguration.HeartbeatStrategy.STAT_HB);
                setProtocol(MiraiProtocol.ANDROID_PHONE);
                setWorkingDir(new File("/mirai"));
                fileBasedDeviceInfo("device.json");
            }
        });
        FixProtocolVersion.load(BotConfiguration.MiraiProtocol.ANDROID_PHONE, new File("/mirai/8.9.63.json"));
        bot.login();
        long activeGroup=708138668;
        bot.getGroup(activeGroup).sendMessage("GTNH QQ小助手v1.14514已启动！发送“银月酱”及“帮助”了解食用方法");
        bot.getEventChannel().subscribeAlways(GroupMessageEvent.class, event -> {
            if (event.getSubject().getId() != activeGroup) return;
            String message = event.getMessage().contentToString();
            if (!message.contains("银月")) return;
            if (System.currentTimeMillis() - time < 5000) return;
            time = System.currentTimeMillis();
            if (message.contains("配方")) {
                MessageChainBuilder chain = new MessageChainBuilder()
                        .append(new QuoteReply(event.getMessage()));
                long usedTime = System.currentTimeMillis();
                if(recipeSearchHelper.buildSearchSpecs(message)==null){
                    chain.append("查询配方可使用以下关键词：" +
                            "[银月]查个[配方]，[精准匹配]输入物品和输出物品，[配方类型]是工业高炉 " +
                            "[输入物品]是铁粉,[输入流体]是氧气，[输出物品]是钢锭\n当前数据库版本:2.6.0");
                    event.getSubject().sendMessage(chain.build());
                    return;
                }
                List<Recipe> recipes = recipeSearchHelper.searchRecipes(message);
                usedTime = (System.currentTimeMillis() - usedTime);
                if (recipes.isEmpty()) {
                    chain.append("诶呀，没查询到有产出这个的配方！");
                    event.getSubject().sendMessage(chain.build());
                    return;
                } else {
                    chain.append("成功找到了").append(String.valueOf(recipes.size())).append("个配方，耗时")
                            .append(String.format("%.2f", (double) usedTime / 1000.0)).append("秒！").append(recipes.size()!=1?
                                            "以下仅显示第一个:":"");
                    chain.append(recipeToMessage(recipes.get(0)));
                }
                event.getSubject().sendMessage(chain.build());
            }
            if(message.contains("查询物品")){
                MessageChainBuilder chain = new MessageChainBuilder()
                        .append(new QuoteReply(event.getMessage()));
                long usedTime = System.currentTimeMillis();
                if(itemSearchHelper.buildSearchSpecs(message)==null){
                    chain.append("查询物品可使用以下关键词：" +
                            "[银月][查询物品]，[物品名]是工业高炉，[模组]是gregtech“\n当前数据库版本:2.6.0");
                    event.getSubject().sendMessage(chain.build());
                    return;
                }
                List<Item> item = itemSearchHelper.searchItem(message);
                usedTime = (System.currentTimeMillis() - usedTime);
                if (item.isEmpty()) {
                    chain.append("诶呀，没查询到有这个物品！");
                    event.getSubject().sendMessage(chain.build());
                    return;
                } else {
                    chain.append("成功找到了").append(String.valueOf(item.size())).append("个物品，耗时")
                            .append(String.format("%.2f", (double) usedTime / 1000.0)).append("秒！").append(item.size()!=1?
                                    "以下仅显示第一个:":"");
                    chain.append(itemToMessage(item.get(0)));
                }
                event.getSubject().sendMessage(chain.build());
            }
            if (message.contains("帮助")){
                event.getSubject().sendMessage("""
                        GTNH QQ小助手v1.14514
                        目前功能列表：
                        1.查询配方-发送”银月“+”配方”即可查询NEI配方“
                        2.查询物品-发送”银月“+”查询物品“即可查询物品""");
            }
        });
    }

    public String recipeToMessage(Recipe recipe) {
        StringBuilder recipes = new StringBuilder();
        recipes.append("\n配方类型：").append(recipe.getRecipeType().getType());
        GregTechRecipe gtRecipe=recipeSearchHelper.getGTRecipe(recipe);
        if(gtRecipe!=null) {
            recipes.append("\n电压：").append(gtRecipe.getVoltage()).append(" eu/t");
            recipes.append("\n电流：").append(gtRecipe.getAmperage()).append(" A");
            recipes.append("\n时间：").append(gtRecipe.getDuration()).append(" tick (").append(gtRecipe.getDuration()/20).append("s)");
        }
        if (!recipe.getItemInputs().isEmpty()) {
            recipes.append("\n输入物品：");
            for (ItemGroup item : recipe.getItemInputs().values()) {
                ItemStack itemStack = item.getItemStacks().stream().toList().get(0);
                recipes.append(itemStack.getItem().getLocalizedName()).append("(").append(itemStack.getStackSize()).append(") ");
            }
        }
        if (!recipe.getFluidInputs().isEmpty()) {
            recipes.append("\n输入流体：");
            for (FluidGroup fluid : recipe.getFluidInputs().values()) {
                FluidStack fluidStack = fluid.getFluidStacks().stream().toList().get(0);
                recipes.append(fluidStack.getFluid().getLocalizedName()).append("(").append(fluidStack.getAmount()).append("mB) ");
            }
        }
        if (!recipe.getItemOutputs().isEmpty()) {
            recipes.append("\n输出物品：");
            for (ItemStackWithProbability item : recipe.getItemOutputs().values()) {
                recipes.append(item.getItem().getLocalizedName()).append("(").append(item.getStackSize()).append(") ");
            }
        }
        if (!recipe.getFluidOutputs().isEmpty()) {
            recipes.append("\n输出流体：");
            for (FluidStackWithProbability fluid : recipe.getFluidOutputs().values()) {
                recipes.append(fluid.getFluid().getLocalizedName()).append("(").append(fluid.getAmount()).append("mB) ");
            }
        }
        return recipes.toString();
    }
    public String itemToMessage(Item item) {
        StringBuilder items = new StringBuilder();
        items.append("\n物品名：").append(item.getLocalizedName())
                .append("\n物品模组：").append(item.getModId())
                .append("\n描述：").append(item.getTooltip());
        return items.toString();
    }
}
