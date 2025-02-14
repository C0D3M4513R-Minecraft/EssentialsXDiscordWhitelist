package com.c0d3m4513r.plugins.essentialsxwhitelist;

import com.c0d3m4513r.config.iface.provider.IConfigLoader;
import com.c0d3m4513r.config.iface.provider.IConfigLoaderSaver;
import com.c0d3m4513r.config.iface.provider.IConfigSaver;
import com.c0d3m4513r.logger.Logging;
import lombok.AllArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.common.returnsreceiver.qual.This;
import org.checkerframework.dataflow.qual.Deterministic;
import org.checkerframework.dataflow.qual.Pure;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.util.List;
import java.util.Objects;

@AllArgsConstructor
public class ConfigLoaderSaver implements IConfigLoaderSaver {
    private final @NonNull Plugin plugin;
    @NonNull
    private final YamlConfigurationLoader configurationLoader;
    @NonNull
    private ConfigurationNode root;
    public ConfigLoaderSaver(@NonNull Plugin plugin, @NonNull String configFileName) {
        this.plugin = Objects.requireNonNull(plugin);
//        this.configFileName = configFileName;
        plugin.getDataFolder().mkdirs();
        this.configFile = new File(plugin.getDataFolder(), Objects.requireNonNull(configFileName));
        try {
            configFile.createNewFile();
        } catch (Exception e) {
            Logging.INSTANCE.error("Error creating config File", e);
            plugin.disable();
        }
        configurationLoader = YamlConfigurationLoader.builder()
                .indent(2)
                .path(configFile.toPath())
                .nodeStyle(NodeStyle.BLOCK)
                .build();
        try{
            root=configurationLoader.load();
        }catch (Throwable e){
            Logging.INSTANCE.error("Error loading config file", e);
            plugin.disable();
        }
    }

    private ConfigurationNode getNode(String path){
        return root.node((Object[]) path.split("\\."));
    }

    @Pure
    @Override
    @SuppressWarnings("purity.not.deterministic.catch")
    public <T> @Nullable T loadConfigKey(@NonNull String path, @NonNull Class<T> type) {
        try {
            return getNode(path).get(type);
        } catch (Throwable e) {
            return null;
        }
    }

    @Pure
    @Override
    @SuppressWarnings({"purity.not.deterministic.not.sideeffectfree.call", "purity.not.deterministic.catch", "methodref.param"})
    public @Nullable <T> List<T> loadConfigKeyList(@NonNull String path, @NonNull Class<T> type) {
        try {
            return getNode(path).getList(type);
        } catch (Throwable e) {
            return null;
        }
    }

    @Pure
    @Override
    public @This @NonNull IConfigLoader reloadConfigLoader() {
        try {
            root = configurationLoader.load();
        } catch (Throwable e) {
            Logging.INSTANCE.error("Error reloading config file, disabling plugin:", e);
            plugin.disable();
        }
        return this;
    }

    @Pure
    @Override
    @SuppressWarnings("purity.not.deterministic.catch")
    public <T> boolean saveConfigKey(@Nullable T value, @NonNull Class<T> typeToken, @NonNull String path) {
        try {
            getNode(path).set(typeToken, value);
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    @Pure
    @Override
    @SuppressWarnings("purity.not.deterministic.catch")
    public <T> boolean saveConfigKeyList(@Nullable List<T> value, @NonNull Class<T> typeToken, @NonNull String path) {
        try{
            getNode(path).setList(typeToken, value);
            return true;
        }catch (Throwable e){
            return false;
        }
    }

    @Deterministic
    @Override
    @SuppressWarnings("purity.not.deterministic.call")
    public @This @NonNull IConfigSaver saveToConfig() {
        try {
            configurationLoader.save(root);
        }catch (Throwable e){
            Logging.INSTANCE.error("Error saving config file", e);
        }
        return this;
    }

    @Pure
    @Override
    public @Nullable File getConfigFolder() {
        return plugin.getDataFolder();
    }



    @NonNull
    private File configFile;

    @Pure
    @Override
    @SuppressWarnings({"purity.not.deterministic.object.creation","purity.not.deterministic.not.sideeffectfree.assign.field"})
    public @NonNull File getConfigFile() {
        return configFile;
    }
}