package io.github.ptitjes.konvo.frontend.compose.toolkit.viewmodels

import androidx.compose.runtime.*
import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.*
import androidx.lifecycle.viewmodel.compose.*
import org.kodein.di.compose.*
import org.kodein.di.compose.viewmodel.*
import org.kodein.type.*
import androidx.lifecycle.viewmodel.compose.viewModel as androidxViewModel

@Composable
inline fun <reified VM : ViewModel> viewModel(
    viewModelStoreOwner: ViewModelStoreOwner =
        checkNotNull(LocalViewModelStoreOwner.current) {
            "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
        },
    tag: String? = null,
): VM =
    with(localDI()) {
        androidxViewModel(
            modelClass = VM::class,
            viewModelStoreOwner = viewModelStoreOwner,
            key = null,
            KodeinViewModelScopedSingleton(di = di, tag = tag),
            extras = if (viewModelStoreOwner is HasDefaultViewModelProviderFactory) {
                viewModelStoreOwner.defaultViewModelCreationExtras
            } else {
                CreationExtras.Empty
            }
        )
    }


@Composable
inline fun <reified VM : ViewModel> viewModel(
    viewModelStoreOwner: ViewModelStoreOwner =
        checkNotNull(LocalViewModelStoreOwner.current) {
            "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
        },
    tag: String? = null,
    key: String,
): VM = with(localDI()) {
    androidxViewModel(
        modelClass = VM::class,
        viewModelStoreOwner = viewModelStoreOwner,
        key = key,
        KodeinViewModelScopedFactory(
            di = di,
            argType = generic<String>(),
            arg = key,
            tag = tag
        ),
        extras = if (viewModelStoreOwner is HasDefaultViewModelProviderFactory) {
            viewModelStoreOwner.defaultViewModelCreationExtras
        } else {
            CreationExtras.Empty
        }
    )
}
