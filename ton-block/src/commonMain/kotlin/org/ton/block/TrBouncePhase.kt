@file:Suppress("OPT_IN_USAGE")

package org.ton.block

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@JsonClassDiscriminator("@type")
@Serializable
sealed interface TrBouncePhase {
    @SerialName("tr_phase_bounce_negfunds")
    @Serializable
    object TrPhaseBounceNegFunds : TrBouncePhase

    @SerialName("tr_phase_bounce_nofunds")
    @Serializable
    data class TrPhaseBounceNoFunds(
            val msg_size: StorageUsedShort,
            val req_fwd_fees: Coins
    ) : TrBouncePhase

    @SerialName("tr_phase_bounce_ok")
    @Serializable
    data class TrPhaseBounceOk(
            val msg_size: StorageUsedShort,
            val msg_fees: Coins,
            val fwd_fees: Coins
    )
}