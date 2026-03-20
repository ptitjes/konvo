package io.github.ptitjes.konvo.plugin.core.conversations.storage.files

import io.github.ptitjes.syrup.*
import io.github.ptitjes.syrup.specification.*
import org.kodein.di.*

open class MockPluginContext : PluginContext {
    override fun <T : Any> contribution(extensionPoint: ExtensionPoint.Singular<T>): LazyDelegate<T> {
        error("Unexpected call to MockPluginContext.contribution($extensionPoint)")
    }

    override fun <T : Any> contributionOrNull(extensionPoint: ExtensionPoint.Singular<T>): LazyDelegate<T?> {
        error("Unexpected call to MockPluginContext.contributionOrNull($extensionPoint)")
    }

    override fun <T : Any> contributions(extensionPoint: ExtensionPoint.Plural<T>): LazyDelegate<Set<T>> {
        error("Unexpected call to MockPluginContext.contributions($extensionPoint)")
    }

    override fun <T : Any> sourcedContribution(extensionPoint: ExtensionPoint.Singular<T>): LazyDelegate<Sourced<T>> {
        error("Unexpected call to MockPluginContext.sourcedContribution($extensionPoint)")
    }

    override fun <T : Any> sourcedContributionOrNull(extensionPoint: ExtensionPoint.Singular<T>): LazyDelegate<Sourced<T>?> {
        error("Unexpected call to MockPluginContext.sourcedContributionOrNull($extensionPoint)")
    }

    override fun <T : Any> sourcedContributions(extensionPoint: ExtensionPoint.Plural<T>): LazyDelegate<Set<Sourced<T>>> {
        error("Unexpected call to MockPluginContext.sourcedContributions($extensionPoint)")
    }
}
