package net.darkhax.itemstages.crt;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Predicate;

import org.openzen.zencode.java.ZenCodeType;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.item.IIngredient;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.zencode.impl.util.PositionUtil;
import com.blamejared.crafttweaker.impl.item.MCItemStack;

import net.darkhax.itemstages.Restriction;
import net.darkhax.itemstages.RestrictionManager;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.common.ToolType;

@ZenRegister
@ZenCodeType.Name("mods.itemstages.ItemStages")
public class ZenItemStages {
    
    @ZenCodeType.Method
    public static Restriction restrict (ToolType type, String... requiredStages) {
        
        return restrictInternal(stack -> stack.getItem().getToolTypes(stack).contains(type), requiredStages);
    }
    
    @ZenCodeType.Method
    public static Restriction restrict (Enchantment enchantment, String... requiredStages) {
        
        return restrict(enchantment, true, true, requiredStages);
    }
    
    @ZenCodeType.Method
    public static Restriction restrict (Enchantment enchantment, boolean checkBook, boolean checkItem, String... requiredStages) {
        
        return restrictInternal(stack -> hasEnchantment(stack, enchantment, checkBook, checkItem), requiredStages);
    }
    
    @ZenCodeType.Method
    public static Restriction createModRestriction (String modid, String... requiredStages) {
        
        return restrictInternal(stack -> modid.equalsIgnoreCase(stack.getItem().getRegistryName().getNamespace()), requiredStages);
    }
    
    @ZenCodeType.Method
    public static Restriction restrict (Rarity rarity, String... requiredStages) {
        
        return restrictInternal(stack -> stack.getRarity() == rarity, requiredStages);
    }
    
    @ZenCodeType.Method
    public static Restriction restrict (IIngredient ingredient, String... requiredStages) {
        
        return restrictInternal(ingredient.asVanillaIngredient(), requiredStages);
    }
    
    @ZenCodeType.Method
    public static Restriction restrict (Predicate<IItemStack> predicate, String... requiredStages) {
        
        return restrictInternal(stack -> predicate.test(new MCItemStack(stack)), requiredStages);
    }
    
    private static Restriction restrictInternal (Predicate<ItemStack> condition, String[] requiredStages) {
        
        return create(requiredStages).restrict(condition);
    }
    
    @ZenCodeType.Method
    public static Restriction create (String... requiredStages) {
        
        if (requiredStages.length == 0) {
            
            throw new IllegalStateException("[ItemStages] A restriction was created with no stages specified. This is not supported! " + PositionUtil.getZCScriptPositionFromStackTrace());
        }
        
        CraftTweakerAPI.logger.info("[ItemStages] Creating new restriction for stages " + Arrays.toString(requiredStages) + ".");
        final Restriction restriction = new Restriction(requiredStages);
        RestrictionManager.INSTANCE.addRestriction(restriction);
        return restriction;
    }
    
    private static boolean hasEnchantment(ItemStack stack, Enchantment enchantment, boolean checkBook, boolean checkItem) {
        
        if (stack.hasTag()) {
            
            if (checkBook && stack.getItem() instanceof EnchantedBookItem && EnchantmentHelper.getEnchantments(stack).getOrDefault(enchantment, 0) > 0) {
                
                return true;
            }
            
            if (checkItem && EnchantmentHelper.getEnchantmentLevel(enchantment, stack) > 0) {
                
                return true;
            }
        }
        
        return false;
    }
}