package io.github.ptitjes.konvo.plugin.core.conversations.storage

import io.github.ptitjes.syrup.specification.*
import kotlinx.serialization.modules.*
import org.kodein.type.*

object ConversationSerializers : ExtensionPoint.Plural<SerializersModule>(generic())
