package org.moxqeon.bukkit.auth.lang;

import com.google.common.base.Preconditions;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.moxqeon.bukkit.api.lang.Language;
import org.moxqeon.bukkit.auth.AuthMC;
import org.moxqeon.bukkit.auth.api.IVerificationResult;
import org.moxqeon.bukkit.util.AdventureUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class LanguageSupplier {
    private final AuthMC authMC;

    public LanguageSupplier(@NotNull AuthMC authMC) {
        this.authMC = authMC;
    }

    public void supply() {
        Language.registerSpecial(authMC, new String[]{"Hint.Login", "Hint.Register"}, ((component, context) -> {
            List<Component> components = new ArrayList<>();
            for (Component child : AdventureUtil.recursive(component)) {
                if (child instanceof TextComponent textChild) {
                    switch (textChild.content()) {
                        case "{link}":
                            Component hover = Language.localize(authMC, "Hover.LoginButton", null);
                            components.add(Component.text("■").style(child.style()).clickEvent(ClickEvent.openUrl("https://www.microsoft.com/link")).hoverEvent(HoverEvent.showText(hover)));
                            continue;
                        case "{prefix}":
                            components.add(MiniMessage.miniMessage().deserialize(AuthMC.PREFIX));
                            continue;
                    }
                }
                components.add(child);
            }
            return Component.join(JoinConfiguration.separator(Component.empty()), components);
        }));
        Language.registerSpecial(authMC, "Hint.UserCode", (component, context) -> {
            List<Component> components = new ArrayList<>();
            Player player = Bukkit.getPlayer(context.get("uuid", UUID.class));
            if (player == null) throw new RuntimeException();
            PersistentDataContainer container = player.getPersistentDataContainer();
            boolean userCodeReady = container.has(IVerificationResult.USER_CODE_KEY, PersistentDataType.STRING);
            for (Component child : AdventureUtil.recursive(component)) {
                if (child instanceof TextComponent textChild) {
                    if (textChild.content().equals("{prefix}")) {
                        components.add(MiniMessage.miniMessage().deserialize(AuthMC.PREFIX));
                    } else {
                        Component hover;
                        if (userCodeReady) {
                            hover = Language.localize(authMC, "Hover.UserCode.Ready", null);
                            String userCode = Preconditions.checkNotNull(container.get(IVerificationResult.USER_CODE_KEY, PersistentDataType.STRING));
                            components.add(Component.text(textChild.content()).style(child.style()).clickEvent(ClickEvent.copyToClipboard(userCode)).hoverEvent(HoverEvent.showText(hover)));
                        } else {
                            hover = Language.localize(authMC, "Hover.UserCode.NotReady", null);
                            components.add(Component.text(textChild.content()).style(child.style()).hoverEvent(HoverEvent.showText(hover)));
                        }
                    }
                } else {
                    components.add(child);
                }
            }
            return Component.join(JoinConfiguration.separator(Component.empty()), components);
        });
        Language.registerSpecial(authMC, new String[]{"Hint.BindSuccess", "Hint.LoginSuccess", "Hint.LoginFail", "Hint.EventWarning", "Hint.PassLogin"}, (component, context) -> {
            List<Component> components = new ArrayList<>();
            for (Component child : AdventureUtil.recursive(component)) {
                if (child instanceof TextComponent textChild) {
                    if (textChild.content().equals("{prefix}"))
                        components.add(MiniMessage.miniMessage().deserialize(AuthMC.PREFIX));
                    else components.add(child);
                } else components.add(child);
            }
            return Component.join(JoinConfiguration.separator(Component.empty()), components);
        });
    }
}