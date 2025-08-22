package io.github.ptitjes.konvo.frontend.compose.toolkit.viewmodels

import androidx.compose.runtime.*
import androidx.lifecycle.*
import org.kodein.di.compose.*

@Composable
inline fun <reified T : ViewModel> viewModel(): T {
    val viewModel by rememberInstance<T>()
    return viewModel
}

@Composable
inline fun <reified T : ViewModel, reified A : Any> viewModel(arg: A): T {
    val factory by rememberFactory<A, T>()
    return remember(arg) { factory(arg) }
}
