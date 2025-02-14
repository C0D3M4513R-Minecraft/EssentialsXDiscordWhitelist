package com.c0d3m4513r.plugins.essentialsxwhitelist;
import com.c0d3m4513r.config.ConfigStorage;
import com.c0d3m4513r.logger.JavaUtilLogger;
import com.c0d3m4513r.logger.Logging;
import com.c0d3m4513r.plugins.essentialsxwhitelist.discordCommands.WhitelistCommand;
import com.earth2me.essentials.IConf;
import com.earth2me.essentials.IEssentials;
import net.essentialsx.api.v2.services.discord.DiscordService;
import net.essentialsx.api.v2.services.discord.InteractionException;
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
            api.getInteractionController().registerCommand(new WhitelistCommand());
        } catch (InteractionException e) {
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
