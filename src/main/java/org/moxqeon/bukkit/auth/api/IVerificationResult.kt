package org.moxqeon.bukkit.auth.api

import org.moxqeon.bukkit.api.Constructor
import org.moxqeon.bukkit.auth.AuthMC
import java.util.*

interface IVerificationResult {
    companion object {
        @JvmField
        val VERIFY_KEY = Constructor.namespacedKey(AuthMC.NAMESPACE, "verified")

        @JvmField
        val MAIL_KEY = Constructor.namespacedKey(AuthMC.NAMESPACE, "mail")

        @JvmField
        val USER_CODE_KEY = Constructor.namespacedKey(AuthMC.NAMESPACE, "user_code")

        @JvmField
        val DEVICE_CODE_KEY = Constructor.namespacedKey(AuthMC.NAMESPACE, "device_code")

        @JvmField
        val FETCHING_KEY = Constructor.namespacedKey(AuthMC.NAMESPACE, "fetching")

        @JvmField
        val IP_ADDRESS_KEY = Constructor.namespacedKey(AuthMC.NAMESPACE, "ip_address")

        @JvmField
        val LAST_LOGIN_KEY = Constructor.namespacedKey(AuthMC.NAMESPACE, "last_login")
    }

    val uuid: UUID
    val isVerified: Boolean
}