package org.moxqeon.bukkit.auth.listener;

import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.*;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.moxqeon.bukkit.api.lang.Language;
import org.moxqeon.bukkit.auth.AuthMC;
import org.moxqeon.bukkit.auth.api.IVerificationResult;
import org.moxqeon.bukkit.auth.event.PlayerPassVerifyEvent;
import org.moxqeon.bukkit.auth.event.PlayerVerifyEvent;
import org.moxqeon.bukkit.module.persistent.PersistentBoolean;

public final class PlayerListener implements Listener {
    private final AuthMC authMC;

    public PlayerListener(@NotNull AuthMC authMC) {
        this.authMC = authMC;
    }

    @SuppressWarnings("DataFlowIssue")
    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PersistentDataContainer container = player.getPersistentDataContainer();
        String lastIP = container.get(IVerificationResult.IP_ADDRESS_KEY, PersistentDataType.STRING);
        Long lastLoginTime = container.get(IVerificationResult.LAST_LOGIN_KEY, PersistentDataType.LONG);
        boolean ipChecked = false;
        boolean loginTimeChecked = false;
        if (authMC.getConfig().getBoolean("Login.CheckIP") && lastIP != null && lastIP.equals(player.getAddress().getHostString()))
            ipChecked = true;
        if (lastLoginTime != null && System.currentTimeMillis() - lastLoginTime < authMC.getConfig().getInt("Login.CheckLoginTime") * 1000L)
            loginTimeChecked = true;
        if (ipChecked && loginTimeChecked) {
            new PlayerPassVerifyEvent(player).callEvent();
            return;
        }
        container.set(IVerificationResult.VERIFY_KEY, PersistentBoolean.INSTANCE, false);
        container.remove(IVerificationResult.USER_CODE_KEY);
        container.remove(IVerificationResult.DEVICE_CODE_KEY);
        authMC.getVerificationManager().fetchUserCode(player);
    }

    @EventHandler
    private void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (event.hasChangedPosition() && !authMC.getVerificationManager().checkVerification(player).isVerified())
            player.teleport(event.getFrom().setDirection(event.getTo().getDirection()), PlayerTeleportEvent.TeleportCause.PLUGIN);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onPlayerInteract(PlayerInteractEvent event) {
        if (authMC.getVerificationManager().checkVerification(event.getPlayer()).isVerified()) return;
        event.setCancelled(true);
        event.getPlayer().sendMessage(Language.localize(authMC, "Hint.EventWarning", null));
    }

    @EventHandler
    private void onPlayerDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player)
            if (!authMC.getVerificationManager().checkVerification(player).isVerified()) event.setCancelled(true);
    }

    @EventHandler
    private void onPlayerVerify(PlayerVerifyEvent event) {
        Player player = event.getPlayer();
        if (event.isSuccess()) {
            player.sendMessage(Language.localize(authMC, "Hint.LoginSuccess", null));
            player.setFallDistance(0);
        } else player.sendMessage(Language.localize(authMC, "Hint.LoginFail", null));
    }

    @EventHandler
    private void onPlayerPassVerify(PlayerPassVerifyEvent event) {
        Player player = event.getPlayer();
        player.sendMessage(Language.localize(authMC, "Hint.PassLogin", null));
    }

    @EventHandler
    private void onPlayerCommand(PlayerCommandPreprocessEvent event){
        if (authMC.getVerificationManager().checkVerification(event.getPlayer()).isVerified()) return;
        event.setCancelled(true);
        event.getPlayer().sendMessage(Language.localize(authMC, "Hint.EventWarning", null));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onPlayerChat(AsyncChatEvent event) {
        if (authMC.getVerificationManager().checkVerification(event.getPlayer()).isVerified()) return;
        event.setCancelled(true);
        event.getPlayer().sendMessage(Language.localize(authMC, "Hint.EventWarning", null));
    }

    @EventHandler
    private void onToggleFlight(PlayerToggleFlightEvent event) {
        if(authMC.getVerificationManager().checkVerification(event.getPlayer()).isVerified()) return;
        event.setCancelled(true);
        event.getPlayer().sendMessage(Language.localize(authMC, "Hint.EventWarning", null));
    }
}
