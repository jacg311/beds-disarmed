package net.jacg.beds_disarmed.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.jacg.beds_disarmed.BedsDisarmed;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BedBlock.class)
public class BedBlockMixin {
	@ModifyExpressionValue(
			method = "onUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BedBlock;isBedWorking(Lnet/minecraft/world/World;)Z"))
	private boolean onlyExplodeWhenConfigAllows(boolean original, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		return BedsDisarmed.CONFIG.getOrDefault(world.getRegistryKey().getValue().toString(), original);
	}
}
