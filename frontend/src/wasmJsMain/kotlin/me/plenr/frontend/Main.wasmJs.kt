package me.plenr.frontend

import dev.kilua.Hot

/* Hot module replacement is not supported on Wasm */
actual fun webpackHot(): Hot? = null
