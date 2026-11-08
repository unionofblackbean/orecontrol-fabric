# Minecraft 26.1.2 地物覆盖审计

核查原版命名空间的 221 个配置地物、258 个放置地物；自然装饰引用的命名配置地物均可由单项或组合控制关闭。
从游戏实际 Registry 与原版生物群系的 features 列表读取，逐项执行开关判定。`Stages` 总开关未用于掩盖单项映射遗漏。

| 配置地物 | 类型 | 对应开关 | 原版自然装饰阶段 |
| --- | --- | --- | --- |
| `minecraft:acacia` | `minecraft:tree` | Decorate.disableTREE | VEGETAL_DECORATION |
| `minecraft:amethyst_geode` | `minecraft:geode` | Decorate.disableGEODE | LOCAL_MODIFICATIONS |
| `minecraft:azalea_tree` | `minecraft:tree` | Decorate.disableTREE |  |
| `minecraft:bamboo_no_podzol` | `minecraft:bamboo` | Decorate.disableBAMBOO | VEGETAL_DECORATION |
| `minecraft:bamboo_some_podzol` | `minecraft:bamboo` | Decorate.disableBAMBOO | VEGETAL_DECORATION |
| `minecraft:bamboo_vegetation` | `minecraft:random_selector` | 组合入口，子地物分别受控 | VEGETAL_DECORATION |
| `minecraft:basalt_blobs` | `minecraft:netherrack_replace_blobs` | Decorate.disableBASALT | UNDERGROUND_DECORATION |
| `minecraft:basalt_pillar` | `minecraft:basalt_pillar` | Decorate.disableBASALT | LOCAL_MODIFICATIONS |
| `minecraft:berry_bush` | `minecraft:simple_block` | Decorate.disableBERRIES | VEGETAL_DECORATION |
| `minecraft:birch` | `minecraft:tree` | Decorate.disableTREE |  |
| `minecraft:birch_bees_0002` | `minecraft:tree` | Decorate.disableTREE | VEGETAL_DECORATION |
| `minecraft:birch_bees_0002_leaf_litter` | `minecraft:tree` | Decorate.disableTREE | VEGETAL_DECORATION |
| `minecraft:birch_bees_002` | `minecraft:tree` | Decorate.disableTREE | VEGETAL_DECORATION |
| `minecraft:birch_bees_005` | `minecraft:tree` | Decorate.disableTREE |  |
| `minecraft:birch_leaf_litter` | `minecraft:tree` | Decorate.disableTREE | VEGETAL_DECORATION |
| `minecraft:birch_tall` | `minecraft:random_selector` | Decorate.disableTREE | VEGETAL_DECORATION |
| `minecraft:blackstone_blobs` | `minecraft:netherrack_replace_blobs` | Decorate.disableBLACKSTONE_BLOBS | UNDERGROUND_DECORATION |
| `minecraft:blue_ice` | `minecraft:blue_ice` | Decorate.disableBLUE_ICE | SURFACE_STRUCTURES |
| `minecraft:bonus_chest` | `minecraft:bonus_chest` | 特殊入口，不在原版生物群系自然装饰表中 |  |
| `minecraft:brown_mushroom` | `minecraft:simple_block` | Decorate.disableSHROOM | UNDERGROUND_DECORATION, VEGETAL_DECORATION |
| `minecraft:bush` | `minecraft:simple_block` | Decorate.disableBUSH | VEGETAL_DECORATION |
| `minecraft:cactus` | `minecraft:block_column` | Decorate.disableCACTUS | VEGETAL_DECORATION |
| `minecraft:cave_vine` | `minecraft:block_column` | Decorate.disableCAVE_VINES | VEGETAL_DECORATION |
| `minecraft:cave_vine_in_moss` | `minecraft:block_column` | Decorate.disableCAVE_VINES |  |
| `minecraft:cherry` | `minecraft:tree` | Decorate.disableTREE |  |
| `minecraft:cherry_bees_005` | `minecraft:tree` | Decorate.disableTREE | VEGETAL_DECORATION |
| `minecraft:chorus_plant` | `minecraft:chorus_plant` | Decorate.disableCHORUS | VEGETAL_DECORATION |
| `minecraft:clay_pool_with_dripleaves` | `minecraft:waterlogged_vegetation_patch` | Decorate.disableCAVE_CLAY | VEGETAL_DECORATION |
| `minecraft:clay_with_dripleaves` | `minecraft:vegetation_patch` | Decorate.disableCAVE_CLAY | VEGETAL_DECORATION |
| `minecraft:crimson_forest_vegetation` | `minecraft:nether_forest_vegetation` | Decorate.disableNETHER_VEGETATION | VEGETAL_DECORATION |
| `minecraft:crimson_forest_vegetation_bonemeal` | `minecraft:nether_forest_vegetation` | Decorate.disableNETHER_VEGETATION |  |
| `minecraft:crimson_fungus` | `minecraft:huge_fungus` | Decorate.disableTREE | VEGETAL_DECORATION |
| `minecraft:crimson_fungus_planted` | `minecraft:huge_fungus` | Decorate.disableTREE |  |
| `minecraft:crimson_roots` | `minecraft:simple_block` | Decorate.disableNETHER_VEGETATION | UNDERGROUND_DECORATION |
| `minecraft:dark_forest_vegetation` | `minecraft:random_selector` | Decorate.disableTREE | VEGETAL_DECORATION |
| `minecraft:dark_oak` | `minecraft:tree` | Decorate.disableTREE |  |
| `minecraft:dark_oak_leaf_litter` | `minecraft:tree` | Decorate.disableTREE | VEGETAL_DECORATION |
| `minecraft:dead_bush` | `minecraft:simple_block` | Decorate.disableDEAD_BUSH | VEGETAL_DECORATION |
| `minecraft:delta` | `minecraft:delta_feature` | Decorate.disableDELTA | SURFACE_STRUCTURES |
| `minecraft:desert_well` | `minecraft:desert_well` | Decorate.disableDESERT_WELL | SURFACE_STRUCTURES |
| `minecraft:disk_clay` | `minecraft:disk` | Decorate.disableCLAY | UNDERGROUND_ORES |
| `minecraft:disk_grass` | `minecraft:disk` | Decorate.disableDIRT_DISK | UNDERGROUND_ORES |
| `minecraft:disk_gravel` | `minecraft:disk` | Decorate.disableSAND_PASS2 | UNDERGROUND_ORES |
| `minecraft:disk_sand` | `minecraft:disk` | Decorate.disableSAND | UNDERGROUND_ORES |
| `minecraft:dripleaf` | `minecraft:simple_random_selector` | Decorate.disableDRIPLEAF |  |
| `minecraft:dripstone_cluster` | `minecraft:dripstone_cluster` | Decorate.disableDRIPSTONE | UNDERGROUND_DECORATION |
| `minecraft:dry_grass` | `minecraft:simple_block` | Decorate.disableGRASS | VEGETAL_DECORATION |
| `minecraft:end_gateway_delayed` | `minecraft:end_gateway` | Decorate.disableEND_GATEWAY |  |
| `minecraft:end_gateway_return` | `minecraft:end_gateway` | Decorate.disableEND_GATEWAY | SURFACE_STRUCTURES |
| `minecraft:end_island` | `minecraft:end_island` | Decorate.disableEND_ISLAND | RAW_GENERATION |
| `minecraft:end_platform` | `minecraft:end_platform` | Decorate.disableEND_PLATFORM | TOP_LAYER_MODIFICATION |
| `minecraft:end_spike` | `minecraft:end_spike` | Decorate.disableEND_SPIKE | SURFACE_STRUCTURES |
| `minecraft:fallen_birch_tree` | `minecraft:fallen_tree` | Decorate.disableTREE | VEGETAL_DECORATION |
| `minecraft:fallen_jungle_tree` | `minecraft:fallen_tree` | Decorate.disableTREE | VEGETAL_DECORATION |
| `minecraft:fallen_oak_tree` | `minecraft:fallen_tree` | Decorate.disableTREE | VEGETAL_DECORATION |
| `minecraft:fallen_spruce_tree` | `minecraft:fallen_tree` | Decorate.disableTREE | VEGETAL_DECORATION |
| `minecraft:fallen_super_birch_tree` | `minecraft:fallen_tree` | Decorate.disableTREE | VEGETAL_DECORATION |
| `minecraft:fancy_oak` | `minecraft:tree` | Decorate.disableTREE | VEGETAL_DECORATION |
| `minecraft:fancy_oak_bees` | `minecraft:tree` | Decorate.disableTREE | VEGETAL_DECORATION |
| `minecraft:fancy_oak_bees_0002_leaf_litter` | `minecraft:tree` | Decorate.disableTREE | VEGETAL_DECORATION |
| `minecraft:fancy_oak_bees_002` | `minecraft:tree` | Decorate.disableTREE | VEGETAL_DECORATION |
| `minecraft:fancy_oak_bees_005` | `minecraft:tree` | Decorate.disableTREE | VEGETAL_DECORATION |
| `minecraft:fancy_oak_leaf_litter` | `minecraft:tree` | Decorate.disableTREE | VEGETAL_DECORATION |
| `minecraft:firefly_bush` | `minecraft:simple_block` | Decorate.disableFIREFLY_BUSH | VEGETAL_DECORATION |
| `minecraft:flower_cherry` | `minecraft:simple_block` | Decorate.disableFLOWERS | VEGETAL_DECORATION |
| `minecraft:flower_default` | `minecraft:simple_block` | Decorate.disableFLOWERS | VEGETAL_DECORATION |
| `minecraft:flower_flower_forest` | `minecraft:simple_block` | Decorate.disableFLOWERS | VEGETAL_DECORATION |
| `minecraft:flower_meadow` | `minecraft:simple_block` | Decorate.disableFLOWERS | VEGETAL_DECORATION |
| `minecraft:flower_pale_garden` | `minecraft:simple_block` | Decorate.disableFLOWERS | VEGETAL_DECORATION |
| `minecraft:flower_plain` | `minecraft:simple_block` | Decorate.disableFLOWERS | VEGETAL_DECORATION |
| `minecraft:flower_swamp` | `minecraft:simple_block` | Decorate.disableFLOWERS | VEGETAL_DECORATION |
| `minecraft:forest_flowers` | `minecraft:simple_random_selector` | Decorate.disableFLOWERS | VEGETAL_DECORATION |
| `minecraft:forest_rock` | `minecraft:block_blob` | Decorate.disableROCK | LOCAL_MODIFICATIONS |
| `minecraft:fossil_coal` | `minecraft:fossil` | Decorate.disableFOSSIL | UNDERGROUND_STRUCTURES |
| `minecraft:fossil_diamonds` | `minecraft:fossil` | Decorate.disableFOSSIL | UNDERGROUND_STRUCTURES |
| `minecraft:freeze_top_layer` | `minecraft:freeze_top_layer` | Populate.disableICE | TOP_LAYER_MODIFICATION |
| `minecraft:glow_lichen` | `minecraft:multiface_growth` | Decorate.disableLICHEN | VEGETAL_DECORATION |
| `minecraft:glowstone_extra` | `minecraft:glowstone_blob` | Populate.disableGLOWSTONE | UNDERGROUND_DECORATION |
| `minecraft:grass` | `minecraft:simple_block` | Decorate.disableGRASS | VEGETAL_DECORATION |
| `minecraft:grass_jungle` | `minecraft:simple_block` | Decorate.disableGRASS | VEGETAL_DECORATION |
| `minecraft:huge_brown_mushroom` | `minecraft:huge_brown_mushroom` | Decorate.disableBIG_SHROOM | VEGETAL_DECORATION |
| `minecraft:huge_red_mushroom` | `minecraft:huge_red_mushroom` | Decorate.disableBIG_SHROOM | VEGETAL_DECORATION |
| `minecraft:ice_patch` | `minecraft:disk` | Decorate.disableICE | SURFACE_STRUCTURES |
| `minecraft:ice_spike` | `minecraft:spike` | Decorate.disableICE | SURFACE_STRUCTURES |
| `minecraft:iceberg_blue` | `minecraft:iceberg` | Decorate.disableICEBERG | LOCAL_MODIFICATIONS |
| `minecraft:iceberg_packed` | `minecraft:iceberg` | Decorate.disableICEBERG | LOCAL_MODIFICATIONS |
| `minecraft:jungle_bush` | `minecraft:tree` | Decorate.disableTREE | VEGETAL_DECORATION |
| `minecraft:jungle_tree` | `minecraft:tree` | Decorate.disableTREE | VEGETAL_DECORATION |
| `minecraft:jungle_tree_no_vine` | `minecraft:tree` | Decorate.disableTREE |  |
| `minecraft:kelp` | `minecraft:kelp` | Decorate.disableKELP | VEGETAL_DECORATION |
| `minecraft:lake_lava` | `minecraft:lake` | Populate.disableLAVA | LAKES |
| `minecraft:large_basalt_columns` | `minecraft:basalt_columns` | Decorate.disableBASALT | SURFACE_STRUCTURES |
| `minecraft:large_dripstone` | `minecraft:large_dripstone` | Decorate.disableDRIPSTONE | LOCAL_MODIFICATIONS |
| `minecraft:large_fern` | `minecraft:simple_block` | Decorate.disableGRASS | VEGETAL_DECORATION |
| `minecraft:leaf_litter` | `minecraft:simple_block` | Decorate.disableLEAF_LITTER | VEGETAL_DECORATION |
| `minecraft:lush_caves_clay` | `minecraft:random_boolean_selector` | Decorate.disableCAVE_CLAY | VEGETAL_DECORATION |
| `minecraft:mangrove` | `minecraft:tree` | Decorate.disableTREE | VEGETAL_DECORATION |
| `minecraft:mangrove_vegetation` | `minecraft:random_selector` | Decorate.disableTREE | VEGETAL_DECORATION |
| `minecraft:meadow_trees` | `minecraft:random_selector` | Decorate.disableTREE | VEGETAL_DECORATION |
| `minecraft:mega_jungle_tree` | `minecraft:tree` | Decorate.disableTREE | VEGETAL_DECORATION |
| `minecraft:mega_pine` | `minecraft:tree` | Decorate.disableTREE | VEGETAL_DECORATION |
| `minecraft:mega_spruce` | `minecraft:tree` | Decorate.disableTREE | VEGETAL_DECORATION |
| `minecraft:melon` | `minecraft:simple_block` | Decorate.disableMELON | VEGETAL_DECORATION |
| `minecraft:monster_room` | `minecraft:monster_room` | Populate.disableDUNGEON | UNDERGROUND_STRUCTURES |
| `minecraft:moss_patch` | `minecraft:vegetation_patch` | Decorate.disableMOSS | VEGETAL_DECORATION |
| `minecraft:moss_patch_bonemeal` | `minecraft:vegetation_patch` | Decorate.disableMOSS |  |
| `minecraft:moss_patch_ceiling` | `minecraft:vegetation_patch` | Decorate.disableMOSS | VEGETAL_DECORATION |
| `minecraft:moss_vegetation` | `minecraft:simple_block` | Decorate.disableMOSS |  |
| `minecraft:mushroom_island_vegetation` | `minecraft:random_boolean_selector` | 组合入口，子地物分别受控 | VEGETAL_DECORATION |
| `minecraft:nether_sprouts` | `minecraft:nether_forest_vegetation` | Decorate.disableNETHER_VEGETATION | VEGETAL_DECORATION |
| `minecraft:nether_sprouts_bonemeal` | `minecraft:nether_forest_vegetation` | Decorate.disableNETHER_VEGETATION |  |
| `minecraft:oak` | `minecraft:tree` | Decorate.disableTREE | VEGETAL_DECORATION |
| `minecraft:oak_bees_0002_leaf_litter` | `minecraft:tree` | Decorate.disableTREE | VEGETAL_DECORATION |
| `minecraft:oak_bees_002` | `minecraft:tree` | Decorate.disableTREE | VEGETAL_DECORATION |
| `minecraft:oak_bees_005` | `minecraft:tree` | Decorate.disableTREE | VEGETAL_DECORATION |
| `minecraft:oak_leaf_litter` | `minecraft:tree` | Decorate.disableTREE | VEGETAL_DECORATION |
| `minecraft:ore_ancient_debris_large` | `minecraft:scattered_ore` | Minable.disableAllVanillaGeneration / Minable.disableANCIENT_DEBRIS | UNDERGROUND_DECORATION |
| `minecraft:ore_ancient_debris_small` | `minecraft:scattered_ore` | Minable.disableAllVanillaGeneration / Minable.disableANCIENT_DEBRIS | UNDERGROUND_DECORATION |
| `minecraft:ore_andesite` | `minecraft:ore` | Minable.disableAllVanillaGeneration / Minable.disableANDESITE | UNDERGROUND_ORES |
| `minecraft:ore_blackstone` | `minecraft:ore` | Minable.disableAllVanillaGeneration / Minable.disableBLACKSTONE | UNDERGROUND_DECORATION |
| `minecraft:ore_clay` | `minecraft:ore` | Minable.disableAllVanillaGeneration / Minable.disableCLAY | UNDERGROUND_ORES |
| `minecraft:ore_coal` | `minecraft:ore` | Minable.disableAllVanillaGeneration / Minable.disableCOAL | UNDERGROUND_ORES |
| `minecraft:ore_coal_buried` | `minecraft:ore` | Minable.disableAllVanillaGeneration / Minable.disableCOAL | UNDERGROUND_ORES |
| `minecraft:ore_copper_large` | `minecraft:ore` | Minable.disableAllVanillaGeneration / Minable.disableCOPPER | UNDERGROUND_ORES |
| `minecraft:ore_copper_small` | `minecraft:ore` | Minable.disableAllVanillaGeneration / Minable.disableCOPPER | UNDERGROUND_ORES |
| `minecraft:ore_diamond_buried` | `minecraft:ore` | Minable.disableAllVanillaGeneration / Minable.disableDIAMOND | UNDERGROUND_ORES |
| `minecraft:ore_diamond_large` | `minecraft:ore` | Minable.disableAllVanillaGeneration / Minable.disableDIAMOND | UNDERGROUND_ORES |
| `minecraft:ore_diamond_medium` | `minecraft:ore` | Minable.disableAllVanillaGeneration / Minable.disableDIAMOND | UNDERGROUND_ORES |
| `minecraft:ore_diamond_small` | `minecraft:ore` | Minable.disableAllVanillaGeneration / Minable.disableDIAMOND | UNDERGROUND_ORES |
| `minecraft:ore_diorite` | `minecraft:ore` | Minable.disableAllVanillaGeneration / Minable.disableDIORITE | UNDERGROUND_ORES |
| `minecraft:ore_dirt` | `minecraft:ore` | Minable.disableAllVanillaGeneration / Minable.disableDIRT | UNDERGROUND_ORES |
| `minecraft:ore_emerald` | `minecraft:ore` | Minable.disableAllVanillaGeneration / Minable.disableEMERALD | UNDERGROUND_ORES |
| `minecraft:ore_gold` | `minecraft:ore` | Minable.disableAllVanillaGeneration / Minable.disableGOLD | UNDERGROUND_ORES |
| `minecraft:ore_gold_buried` | `minecraft:ore` | Minable.disableAllVanillaGeneration / Minable.disableGOLD | UNDERGROUND_ORES |
| `minecraft:ore_granite` | `minecraft:ore` | Minable.disableAllVanillaGeneration / Minable.disableGRANITE | UNDERGROUND_ORES |
| `minecraft:ore_gravel` | `minecraft:ore` | Minable.disableAllVanillaGeneration / Minable.disableGRAVEL | UNDERGROUND_ORES |
| `minecraft:ore_gravel_nether` | `minecraft:ore` | Minable.disableAllVanillaGeneration / Minable.disableGRAVEL | UNDERGROUND_DECORATION |
| `minecraft:ore_infested` | `minecraft:ore` | Minable.disableAllVanillaGeneration / Minable.disableSILVERFISH | UNDERGROUND_DECORATION |
| `minecraft:ore_iron` | `minecraft:ore` | Minable.disableAllVanillaGeneration / Minable.disableIRON | UNDERGROUND_ORES |
| `minecraft:ore_iron_small` | `minecraft:ore` | Minable.disableAllVanillaGeneration / Minable.disableIRON | UNDERGROUND_ORES |
| `minecraft:ore_lapis` | `minecraft:ore` | Minable.disableAllVanillaGeneration / Minable.disableLAPIS | UNDERGROUND_ORES |
| `minecraft:ore_lapis_buried` | `minecraft:ore` | Minable.disableAllVanillaGeneration / Minable.disableLAPIS | UNDERGROUND_ORES |
| `minecraft:ore_magma` | `minecraft:ore` | Populate.disableNETHER_MAGMA | UNDERGROUND_DECORATION |
| `minecraft:ore_nether_gold` | `minecraft:ore` | Minable.disableAllVanillaGeneration / Minable.disableGOLD | UNDERGROUND_DECORATION |
| `minecraft:ore_quartz` | `minecraft:ore` | Minable.disableAllVanillaGeneration / Minable.disableQUARTZ | UNDERGROUND_DECORATION |
| `minecraft:ore_redstone` | `minecraft:ore` | Minable.disableAllVanillaGeneration / Minable.disableREDSTONE | UNDERGROUND_ORES |
| `minecraft:ore_soul_sand` | `minecraft:ore` | Minable.disableAllVanillaGeneration / Minable.disableSOUL_SAND | UNDERGROUND_DECORATION |
| `minecraft:ore_tuff` | `minecraft:ore` | Minable.disableAllVanillaGeneration / Minable.disableTUFF | UNDERGROUND_ORES |
| `minecraft:pale_forest_flower` | `minecraft:simple_block` | Decorate.disableFLOWERS | VEGETAL_DECORATION |
| `minecraft:pale_garden_vegetation` | `minecraft:random_selector` | Decorate.disableTREE | VEGETAL_DECORATION |
| `minecraft:pale_moss_patch` | `minecraft:vegetation_patch` | Decorate.disableMOSS | VEGETAL_DECORATION |
| `minecraft:pale_moss_patch_bonemeal` | `minecraft:vegetation_patch` | Decorate.disableMOSS |  |
| `minecraft:pale_moss_vegetation` | `minecraft:simple_block` | Decorate.disableMOSS |  |
| `minecraft:pale_oak` | `minecraft:tree` | Decorate.disableTREE | VEGETAL_DECORATION |
| `minecraft:pale_oak_bonemeal` | `minecraft:tree` | Decorate.disableTREE |  |
| `minecraft:pale_oak_creaking` | `minecraft:tree` | Decorate.disableTREE | VEGETAL_DECORATION |
| `minecraft:patch_fire` | `minecraft:simple_block` | Populate.disableFIRE | UNDERGROUND_DECORATION |
| `minecraft:patch_soul_fire` | `minecraft:simple_block` | Populate.disableFIRE | UNDERGROUND_DECORATION |
| `minecraft:pile_hay` | `minecraft:block_pile` | Decorate.disableBLOCK_PILES |  |
| `minecraft:pile_ice` | `minecraft:block_pile` | Decorate.disableBLOCK_PILES |  |
| `minecraft:pile_melon` | `minecraft:block_pile` | Decorate.disableBLOCK_PILES |  |
| `minecraft:pile_pumpkin` | `minecraft:block_pile` | Decorate.disableBLOCK_PILES |  |
| `minecraft:pile_snow` | `minecraft:block_pile` | Decorate.disableBLOCK_PILES |  |
| `minecraft:pine` | `minecraft:tree` | Decorate.disableTREE | VEGETAL_DECORATION |
| `minecraft:pointed_dripstone` | `minecraft:simple_random_selector` | Decorate.disableDRIPSTONE | UNDERGROUND_DECORATION |
| `minecraft:pumpkin` | `minecraft:simple_block` | Decorate.disablePUMPKIN | VEGETAL_DECORATION |
| `minecraft:red_mushroom` | `minecraft:simple_block` | Decorate.disableSHROOM | UNDERGROUND_DECORATION, VEGETAL_DECORATION |
| `minecraft:rooted_azalea_tree` | `minecraft:root_system` | Decorate.disableROOTS | VEGETAL_DECORATION |
| `minecraft:sculk_patch_ancient_city` | `minecraft:sculk_patch` | Decorate.disableSCULK |  |
| `minecraft:sculk_patch_deep_dark` | `minecraft:sculk_patch` | Decorate.disableSCULK | UNDERGROUND_DECORATION |
| `minecraft:sculk_vein` | `minecraft:multiface_growth` | Decorate.disableSCULK | UNDERGROUND_DECORATION |
| `minecraft:sea_pickle` | `minecraft:sea_pickle` | Decorate.disableSEA_PICKLE | VEGETAL_DECORATION |
| `minecraft:seagrass_mid` | `minecraft:seagrass` | Decorate.disableSEAGRASS | VEGETAL_DECORATION |
| `minecraft:seagrass_short` | `minecraft:seagrass` | Decorate.disableSEAGRASS | VEGETAL_DECORATION |
| `minecraft:seagrass_slightly_less_short` | `minecraft:seagrass` | Decorate.disableSEAGRASS | VEGETAL_DECORATION |
| `minecraft:seagrass_tall` | `minecraft:seagrass` | Decorate.disableSEAGRASS | VEGETAL_DECORATION |
| `minecraft:small_basalt_columns` | `minecraft:basalt_columns` | Decorate.disableBASALT | SURFACE_STRUCTURES |
| `minecraft:spore_blossom` | `minecraft:simple_block` | Decorate.disableSPORE_BLOSSOM | VEGETAL_DECORATION |
| `minecraft:spring_lava_frozen` | `minecraft:spring_feature` | Decorate.disableLAKE_LAVA | FLUID_SPRINGS |
| `minecraft:spring_lava_nether` | `minecraft:spring_feature` | Decorate.disableLAKE_LAVA | UNDERGROUND_DECORATION |
| `minecraft:spring_lava_overworld` | `minecraft:spring_feature` | Decorate.disableLAKE_LAVA | FLUID_SPRINGS, VEGETAL_DECORATION |
| `minecraft:spring_nether_closed` | `minecraft:spring_feature` | Decorate.disableLAKE_LAVA | UNDERGROUND_DECORATION |
| `minecraft:spring_nether_open` | `minecraft:spring_feature` | Decorate.disableLAKE_LAVA | UNDERGROUND_DECORATION |
| `minecraft:spring_water` | `minecraft:spring_feature` | Decorate.disableLAKE_WATER | FLUID_SPRINGS |
| `minecraft:spruce` | `minecraft:tree` | Decorate.disableTREE | VEGETAL_DECORATION |
| `minecraft:sugar_cane` | `minecraft:block_column` | Decorate.disableREED | VEGETAL_DECORATION |
| `minecraft:sunflower` | `minecraft:simple_block` | Decorate.disableFLOWERS | VEGETAL_DECORATION |
| `minecraft:super_birch_bees` | `minecraft:tree` | Decorate.disableTREE | VEGETAL_DECORATION |
| `minecraft:super_birch_bees_0002` | `minecraft:tree` | Decorate.disableTREE | VEGETAL_DECORATION |
| `minecraft:swamp_oak` | `minecraft:tree` | Decorate.disableTREE | VEGETAL_DECORATION |
| `minecraft:taiga_grass` | `minecraft:simple_block` | Decorate.disableGRASS | VEGETAL_DECORATION |
| `minecraft:tall_grass` | `minecraft:simple_block` | Decorate.disableGRASS | VEGETAL_DECORATION |
| `minecraft:tall_mangrove` | `minecraft:tree` | Decorate.disableTREE | VEGETAL_DECORATION |
| `minecraft:trees_badlands` | `minecraft:random_selector` | Decorate.disableTREE | VEGETAL_DECORATION |
| `minecraft:trees_birch` | `minecraft:random_selector` | Decorate.disableTREE | VEGETAL_DECORATION |
| `minecraft:trees_birch_and_oak_leaf_litter` | `minecraft:random_selector` | Decorate.disableTREE | VEGETAL_DECORATION |
| `minecraft:trees_flower_forest` | `minecraft:random_selector` | Decorate.disableTREE | VEGETAL_DECORATION |
| `minecraft:trees_grove` | `minecraft:random_selector` | Decorate.disableTREE | VEGETAL_DECORATION |
| `minecraft:trees_jungle` | `minecraft:random_selector` | Decorate.disableTREE | VEGETAL_DECORATION |
| `minecraft:trees_old_growth_pine_taiga` | `minecraft:random_selector` | Decorate.disableTREE | VEGETAL_DECORATION |
| `minecraft:trees_old_growth_spruce_taiga` | `minecraft:random_selector` | Decorate.disableTREE | VEGETAL_DECORATION |
| `minecraft:trees_plains` | `minecraft:random_selector` | Decorate.disableTREE | VEGETAL_DECORATION |
| `minecraft:trees_savanna` | `minecraft:random_selector` | Decorate.disableTREE | VEGETAL_DECORATION |
| `minecraft:trees_snowy` | `minecraft:random_selector` | Decorate.disableTREE | VEGETAL_DECORATION |
| `minecraft:trees_sparse_jungle` | `minecraft:random_selector` | Decorate.disableTREE | VEGETAL_DECORATION |
| `minecraft:trees_taiga` | `minecraft:random_selector` | Decorate.disableTREE | VEGETAL_DECORATION |
| `minecraft:trees_water` | `minecraft:random_selector` | Decorate.disableTREE | VEGETAL_DECORATION |
| `minecraft:trees_windswept_hills` | `minecraft:random_selector` | Decorate.disableTREE | VEGETAL_DECORATION |
| `minecraft:twisting_vines` | `minecraft:twisting_vines` | Decorate.disableNETHER_VINES | VEGETAL_DECORATION |
| `minecraft:twisting_vines_bonemeal` | `minecraft:twisting_vines` | Decorate.disableNETHER_VINES |  |
| `minecraft:underwater_magma` | `minecraft:underwater_magma` | Decorate.disableUNDERWATER_MAGMA | UNDERGROUND_ORES |
| `minecraft:vines` | `minecraft:vines` | Decorate.disableVINES | VEGETAL_DECORATION |
| `minecraft:void_start_platform` | `minecraft:void_start_platform` | Decorate.disableEND_PLATFORM | TOP_LAYER_MODIFICATION |
| `minecraft:warm_ocean_vegetation` | `minecraft:simple_random_selector` | Decorate.disableCORAL | VEGETAL_DECORATION |
| `minecraft:warped_forest_vegetation` | `minecraft:nether_forest_vegetation` | Decorate.disableNETHER_VEGETATION | VEGETAL_DECORATION |
| `minecraft:warped_forest_vegetation_bonemeal` | `minecraft:nether_forest_vegetation` | Decorate.disableNETHER_VEGETATION |  |
| `minecraft:warped_fungus` | `minecraft:huge_fungus` | Decorate.disableTREE | VEGETAL_DECORATION |
| `minecraft:warped_fungus_planted` | `minecraft:huge_fungus` | Decorate.disableTREE |  |
| `minecraft:waterlily` | `minecraft:simple_block` | Decorate.disableLILYPAD | VEGETAL_DECORATION |
| `minecraft:weeping_vines` | `minecraft:weeping_vines` | Decorate.disableNETHER_VINES | VEGETAL_DECORATION |
| `minecraft:wildflower` | `minecraft:simple_block` | Decorate.disableFLOWERS | VEGETAL_DECORATION |

阶段开关控制 PlacedFeature，包含匿名内联地物。大型 Structure、基础地形密度与天然海水不属于这套地物开关。
地表泥土来自独立的表层生成，新增 Surface.disableDIRT；地下泥土团块仍由 Minable.disableDIRT 控制。
树木装饰器附带的藤蔓、落叶，以及苔藓混合提供器中的草，通过自然地物作用域内的最终方块写入控制补充覆盖。
出生奖励箱、骨粉/树苗、指令放置与末影龙重生等 ServerLevel 直接入口不按首次自然装饰拦截。测试夹具复用原版地物，另增加珊瑚/海泡菜尝试次数以建立稳定阳性对照。
