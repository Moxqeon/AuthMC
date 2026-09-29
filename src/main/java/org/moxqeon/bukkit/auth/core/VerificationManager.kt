package org.moxqeon.bukkit.auth.core

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType
import org.moxqeon.api.Accessor
import org.moxqeon.bukkit.api.context.PlayerContext
import org.moxqeon.bukkit.api.lang.localize
import org.moxqeon.bukkit.auth.AuthMC
import org.moxqeon.bukkit.auth.api.IVerificationResult
import org.moxqeon.bukkit.auth.event.PlayerVerifyEvent
import org.moxqeon.bukkit.module.persistent.PersistentBoolean
import org.moxqeon.util.HttpUtil
import java.net.Proxy
import java.nio.charset.StandardCharsets
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class VerificationManager(override val access: AuthMC) : Accessor<AuthMC> {
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()
    private val proxy: Proxy? =
        if (access.config.getBoolean("Proxy.Enabled", false)) HttpUtil.proxy(access.config.getString("Proxy.Address")!!)
        else null
    private val formDataHeader = mutableMapOf(
        Pair("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:134.0) Gecko/20100101 Firefox/134.0"),
        Pair("Content-Type", "application/x-www-form-urlencoded")
    )
    private val userCodeFormData = mutableMapOf(
        Pair("client_id", access.config.getString("Microsoft.ClientId")!!),
        Pair("tenant", "consumers"),
        Pair("scope", "User.Read")
    )
    private val tokenFormData = mutableMapOf(
        Pair("grant_type", "device_code"),
        Pair("client_id", access.config.getString("Microsoft.ClientId")!!),
        Pair("scope", "XboxLive.signin User.Read")
    )

    init {
        access.logger.info("Verification Manager init")
        Bukkit.getScheduler().runTaskTimerAsynchronously(access, Runnable {
            for (player in Bukkit.getOnlinePlayers()) {
                if (checkVerification(player).isVerified) continue
                val container = player.persistentDataContainer
                if (container.has(IVerificationResult.USER_CODE_KEY, PersistentDataType.STRING)) {
                    val hintUserCode = "Hint.UserCode".localize(access, PlayerContext(access, "Hint.UserCode", player))
                    player.sendMessage(hintUserCode)
                    val hintLogin = "Hint.Login".localize(access, null)
                    player.sendMessage(hintLogin)
                    val deviceCode =
                        container.get(IVerificationResult.DEVICE_CODE_KEY, PersistentDataType.STRING) ?: continue
                    tokenFormData["device_code"] = deviceCode
                    val tokenObj = HttpUtil.postJsonResponse(
                        "https://login.microsoftonline.com/consumers/oauth2/v2.0/token",
                        proxy,
                        formDataHeader,
                        HttpUtil.encodeFormData(tokenFormData).toByteArray(StandardCharsets.UTF_8),
                        StandardCharsets.UTF_8
                    ) ?: continue
                    if (tokenObj.has("error")) continue
                    val tokenMS = tokenObj.get("access_token").asString
                    val authorizationHeader = mutableMapOf(Pair("Authorization", tokenMS))
                    val responseObj = HttpUtil.getJsonResponse(
                        "https://graph.microsoft.com/v1.0/me", proxy, authorizationHeader, StandardCharsets.UTF_8
                    ) ?: continue
                    val mail =
                        if (responseObj.has("mail")) responseObj.get("mail").asString else responseObj.get("userPrincipalName").asString;
                    tryBind(player, mail)
                    verify(player, mail)
                }

            }
        }, 0, 100)
    }

    fun fetchUserCode(player: Player) {
        val container = player.persistentDataContainer
        container.set(IVerificationResult.FETCHING_KEY, PersistentBoolean.INSTANCE, true)
        executor.submit {
            val codeObj = HttpUtil.postJsonResponse(
                "https://login.microsoftonline.com/consumers/oauth2/v2.0/devicecode",
                proxy,
                formDataHeader,
                HttpUtil.encodeFormData(userCodeFormData).toByteArray(
                    StandardCharsets.UTF_8
                ),
                StandardCharsets.UTF_8
            ) { _ -> fetchUserCode(player) }
            if (codeObj != null) {
                container.set(
                    IVerificationResult.USER_CODE_KEY, PersistentDataType.STRING, codeObj.get("user_code").asString
                )
                container.set(
                    IVerificationResult.DEVICE_CODE_KEY, PersistentDataType.STRING, codeObj.get("device_code").asString
                )
                container.set(IVerificationResult.FETCHING_KEY, PersistentBoolean.INSTANCE, false)
            }
        }
    }

    fun getMail(player: Player): String? {
        return player.persistentDataContainer.get(IVerificationResult.MAIL_KEY, PersistentDataType.STRING)
    }

    fun tryBind(player: Player, mail: String) {
        if (getMail(player) == null) {
            player.persistentDataContainer.set(
                IVerificationResult.MAIL_KEY, PersistentDataType.STRING, mail
            )
            player.sendMessage("Hint.BindSuccess".localize(access, null))
        }
    }

    fun forceBind(player: Player, mail: String) {
        player.persistentDataContainer.set(IVerificationResult.MAIL_KEY, PersistentDataType.STRING, mail)
    }

    fun verify(player: Player, mail: String): Boolean {
        access.logger.info("try verifying mail: $mail")
        val container = player.persistentDataContainer
        if (container.get(IVerificationResult.MAIL_KEY, PersistentDataType.STRING) == mail) {
            container.set(IVerificationResult.VERIFY_KEY, PersistentBoolean.INSTANCE, true)
            container.remove(IVerificationResult.USER_CODE_KEY)
            Bukkit.getScheduler().runTask(access, Runnable {
                PlayerVerifyEvent(player, true).callEvent()
            })
            container.set(IVerificationResult.IP_ADDRESS_KEY, PersistentDataType.STRING, player.address.hostString)
            container.set(IVerificationResult.LAST_LOGIN_KEY, PersistentDataType.LONG, System.currentTimeMillis())
            return true
        } else {
            Bukkit.getScheduler().runTask(access, Runnable {
                PlayerVerifyEvent(player, false).callEvent()
            })
            return false
        }
    }

    fun checkVerification(player: Player): IVerificationResult {
        val container = player.persistentDataContainer
        return if (container.has(IVerificationResult.VERIFY_KEY, PersistentDataType.BYTE)) {
            VerificationResult(
                player.uniqueId, container.get(IVerificationResult.VERIFY_KEY, PersistentBoolean.INSTANCE)!!
            )
        } else {
            VerificationResult(player.uniqueId)
        }
    }

}