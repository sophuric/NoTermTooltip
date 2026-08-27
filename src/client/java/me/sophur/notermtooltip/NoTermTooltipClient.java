package me.sophur.notermtooltip;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.hypixel.data.type.GameType;
import net.hypixel.modapi.HypixelModAPI;
import net.hypixel.modapi.packet.impl.clientbound.event.ClientboundLocationPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class NoTermTooltipClient implements ClientModInitializer {
    public static boolean onSkyblock = false, onDungeons = false;

    void registerHypixelLocationListener() {
        var hypixel = HypixelModAPI.getInstance();
        hypixel.subscribeToEventPacket(ClientboundLocationPacket.class);

        hypixel.createHandler(ClientboundLocationPacket.class, packet -> {
            onSkyblock = packet.getServerType().map(GameType.SKYBLOCK::equals).orElse(false);
            onDungeons = packet.getMode().map("dungeon"::equals).orElse(false);
        });

        ClientPlayConnectionEvents.DISCONNECT.register((_, _) -> {
            onSkyblock = false;
            onDungeons = false;
        });
    }

    public static final List<Pattern> TERMINAL_PATTERNS = Stream.of(
            "^Select all the ([A-Z ]+) items!$", // click items with color
            "^Correct all the panes!$", // on/off panes
            "^Click in order!$", // click in order
            "^Change all to same color!$", // 3x3 colours
            "^Click the button on time!$", // melody
            "^What starts with: '([A-Z])'\\?$" // click items starting with letter
    ).map(Pattern::compile).toList();

    @Override
    public void onInitializeClient() {
        registerHypixelLocationListener();
        ItemTooltipCallback.EVENT.register(NoTermTooltipClient::processTooltip);
    }

    private static void processTooltip(ItemStack stack, Item.TooltipContext context, TooltipFlag flag, List<Component> lines) {
        if (!onSkyblock || !onDungeons) return;

        var screen = Minecraft.getInstance().screen;
        if (screen == null) return;
        var title = screen.getTitle().getString(64);

        var matches = TERMINAL_PATTERNS.stream().anyMatch(p -> p.matcher(title).matches());
        if (!matches) return;

        lines.clear();
    }
}
