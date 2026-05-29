package de.jeff_media.doorsreloaded.listeners;

import de.jeff_media.doorsreloaded.config.ModConfig;
import de.jeff_media.doorsreloaded.config.ModConfig;
import de.jeff_media.doorsreloaded.utils.DoorUtils;
import de.jeff_media.doorsreloaded.DoorsReloadedMod;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.resources.Identifier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class InteractionListener {

    public static void register() {
        // Right Click (Use)
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
            if (world.isClientSide()) return InteractionResult.PASS;

            BlockPos pos = hitResult.getBlockPos();
            BlockState state = world.getBlockState(pos);
            ModConfig config = ModConfig.getInstance();

            // Iron Door / Trapdoor handling
            boolean isDoor = DoorUtils.isDoor(state);
            boolean isTrapDoor = DoorUtils.isTrapDoor(state);
            
            // Check for Iron material using Blocks comparison or Tags if available, assuming Iron Door is the main one.
            // In 1.21, we can check block instance or tags.
            // Iron Door and Iron Trapdoor are specific blocks.
            boolean isIronDoor = state.is(Blocks.IRON_DOOR);
            boolean isIronTrapdoor = state.is(Blocks.IRON_TRAPDOOR);
            // Or copper doors if we want to include them as "metal" that requires power? 
            // Copper doors in 1.21 can be opened by hand (unlike iron). Iron is the only one defaulting to locked.
            // So we strictly stick to IRON_DOOR and IRON_TRAPDOOR for the "allow opening with hands" feature if strictly following original plugin.
            // Original used Material.IRON_DOOR etc.

            if (isIronDoor || isIronTrapdoor) {
                if ((isIronDoor && config.allow_opening_irondoors_with_hands) || (isIronTrapdoor && config.allow_opening_irontrapdoors_with_hands)) {
                    boolean isOpen = false;
                    if (isIronDoor) isOpen = state.getValue(DoorBlock.OPEN);
                    else isOpen = state.getValue(TrapDoorBlock.OPEN);
                    
                    // Toggle
                    boolean newOpen = !isOpen;
                    BlockState newState;
                    
                    if (isIronDoor) {
                        newState = state.setValue(DoorBlock.OPEN, newOpen);
                        world.setBlock(pos, newState, 10);
                        world.playSound(null, pos, newOpen ? SoundEvents.IRON_DOOR_OPEN : SoundEvents.IRON_DOOR_CLOSE, SoundSource.BLOCKS, 1.0f, 1.0f);
                        if (newOpen) {
                            de.jeff_media.doorsreloaded.scheduler.DoorScheduler.scheduleAutoClose((net.minecraft.server.level.ServerLevel) world, pos);
                        }
                        if (config.allow_doubledoors) {
                            handleDoubleDoor(world, pos, state, newOpen);
                        }
                    } else {
                        newState = state.setValue(TrapDoorBlock.OPEN, newOpen);
                        world.setBlock(pos, newState, 10);
                        world.playSound(null, pos, newOpen ? SoundEvents.IRON_TRAPDOOR_OPEN : SoundEvents.IRON_TRAPDOOR_CLOSE, SoundSource.BLOCKS, 1.0f, 1.0f);
                        if (newOpen) {
                             de.jeff_media.doorsreloaded.scheduler.DoorScheduler.scheduleAutoClose((net.minecraft.server.level.ServerLevel) world, pos);
                        }
                    }

                    return InteractionResult.SUCCESS; // Intercepted and handled
                }
            }

            // Double Door handling for other doors (Vanilla handles the initial toggle)
            if (isDoor && !isIronDoor && config.allow_doubledoors) {
                 // Predict the new state: Vanilla toggles it.
                 // NOTE: UseBlockCallback happens BEFORE vanilla logic.
                 // If we assume vanilla toggles, we need to apply the toggle to the neighbor.
                 boolean currentOpen = state.getValue(DoorBlock.OPEN);
                 boolean newOpen = !currentOpen;
                 
                 handleDoubleDoor(world, pos, state, newOpen);
                 if (newOpen) {
                      de.jeff_media.doorsreloaded.scheduler.DoorScheduler.scheduleAutoClose((net.minecraft.server.level.ServerLevel) world, pos);
                 }
                 // We return PASS so vanilla toggles the clicked door
                 return InteractionResult.PASS;
            }

            return InteractionResult.PASS;
        });

        // Left Click (Attack) - Knocking
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
            if (world.isClientSide()) return InteractionResult.PASS;
            
            BlockState state = world.getBlockState(pos);
            ModConfig config = ModConfig.getInstance();

            boolean isDoor = DoorUtils.isDoor(state);
            boolean isTrapDoor = DoorUtils.isTrapDoor(state);
            boolean isGate = DoorUtils.isGate(state);

            if (!isDoor && !isTrapDoor && !isGate) {
                DoorsReloadedMod.debug("Block is not a door, trapdoor, or gate.");
                return InteractionResult.PASS;
            }
            if (isDoor && !config.allow_knocking_doors) {
                DoorsReloadedMod.debug("Knocking doors is disabled.");
                return InteractionResult.PASS;
            }
            if (isTrapDoor && !config.allow_knocking_trapdoors) {
                DoorsReloadedMod.debug("Knocking trapdoors is disabled.");
                return InteractionResult.PASS;
            }
            if (isGate && !config.allow_knocking_gates) {
                DoorsReloadedMod.debug("Knocking gates is disabled.");
                return InteractionResult.PASS;
            }

            // Checks
            if (config.knocking_requires_shift && !player.isCrouching()) {
                DoorsReloadedMod.debug("Player is not sneaking.");
                return InteractionResult.PASS;
            }
            if (config.knocking_requires_empty_hand && !player.getMainHandItem().isEmpty()) {
                DoorsReloadedMod.debug("Player hand is not empty.");
                return InteractionResult.PASS;
            }

            DoorsReloadedMod.debug("Player " + player.getName().getString() + " knocked on " + state.getBlock().getDescriptionId());

            // Play Sound
            String soundStr;
            String name = state.getBlock().getDescriptionId().toLowerCase();
            boolean isCopper = name.contains("copper");
            boolean isIron = name.contains("iron") || state.is(Blocks.IRON_DOOR) || state.is(Blocks.IRON_TRAPDOOR);

            if (isDoor) {
                if (isCopper) soundStr = config.sound_knock_door_copper;
                else if (isIron) soundStr = config.sound_knock_door_iron;
                else soundStr = config.sound_knock_door_wood;
            } else if (isTrapDoor) {
                if (isCopper) soundStr = config.sound_knock_trapdoor_copper;
                else if (isIron) soundStr = config.sound_knock_trapdoor_iron;
                else soundStr = config.sound_knock_trapdoor_wood;
            } else {
                if (isCopper) soundStr = config.sound_knock_gate_copper;
                else if (isIron) soundStr = config.sound_knock_gate_iron;
                else soundStr = config.sound_knock_gate_wood;
            }
            
            Identifier soundId = Identifier.tryParse(soundStr);
            SoundEvent sound = null;
            if (soundId != null) {
                sound = BuiltInRegistries.SOUND_EVENT.get(soundId).map(h -> h.value()).orElse(null);
            }
            if (sound == null) sound = SoundEvents.ZOMBIE_ATTACK_WOODEN_DOOR; // Fallback

            SoundSource category = SoundSource.BLOCKS;
            try {
                category = SoundSource.valueOf(config.sound_knock_category);
            } catch (IllegalArgumentException ignored) {}

            world.playSound(null, pos, sound, category, (float)config.sound_knock_volume, (float)config.sound_knock_pitch);

            return InteractionResult.PASS;
        });
    }

    private static void handleDoubleDoor(Level world, BlockPos pos, BlockState state, boolean newOpenState) {
        BlockPos neighborPos = DoorUtils.getDoubleDoorNeighbor(world, pos, state);
        if (neighborPos != null) {
            BlockState neighborState = world.getBlockState(neighborPos);
            if (neighborState.getValue(DoorBlock.OPEN) != newOpenState) {
                world.setBlock(neighborPos, neighborState.setValue(DoorBlock.OPEN, newOpenState), 10);
                // We should also play sound/event? Vanilla usually plays sound for the clicked door.
                // We manually play sound for the other door?
                // Usually double doors sound better if only one sound plays or both.
                // We'll leave it silent or let the block update trigger sound?
                // setBlockState with flag 10 (2 | 8) updates clients and no physics? block update?
                // We might want to play sound.
            }
        }
    }
}
