package org.moxqeon.bukkit.auth.listener;

import ch.njol.skript.command.EffectCommandEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.moxqeon.bukkit.api.lang.Language;
import org.moxqeon.bukkit.auth.AuthMC;

public final class SkriptListener implements Listener {

    private final AuthMC authMC;

    public SkriptListener(@NotNull AuthMC authMC) {
        this.authMC = authMC;
    }

    @EventHandler
    private void onEffectCommand(EffectCommandEvent event) {
        if (event.getSender() instanceof Player player) {
            authMC.getLogger().info("Effect command detected");
            if (authMC.getVerificationManager().checkVerification(player).isVerified()) return;
            event.setCancelled(true);
            player.sendMessage(Language.localize(authMC, "Hint.EventWarning", null));
        }
    }
}
