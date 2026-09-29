package org.moxqeon.bukkit.auth.core

import org.moxqeon.bukkit.auth.api.IVerificationResult
import java.util.*

class VerificationResult : IVerificationResult {
    override val isVerified: Boolean
    override val uuid: UUID

    @JvmOverloads
    constructor(uuid: UUID,isVerified: Boolean = false ) {
        this.uuid = uuid
        this.isVerified = isVerified
    }
}