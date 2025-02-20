package com.c0d3m4513r.plugins.essentialsxwhitelist.discordCommands;

import com.c0d3m4513r.plugins.essentialsxwhitelist.Config;
import com.c0d3m4513r.plugins.essentialsxwhitelist.Plugin;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.essentialsx.dep.net.dv8tion.jda.api.entities.Member;
import net.essentialsx.dep.net.dv8tion.jda.api.entities.Role;
import net.essentialsx.dep.net.dv8tion.jda.api.entities.User;
import net.essentialsx.dep.net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.essentialsx.dep.net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.essentialsx.dep.net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.essentialsx.dep.net.dv8tion.jda.api.utils.FileUpload;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.geysermc.floodgate.api.FloodgateApi;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

public class WhitelistCommand extends ListenerAdapter {
    
    @NonNull  final Plugin plugin;
    public WhitelistCommand(@NonNull Plugin plugin) {
        this.plugin = plugin;
    }

    public static @Nullable OfflinePlayer getOfflinePlayer(@NonNull SlashCommandInteractionEvent event) {
        final OptionMapping om = event.getOption("uuid");
        if (om == null) {
            event.reply("Did not pass a user argument, when one was expected.").setEphemeral(true).queue();
            return null;
        }
        final String mc_uuid = om.getAsString();

        final UUID uuid;
        try {
            uuid = UUID.fromString(mc_uuid);
        }catch (IllegalArgumentException e) {
            event.reply("The supplied minecraft player uuid was not a valid uuid").queue();
            return null;
        }
        return Bukkit.getOfflinePlayer(uuid);
    }

    public void setWhitelistBedrock(@NonNull SlashCommandInteractionEvent event, boolean whitelisted) {
        final String username = event.getOption("username", null, OptionMapping::getAsString);
        if (username == null) {
            event.reply("No username provided").setEphemeral(true).queue();
            return;
        }
        final FloodgateApi api = FloodgateApi.getInstance();
        api.getUuidFor(username).whenCompleteAsync((uuid, throwable)->{
            if (uuid != null) {
                setPlayerWhitelist(event, Bukkit.getOfflinePlayer(uuid), whitelisted);
                return;
            }
            Exception e = new Exception("Couldn't get user UUID");
            if (throwable != null){
                e.addSuppressed(throwable);
            }
            getUUIDOfBedrockPlayer(username).whenCompleteAsync(((uuid1, throwable1) -> {
                if (uuid1 != null) {
                    setPlayerWhitelist(event, Bukkit.getOfflinePlayer(uuid1), whitelisted);
                    return;
                }

                if (throwable1 != null) {
                    e.addSuppressed(throwable1);
                }
                event.reply("There was an error executing that command.\nPlease use the Floodgate-UUID returned by https://mcprofile.io/ and use `/whitelist add-uuid` command instead.")
                        .addFiles(FileUpload.fromData(e.toString().getBytes(), "exception.txt"))
                        .queue();
            }));
        });
    }

    private static CompletableFuture<UUID> getUUIDOfBedrockPlayer(String username) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                final URL url = new URL("https://playerdb.co/api/player/xbox/" + username);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Content-Length", "0");

                connection.setUseCaches(false);
                connection.setDoOutput(false);


