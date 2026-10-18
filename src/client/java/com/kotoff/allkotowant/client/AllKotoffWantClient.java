package com.kotoff.allkotowant.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import net.minecraft.network.chat.Component;

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
                if (client.player != null && client.screen == null) {
                    client.setScreen(new ItemEditorScreen(Component.literal("All-Kotoff-Want")));
                }
            }
        });
    }
}
