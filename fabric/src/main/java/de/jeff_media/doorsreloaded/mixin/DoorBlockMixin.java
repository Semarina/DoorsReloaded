package de.jeff_media.doorsreloaded.mixin;

import de.jeff_media.doorsreloaded.config.ModConfig;
import de.jeff_media.doorsreloaded.utils.DoorUtils;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.redstone.Orientation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockBehaviour.class)
public class DoorBlockMixin {

    @Inject(method = "neighborUpdate", at = @At("RETURN"))
    public void onNeighborUpdate(BlockState state, Level world, BlockPos pos, Block sourceBlock, Orientation wireOrientation, boolean notify, CallbackInfo ci) {
        if (!((Object)this instanceof DoorBlock)) return;
        if (!ModConfig.getInstance().check_for_redstone) return;
        if (world.isClientSide()) return;

        BlockState newState = world.getBlockState(pos);
        if (!(newState.getBlock() instanceof DoorBlock)) return; // Should be impossible if mingled correctly

        boolean wasOpen = state.getValue(DoorBlock.OPEN);
        boolean isOpen = newState.getValue(DoorBlock.OPEN);
        boolean wasPowered = state.getValue(DoorBlock.POWERED);
        boolean isPowered = newState.getValue(DoorBlock.POWERED);

        if (isPowered != wasPowered) {
            // Power state changed, and likely open state changed too handled by vanilla
            // We want to sync the other part
            if (isOpen != wasOpen) { // Only if it actually toggled
                 BlockPos neighborPos = DoorUtils.getDoubleDoorNeighbor(world, pos, newState);
                 if (neighborPos != null) {
                     BlockState neighborState = world.getBlockState(neighborPos);
                     if (neighborState.getValue(DoorBlock.OPEN) != isOpen) {
                         // Toggle neighbor to match
                         // Use flag 10 or similar?
                         world.setBlock(neighborPos, neighborState.setValue(DoorBlock.OPEN, isOpen), 10);
                         // We don't play sound here usually because the primary door played it? 
                         // Or maybe we should?
                         // Vanilla redstone only plays sound for the powered door.
                         // So we probably should play sound to simulate the second door moving.
                         // However, if they are close, one sound might be enough, but standard is both move.
                     }
                 }
            }
        }
    }
}
