package dev.gideonwhite1029.iseeyou.anticheat

import dev.gideonwhite1029.horizon.replay.Photographer

data class SuspiciousPhotographer(
    val photographer: Photographer,
    val name: String,
    val lastTagged: Long,
)
