package ru.darkthrone.registry;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import ru.darkthrone.DarkThrone;
import ru.darkthrone.entity.XardrotEntity;
import ru.darkthrone.gameplay.BloodEclipseEffect;
import ru.darkthrone.gameplay.PrimordialFuryEffect;

public final class DTRegistry {
    private DTRegistry() {}

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, DarkThrone.MOD_ID);
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, DarkThrone.MOD_ID);
    public static final DeferredRegister<Effect> EFFECTS = DeferredRegister.create(ForgeRegistries.POTIONS, DarkThrone.MOD_ID);
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, DarkThrone.MOD_ID);

    public static final ItemGroup GROUP = new ItemGroup("darkthrone") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(ANCIENT_DRAGON_HEART.get());
        }
    };

    public static final RegistryObject<Item> ANCIENT_DRAGON_HEART = ITEMS.register("ancient_dragon_heart",
            () -> new Item(new Item.Properties().tab(GROUP).rarity(Rarity.EPIC).stacksTo(1)));
    public static final RegistryObject<Item> RITUAL_DAGGER = ITEMS.register("ritual_dagger",
            () -> new Item(new Item.Properties().tab(GROUP).rarity(Rarity.RARE).stacksTo(1)));
    public static final RegistryObject<Item> KING_REGALIA = ITEMS.register("king_regalia",
            () -> new Item(new Item.Properties().tab(GROUP).rarity(Rarity.RARE).stacksTo(1)));
    public static final RegistryObject<Item> EMPEROR_REGALIA = ITEMS.register("emperor_regalia",
            () -> new Item(new Item.Properties().tab(GROUP).rarity(Rarity.EPIC).stacksTo(1)));

    public static final RegistryObject<Block> BLOOD_OBELISK = BLOCKS.register("blood_obelisk",
            () -> new Block(AbstractBlock.Properties.of(Material.STONE).strength(50f, 1200f)
                    .harvestTool(ToolType.PICKAXE).harvestLevel(3).requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> BLOOD_PORTAL_FRAME = BLOCKS.register("blood_portal_frame",
            () -> new Block(AbstractBlock.Properties.of(Material.STONE).strength(50f, 1200f)
                    .harvestTool(ToolType.PICKAXE).harvestLevel(3).requiresCorrectToolForDrops()));
    public static final RegistryObject<Item> BLOOD_OBELISK_ITEM = ITEMS.register("blood_obelisk",
            () -> new BlockItem(BLOOD_OBELISK.get(), new Item.Properties().tab(GROUP)));
    public static final RegistryObject<Item> BLOOD_PORTAL_FRAME_ITEM = ITEMS.register("blood_portal_frame",
            () -> new BlockItem(BLOOD_PORTAL_FRAME.get(), new Item.Properties().tab(GROUP)));

    public static final RegistryObject<Effect> PRIMORDIAL_FURY = EFFECTS.register("primordial_fury",
            () -> new PrimordialFuryEffect(EffectType.BENEFICIAL, 0xB41432));
    public static final RegistryObject<Effect> BLOOD_ECLIPSE = EFFECTS.register("blood_eclipse",
            () -> new BloodEclipseEffect(EffectType.HARMFUL, 0x5A0B75));

    public static final RegistryObject<EntityType<XardrotEntity>> XARDROT = ENTITIES.register("xardrot",
            () -> EntityType.Builder.<XardrotEntity>of(XardrotEntity::new, EntityClassification.MONSTER)
                    .sized(1.4f, 3.6f).fireImmune().setTrackingRange(64).setUpdateInterval(1)
                    .build(DarkThrone.MOD_ID + ":xardrot"));
}
