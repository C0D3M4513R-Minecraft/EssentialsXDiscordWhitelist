package com.c0d3m4513r.plugins.essentialsxwhitelist;
import com.c0d3m4513r.config.ConfigStorage;
import com.c0d3m4513r.logger.JavaUtilLogger;
import com.c0d3m4513r.logger.Logging;
import com.c0d3m4513r.plugins.essentialsxwhitelist.discordCommands.WhitelistCommand;
import com.earth2me.essentials.IConf;
import com.earth2me.essentials.IEssentials;
import net.essentialsx.api.v2.services.discord.DiscordService;
import net.essentialsx.dep.net.dv8tion.jda.api.JDA;
import net.essentialsx.dep.net.dv8tion.jda.api.interactions.commands.OptionType;
import net.essentialsx.dep.net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Plugin extends JavaPlugin implements IConf {
    public Plugin(){
        ConfigStorage.setConfigLoaderSaver(new ConfigLoaderSaver(this, "config.yml"));
        Config.Instance.loadValue();
        Config.Instance.saveValue();
    }
    
    @Override
    public void onEnable() {
        Logging.setConfigLogger(new JavaUtilLogger<>(getLogger()));

        IEssentials ess = (IEssentials) getServer().getPluginManager().getPlugin("Essentials");
        ess.addReloadListener(this);
        final DiscordService api = Bukkit.getServicesManager().load(DiscordService.class);
        try {
            final JDA jda = api.getUnsafe().getJDAInstance();
            jda
                .upsertCommand("whitelist", "Configure the Whitelist of the server from discord")
                .addSubcommands(
                    new SubcommandData("add-uuid", "Adds a UUID to the Whitelist").addOption(OptionType.STRING, "uuid", "UUID of the player", true),
                    new SubcommandData("remove-uuid", "Removes a UUID to the Whitelist").addOption(OptionType.STRING, "uuid", "UUID of the player", true),
                    new SubcommandData("add-java", "Adds a java username to the Whitelist").addOption(OptionType.STRING, "username", "Username of the player", true),
                    new SubcommandData("remove-java", "Removes a java username to the Whitelist").addOption(OptionType.STRING, "username", "Username of the player", true),
                    new SubcommandData("add-bedrock", "Adds a bedrock username to the Whitelist").addOption(OptionType.STRING, "username", "Username of the player", true),
                    new SubcommandData("remove-bedrock", "Removes a bedrock username to the Whitelist").addOption(OptionType.STRING, "username", "Username of the player", true)
                ).queue();
            
            jda.addEventListener(new WhitelistCommand(this));
        } catch (Exception e) {
            setEnabled(false);
            Logging.INSTANCE.error("Disabling plugin: Could not register command: ", e);
        }
    }

    @Override
    public void reloadConfig(){
        ConfigStorage.getConfigLoaderSaver().reloadConfigLoader();
        Config.Instance.loadValue();
    }
    
    public void disable(){
        setEnabled(false);
    }
}
