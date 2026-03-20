package io.github.ptitjes.konvo.plugin.core.conversations.model

import io.github.ptitjes.konvo.plugin.core.conversations.model.Actions.Contribution
import io.github.ptitjes.syrup.specification.*
import org.kodein.type.*
import kotlin.reflect.*

object Actions : ExtensionPoint.Plural<Contribution<*>>(generic()) {
    @ConsistentCopyVisibility
    data class Contribution<P : Action.Payload> @PublishedApi internal constructor(
        val klass: KClass<P>,
        val deprecatedNames: Set<String>,
    )
}

inline fun <reified P : Action.Payload> PluralContributionBuilder<Contribution<*>>.payload(
    vararg deprecatedNames: String,
) {
    contribution {
        Contribution(
            klass = P::class,
            deprecatedNames = deprecatedNames.toSet(),
        )
    }
}
