package com.c0d3m4513r.plugins.essentialsxwhitelist;

import com.c0d3m4513r.config.ClassValue;
import com.c0d3m4513r.config.ConfigEntry.ConfigEntry;
import com.c0d3m4513r.config.ConfigEntry.ListConfigEntry;
import com.c0d3m4513r.config.iface.key.IConfigLoadableSaveable;
import com.c0d3m4513r.config.qual.LoadableSaveable;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Arrays;

@Data
@Setter(AccessLevel.NONE)
@NoArgsConstructor
public class Config implements IConfigLoadableSaveable {
    public static final Config Instance = new Config();

    @NonNull
    @LoadableSaveable
    private final ConfigEntry<Boolean> enable = new ConfigEntry<>(new ClassValue<>(Boolean.TRUE, Boolean.class), "enable");

    @NonNull
    @LoadableSaveable
    private final ListConfigEntry<String> add_allowed_role_ids = new ListConfigEntry<>(new ClassValue<>(Arrays.asList(), String.class), "add_allowed_role_ids");

    @NonNull
    @LoadableSaveable
    private final ListConfigEntry<String> remove_allowed_role_ids = new ListConfigEntry<>(new ClassValue<>(Arrays.asList(), String.class), "remove_allowed_role_ids");
}
