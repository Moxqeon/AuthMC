package org.moxqeon.bukkit.auth.core

import org.moxqeon.api.Accessor
import org.moxqeon.bukkit.auth.AuthMC
import org.moxqeon.ext.subFile

class BanManager(override val access: AuthMC): Accessor<AuthMC> {
    private val banFile = access.dataFolder.subFile("ban.json")

    private val bans = mutableSetOf<String>()

    fun isBanned(mail: String): Boolean = bans.contains(mail)
}