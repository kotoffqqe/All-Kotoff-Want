package com.kotoff.allkotowant.client;

import com.kotoff.allkotowant.client.mixin.AbstractContainerScreenAccessor;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.Slot;

public final class AllKotoffWantClient implements ClientModInitializer {
    private static KeyMapping openEditor;

    @Override
    public void onInitializeClient() {
        KeyMapping.Category category = KeyMapping.Category.register(
                Identifier.fromNamespaceAndPath("allkotowant", "main")
        );
        openEditor = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.allkotowant.open_editor",
                InputConstants.Type.KEYSYM,
                InputConstants.KEY_R,
                category
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openEditor.consumeClick()) {
                if (client.player == null || client.screen instanceof ItemEditorScreen) continue;
                if (client.screen instanceof AbstractContainerScreen<?> inventoryScreen) {
                    AbstractContainerScreenAccessor accessor = (AbstractContainerScreenAccessor) inventoryScreen;
                    Slot hovered = accessor.allkotowant$getHoveredSlot();
                    if (hovered == null) continue;
                    int menuSlot = accessor.allkotowant$getMenu().slots.indexOf(hovered);
                    EditorContext.select(hovered.getItem(), menuSlot, inventoryScreen);
                    client.setScreen(new ItemEditorScreen(Component.literal(UiText.t("All-Kotoff-Want")), inventoryScreen));
                } else if (client.screen == null) {
                    EditorContext.select(client.player.getMainHandItem(), -1, null);
                    client.setScreen(new ItemEditorScreen(Component.literal(UiText.t("All-Kotoff-Want")), null));
                }
            }
        });
    }
}
