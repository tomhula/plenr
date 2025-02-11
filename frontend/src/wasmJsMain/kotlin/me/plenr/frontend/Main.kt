package me.plenr.frontend

import dev.kilua.*

fun main()
{
    startApplication(
        ::PlenrFrontendApp,
        BootstrapModule,
        BootstrapCssModule,
        JetpackModule,
        ImaskModule,
        TempusDominusModule,
        RsupProgressModule
    )
}
