package io.github.ptitjes.konvo.plugin.roleplay

import dev.whyoleg.sweetspi.*
import io.github.ptitjes.konvo.plugin.core.*
import io.github.ptitjes.konvo.plugin.core.agents.*
import io.github.ptitjes.konvo.plugin.core.conversations.storage.*
import io.github.ptitjes.konvo.plugin.roleplay.providers.*
import io.github.ptitjes.syrup.*
import io.github.ptitjes.syrup.specification.*
import kotlinx.serialization.*
import kotlinx.serialization.modules.*
import org.kodein.di.*

@ServiceProvider
object RoleplayPlugin : Plugin {
    override val id: PluginId = PluginConfig.Id

    override val dependencies: Set<Plugin> = setOf(CorePlugin)

    override fun PluginSpecificationBuilder.specification() {
        exposedType<CharacterManager>()
        exposedType<LorebookManager>()
        exposedType<FileSystemLorebookProvider>()

        Agents {
            contribution { new(::RoleplayAgent) }
        }

        ConversationSerializers {
            contribution {
                SerializersModule {
                    polymorphic(
                        baseClass = AgentConfiguration::class,
                        actualClass = RoleplayAgentConfiguration::class,
                        serializer(),
                    )
                }
            }
        }
    }

    override fun DI.Builder.implementation() {
        bindSingleton<CharacterManager> { new(::DefaultCharacterManager) }

        bindSet<LorebookProvider>()

        bindSingleton { new(::FileSystemLorebookProvider) }

        inBindSet<LorebookProvider> {
            addSingleton { instance<FileSystemLorebookProvider>() }
        }

        bindSingleton<LorebookManager> { new(::DiLorebookManager) }
    }
}
