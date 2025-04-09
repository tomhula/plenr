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
        FontAwesomeModule
    )
}

expect fun webpackHot(): Hot?