                //Get Response  
                InputStream is = connection.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = rd.readLine()) != null) {
                    response.append(line);
                    response.append('\r');
                }
                rd.close();
                
                JsonElement element = new JsonParser().parse(response.toString());
                JsonObject obj = element.getAsJsonObject();
                String rawuuidstring = obj.get("data").getAsJsonObject().get("player").getAsJsonObject().get("id").getAsString();
                long uuid_decoded = Long.parseLong(rawuuidstring);
                return new UUID(0, uuid_decoded);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static boolean checkRole(@NonNull SlashCommandInteractionEvent event, List<Long> role_list, List<Long> user_list) {
        final User user = event.getUser();
        if (new HashSet<>(user_list).contains(user.getIdLong())) {
            return true;
        }
        
        final Member member = event.getMember();
        if (member == null) {
            event.reply("Cannot get Member from Slash-Command Interaction.").setEphemeral(true).queue();
            return false;
        }

        Set<Long> roles = new HashSet<>(role_list);
        if (member.getRoles().stream().mapToLong(Role::getIdLong).anyMatch(roles::contains)) {
            return true;
        }
        
        event.reply("You are not allowed to use this command").setEphemeral(true).queue();
        return false;
    }

    public void setPlayerWhitelist(@NonNull SlashCommandInteractionEvent event, @NonNull OfflinePlayer player, boolean whitelisted) {
        Bukkit.getScheduler().runTask(this.plugin, ()->player.setWhitelisted(whitelisted));

        String name = player.getName();
        if (name == null)
            name = "player";
        else
            name = "player `" + name + "`";

        if (whitelisted)
            event.reply("Added "+ name +" to Whitelist").queue();
        else
            event.reply("Removed "+ name +" from Whitelist").queue();
    }
    
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        final OfflinePlayer player;
        final FloodgateApi api;
        final String username;
        final UUID uuid;
        
        if (event.getName().equals("whitelist")) {
            final String subcommand = event.getSubcommandName();
            if (subcommand == null) {
                event.reply("Could not get subcommand").setEphemeral(true).queue();
                return;
            }

            switch (subcommand) {
                case "add-uuid":
                    if (!checkRole(event, Config.Instance.getAdd_allowed_role_ids().getValue(), Config.Instance.getAdd_allowed_user_ids().getValue())) return;
                    player = getOfflinePlayer(event);
                    if (player == null) return;
                    setPlayerWhitelist(event, player, true);

                    break;
                case "remove-uuid":
                    if (!checkRole(event, Config.Instance.getRemove_allowed_role_ids().getValue(), Config.Instance.getRemove_allowed_user_ids().getValue())) return;
                    player = getOfflinePlayer(event);
                    if (player == null) return;
                    setPlayerWhitelist(event, player, false);

                    break;
                case "add-java":
                    if (!checkRole(event, Config.Instance.getAdd_allowed_role_ids().getValue(), Config.Instance.getAdd_allowed_user_ids().getValue())) return;
                    username = event.getOption("username", null, OptionMapping::getAsString);
                    if (username == null) {
                        event.reply("No username provided").setEphemeral(true).queue();
                        return;
                    }

                    player = Bukkit.getOfflinePlayer(username);
                    if (player == null) {
                        event.reply("Could not get UUID from username").queue();
                        return;
                    }
                    
                    setPlayerWhitelist(event, player, true);
                    break;
                case "remove-java":
                    if (!checkRole(event, Config.Instance.getRemove_allowed_role_ids().getValue(), Config.Instance.getRemove_allowed_user_ids().getValue())) return;
                    username = event.getOption("username", null, OptionMapping::getAsString);
                    if (username == null) {
                        event.reply("No username provided").setEphemeral(true).queue();
                        return;
                    }

                    player = Bukkit.getOfflinePlayer(username);
                    if (player == null) {
                        event.reply("Could not get UUID from username").queue();
                        return;
                    }

                    setPlayerWhitelist(event, player, false);

                    break;
                case "add-bedrock":
                    if (!checkRole(event, Config.Instance.getAdd_allowed_role_ids().getValue(), Config.Instance.getAdd_allowed_user_ids().getValue())) return;
                    setWhitelistBedrock(event, true);

                    break;
                case "remove-bedrock":
                    if (!checkRole(event, Config.Instance.getRemove_allowed_role_ids().getValue(), Config.Instance.getRemove_allowed_user_ids().getValue())) return;
                    setWhitelistBedrock(event, false);

                    break;
                default:
                    event.reply("Unknown Subcommand").setEphemeral(true).queue();
            }
        }
    }
}
