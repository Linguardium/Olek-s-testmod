package com.ninetwofifty;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class NEWTESTClient implements ClientModInitializer {

    private static int selectedSlot = 0;

    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            dispatcher.register(
                ClientCommandManager.literal("setSelectedSlot")
                    .then(ClientCommandManager.argument("slot", IntegerArgumentType.integer(0, 8))
                        .executes(context -> setSelectedSlot(context.getSource(), IntegerArgumentType.getInteger(context, "slot"))))
            );
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.world != null) {
                ClientPlayerEntity player = client.player;
                if (player != null) {
                    Vec3d cameraPos = player.getCameraPosVec(1.0F);
                    Vec3d rotationVec = player.getRotationVec(1.0F);
                    Vec3d endPos = cameraPos.add(rotationVec.x * 5.0, rotationVec.y * 5.0, rotationVec.z * 5.0);

                    for (double t = 0; t <= 5.0; t += 0.1) {
                        Vec3d currentPos = cameraPos.add(rotationVec.x * t, rotationVec.y * t, rotationVec.z * t);
                        BlockPos blockPos = new BlockPos((int) currentPos.x, (int) currentPos.y, (int) currentPos.z);
                        Block block = player.getEntityWorld().getBlockState(blockPos).getBlock();

                        if (block == Blocks.WATER && player.getEntityWorld().getBlockState(blockPos).getFluidState().isStill()) {
                            player.getInventory().selectedSlot = selectedSlot;

                            try {
                                Thread.sleep(10);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            client.interactionManager.interactItem(player, Hand.MAIN_HAND);
                            return;
                        }
                    }
                }
            }
        });
    }

    private static int setSelectedSlot(FabricClientCommandSource source, int slot) {
        selectedSlot = slot;
        return 1;
    }
}
