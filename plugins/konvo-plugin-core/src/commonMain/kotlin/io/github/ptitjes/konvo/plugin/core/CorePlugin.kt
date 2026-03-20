package io.github.ptitjes.konvo.plugin.core

import dev.whyoleg.sweetspi.*
import io.github.ptitjes.konvo.plugin.core.agents.*
import io.github.ptitjes.konvo.plugin.core.agents.toolkit.*
import io.github.ptitjes.konvo.plugin.core.conversations.*
import io.github.ptitjes.konvo.plugin.core.conversations.model.*
import io.github.ptitjes.konvo.plugin.core.conversations.model.events.*
import io.github.ptitjes.konvo.plugin.core.conversations.storage.*
import io.github.ptitjes.konvo.plugin.core.conversations.storage.files.*
import io.github.ptitjes.konvo.plugin.core.i18n.*
import io.github.ptitjes.konvo.plugin.core.mcp.*
import io.github.ptitjes.konvo.plugin.core.models.*
import io.github.ptitjes.konvo.plugin.core.platform.*
import io.github.ptitjes.konvo.plugin.core.prompts.*
import io.github.ptitjes.konvo.plugin.core.roleplay.*
import io.github.ptitjes.konvo.plugin.core.roleplay.providers.*
import io.github.ptitjes.konvo.plugin.core.settings.*
import io.github.ptitjes.konvo.plugin.core.tools.*
import io.github.ptitjes.syrup.*
import io.github.ptitjes.syrup.specification.*
import kotlinx.serialization.*
import kotlinx.serialization.modules.*
import org.kodein.di.*
import kotlin.coroutines.*

@ServiceProvider
object CorePlugin : Plugin {
    override val id: PluginId = PluginConfig.Id

    override fun PluginSpecificationBuilder.specification() {
        extensionPoint(I18nStringsProviders)
        exposedType<I18nManager>()

        exposedType<StoragePaths>()
        exposedType<SettingsRepository>()

        exposedType<ConversationRepository>()
        exposedType<ConversationManager>()

        exposedType<ModelManager>()
        exposedType<SettingsBasedModelManager>()
        exposedType<PromptManager>()
        exposedType<ToolManager>()
        exposedType<McpServerSpecificationsManager>()

        extensionPoint(Actions)

        Actions {
            payload<ConversationControl.TitleChange>()
            payload<ConversationControl.InviteAgent>()

            payload<Presence.Joining>()
            payload<Presence.Leaving>()
            payload<Presence.ViewNotification>()

            payload<AgentCapabilities.Messaging>()

            payload<AgentProcessing.Start>()
            payload<AgentProcessing.Cancellation>()
            payload<AgentProcessing.Failure>()
            payload<AgentProcessing.Completion>()

            payload<Messaging.Message>()

            payload<ToolUsage.Vetting>()
            payload<ToolUsage.Approval>()
            payload<ToolUsage.Notification>()
        }

        extensionPoint(InteractionProtocols)

        InteractionProtocols {
            contribution { Presence.Agent }
            contribution { AgentProcessing.TurnBased }
            contribution { ToolUsage.VettingProtocol }
        }

        extensionPoint(ConversationSerializers)

        ConversationSerializers {
            contribution {
                SerializersModule {
                    polymorphic(
                        baseClass = AgentConfiguration::class,
                        actualClass = QuestionAnswerAgentConfiguration::class,
                        serializer(),
                    )
                }
            }
        }

        exposedType<CharacterManager>()
        exposedType<FileSystemCharacterProvider>()
        exposedType<LorebookManager>()
        exposedType<FileSystemLorebookProvider>()
    }

    override fun DI.Builder.implementation() {
        import(platformModule)

        bindSingletonOf(::I18nManager)

        bindSingleton<ActionRegistry> { new(::DefaultActionRegistry) }
        bindSingleton<InteractionProtocolRegistry> { new(::DefaultInteractionProtocolRegistry) }

        bindSet<PromptProvider>()
        bindSet<ToolProvider>()
        bindSet<CharacterProvider>()
        bindSet<LorebookProvider>()

        inBindSet<PromptProvider> {
            addSingleton { new(::McpPromptProvider) }
        }

        bindSingleton { ToolPermissions(default = ToolPermission.ASK) }

        inBindSet<ToolProvider> {
            addSingleton { new(::McpToolProvider) }
        }

        bindSingleton { new(::FileSystemCharacterProvider) }
        bindSingleton { new(::FileSystemLorebookProvider) }

        inBindSet<CharacterProvider> {
            addSingleton { instance<FileSystemCharacterProvider>() }
        }

        inBindSet<LorebookProvider> {
            addSingleton { instance<FileSystemLorebookProvider>() }
        }

        bindSingleton<McpServerSpecificationsManager> {
            new(::SettingsBasedMcpServerSpecificationsManager)
        }
        bindSingleton<SettingsBasedModelManager> { new(::SettingsBasedModelManager) }
        bindSingleton<ModelManager> { instance<SettingsBasedModelManager>() }

        bindSingleton<PromptManager> { new(::DiPromptManager) }
        bindSingleton<ToolManager> { new(::DiToolManager) }
        bindSingleton<CharacterManager> { new(::DiCharacterManager) }
        bindSingleton<LorebookManager> { new(::DiLorebookManager) }

        bindSingleton<(CoroutineContext) -> McpHostSession> {
            { coroutineContext: CoroutineContext -> new(::McpHostSession, coroutineContext) }
        }

        bindSingleton<SettingsRepository> { new(::FileSystemSettingsRepository) }

        bindSet<InteractiveAgent<*>>()

        inBindSet<InteractiveAgent<*>> {
            addSingleton { new(::QuestionAnswerAgent) }
            addSingleton { new(::RoleplayAgent) }
        }

        bindSingleton { new(::AgentFactory) }

//    bindSingletonOf<ConversationRepository>(::InMemoryConversationRepository)
        bindSingleton<ConversationRepository> { new(::FileConversationRepository) }

        bindSingleton { new(::ConversationManager) }
    }
}
