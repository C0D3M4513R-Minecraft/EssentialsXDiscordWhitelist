package com.c0d3m4513r.plugins.essentialsxwhitelist.discordCommands;

import com.c0d3m4513r.plugins.essentialsxwhitelist.Config;
import net.essentialsx.api.v2.services.discord.InteractionCommand;
import net.essentialsx.api.v2.services.discord.InteractionCommandArgument;
import net.essentialsx.api.v2.services.discord.InteractionCommandArgumentType;
import net.essentialsx.api.v2.services.discord.InteractionEvent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Pure;

import java.util.*;

public class WhitelistCommand implements InteractionCommand {
    @Override
    @Pure
    public boolean isDisabled() {
        return !Config.Instance.getEnable().getValue();
    }

    @Override
    @Pure
    public boolean isEphemeral() {
        return false;
    }

    @Override
    @Pure
    public String getName() {
        return "whitelist";
    }

    @Override
    @Pure
    public String getDescription() {
        return "Configure the Whitelist of a server from discord";
    }

    @Override
    @Pure
    public List<InteractionCommandArgument> getArguments() {
        return Collections.unmodifiableList(Arrays.asList(
                new InteractionCommandArgument("action", "Action to take", InteractionCommandArgumentType.STRING, true),
                new InteractionCommandArgument("user", "Minecraft Player UUID", InteractionCommandArgumentType.STRING, false)
        ));
    }

    public static @Nullable OfflinePlayer getOfflinePlayer(@NonNull InteractionEvent event) {
        final String mc_uuid = event.getStringArgument("user");
        if (mc_uuid == null) {
            event.reply("Did not pass a user, when one was expected.");
            return null;
        }

        final UUID uuid;
        try {
            uuid = UUID.fromString(mc_uuid);
        }catch (IllegalArgumentException e) {
            event.reply("The supplied minecraft player uuid was not a valid uuid");
            return null;
        }
        return Bukkit.getOfflinePlayer(uuid);
    }
    public static boolean checkRole(@NonNull InteractionEvent event, List<String> roles) {
        for (final String role: roles){
            if (event.getMember().hasRole(role)) {
                return true;
            }
        }
        return false;
    }

    public static void setPlayerWhitelist(@NonNull InteractionEvent event, boolean whitelisted) {
        final OfflinePlayer player = getOfflinePlayer(event);
        if (player == null) return;

        player.setWhitelisted(whitelisted);

        String name = player.getName();
        if (name == null) 
            name = "player";
        else
            name = "player `" + name + "`";

        if (whitelisted)
            event.reply("Added "+ name +" to Whitelist");
        else
            event.reply("Removed "+ name +" from Whitelist");
    }

    @Override
    public void onCommand(InteractionEvent event) {
        switch (event.getStringArgument("action")) {
            case "add":
                if (!checkRole(event, Config.Instance.getAdd_allowed_role_ids().getValue())) {
                    event.reply("You are not allowed to use this command");
                    return;
                }
                setPlayerWhitelist(event, true);
                return;
            case "remove":
                if (!checkRole(event, Config.Instance.getRemove_allowed_role_ids().getValue())) {
                    event.reply("You are not allowed to use this command");
                    return;
                }
                setPlayerWhitelist(event, false);
                return;
            default:
                event.reply("Unknown action. Supported actions are \"add\" and \"remove\"");
        }
    }
}
