package org.moxqeon.bukkit.auth;

import com.google.common.base.Preconditions;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.moxqeon.api.context.Context;
import org.moxqeon.bukkit.api.lang.Language;
import org.moxqeon.bukkit.auth.api.IVerificationResult;
import org.moxqeon.bukkit.auth.core.VerificationManager;
import org.moxqeon.bukkit.auth.lang.LanguageSupplier;
import org.moxqeon.bukkit.auth.listener.PlayerListener;
import org.moxqeon.bukkit.auth.listener.SkriptListener;
import org.moxqeon.bukkit.util.AdventureUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class AuthMC extends JavaPlugin {
    public static final String NAMESPACE = "auth";
    public static final String PREFIX = "&b[&eAuthMC&b] ";

    private static AuthMC instance;

    public static AuthMC instance() {
        return Preconditions.checkNotNull(instance);
    }

    private VerificationManager verificationManager;

    @Override
    public void onEnable() {
        Preconditions.checkState(instance == null, "Plugin shouldn't be enabled multiple times");
        instance = this;
        verificationManager = new VerificationManager(this);
        Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);
        Plugin skript;
        if ((skript = Bukkit.getPluginManager().getPlugin("Skript")) != null) {
            getLogger().info("Skript detected, enabling hook...");
            try {
                Bukkit.getPluginManager().registerEvents(new SkriptListener(this), this);
            } catch (Exception ignored) {
            }
        }
        Language.register(this);
        new LanguageSupplier(this).supply();
    }

    @Override
    public void onDisable() {
        Preconditions.checkState((instance != null), "Plugin is not enabled");
        instance = null;
        Language.unregister(this);
    }

    @NotNull
    public VerificationManager getVerificationManager() {
        return verificationManager;
    }
}
