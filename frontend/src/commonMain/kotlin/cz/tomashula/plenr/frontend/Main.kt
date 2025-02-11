package cz.tomashula.plenr.frontend

import dev.kilua.*

fun main()
{
    startApplication(
        ::PlenrFrontendApp,
        webpackHot(),
        BootstrapModule,
        BootstrapCssModule,
        JetpackModule,
        ImaskModule,
        TempusDominusModule,
        RsupProgressModule
    )
}

expect fun webpackHot(): Hot?
