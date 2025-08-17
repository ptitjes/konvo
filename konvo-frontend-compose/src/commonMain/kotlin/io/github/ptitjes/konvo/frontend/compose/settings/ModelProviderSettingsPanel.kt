package io.github.ptitjes.konvo.frontend.compose.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.*
import io.github.ptitjes.konvo.core.models.providers.*
import io.github.ptitjes.konvo.core.settings.*
import io.github.ptitjes.konvo.frontend.compose.components.*
import io.github.ptitjes.konvo.frontend.compose.components.settings.*

private enum class ProviderType { Ollama, Anthropic, OpenAI, Google }

@Composable
fun ModelProviderSettingsPanel(
    settings: ModelProviderSettings,
    updateSettings: (updater: (previous: ModelProviderSettings) -> ModelProviderSettings) -> Unit,
) {
    fun addProvider(newProvider: NamedModelProvider) {
        updateSettings { previous -> previous.copy(providers = previous.providers + newProvider) }
    }

    fun updateProvider(index: Int, transform: (previous: NamedModelProvider) -> NamedModelProvider) {
        updateSettings { previous ->
            previous.copy(providers = previous.providers.mapIndexed { i, provider ->
                if (i == index) transform(provider) else provider
            })
        }
    }

    fun removeProvider(index: Int) {
        updateSettings { previous ->
            previous.copy(providers = previous.providers.filterIndexed { i, _ -> i != index })
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SettingsBox(
            title = "Configured providers",
            description = "Add, remove, and edit model providers.",
        ) {
            if (settings.providers.isEmpty()) {
                Text(
                    text = "No model providers configured.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    settings.providers.forEachIndexed { index, provider ->
                        ProviderEditor(
                            provider = provider,
                            otherNames = settings.providers.map { it.name }.toSet() - provider.name,
                            onChange = { updated -> updateProvider(index) { _ -> updated } },
                            onRemove = { removeProvider(index) },
                        )
                        if (index < settings.providers.lastIndex) HorizontalDivider()
                    }
                }
            }
        }

        AddProviderBox(
            existingNames = settings.providers.map { it.name }.toSet(),
            onAdd = { newProvider -> addProvider(newProvider) }
        )
    }
}

@Composable
private fun ProviderEditor(
    provider: NamedModelProvider,
    otherNames: Set<String>,
    onChange: (NamedModelProvider) -> Unit,
    onRemove: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                modifier = Modifier.weight(1f),
                value = provider.name,
                onValueChange = { newName -> onChange(provider.copy(name = newName)) },
                label = { Text("Name") },
                isError = provider.name.isBlank() || otherNames.contains(provider.name),
                singleLine = true,
            )

            var type by remember(provider.configuration) { mutableStateOf(provider.configuration.toType()) }
            GenericSelector(
                label = "Type",
                selectedItem = type,
                onSelectItem = { selected ->
                    type = selected
                    val newConfig = buildNewConfiguration(selected, provider)
                    onChange(provider.copy(configuration = newConfig))
                },
                options = ProviderType.entries,
                itemLabeler = { it.name },
                modifier = Modifier.widthIn(min = 180.dp).weight(0.7f),
            )

            IconButton(onClick = onRemove) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Remove provider")
            }
        }

        when (val conf = provider.configuration) {
            is ModelProviderConfiguration.Ollama -> {
                OutlinedTextField(
                    value = conf.url,
                    onValueChange = { newUrl -> onChange(provider.copy(configuration = conf.copy(url = newUrl))) },
                    label = { Text("Ollama base URL") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            is ModelProviderConfiguration.Anthropic -> {
                OutlinedTextField(
                    value = conf.apiKey,
                    onValueChange = { newKey -> onChange(provider.copy(configuration = conf.copy(apiKey = newKey))) },
                    label = { Text("Anthropic API key") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            is ModelProviderConfiguration.OpenAI -> {
                OutlinedTextField(
                    value = conf.apiKey,
                    onValueChange = { newKey -> onChange(provider.copy(configuration = conf.copy(apiKey = newKey))) },
                    label = { Text("OpenAI API key") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            is ModelProviderConfiguration.Google -> {
                OutlinedTextField(
                    value = conf.apiKey,
                    onValueChange = { newKey -> onChange(provider.copy(configuration = conf.copy(apiKey = newKey))) },
                    label = { Text("Google API key") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        // Optional helper text for name validity
        if (provider.name.isBlank()) {
            Text(
                text = "Name cannot be empty",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        } else if (otherNames.contains(provider.name)) {
            Text(
                text = "Name must be unique",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

private fun buildNewConfiguration(
    selected: ProviderType,
    provider: NamedModelProvider,
): ModelProviderConfiguration = when (selected) {
    ProviderType.Ollama -> when (val configuration = provider.configuration) {
        is ModelProviderConfiguration.Ollama -> configuration
        else -> ModelProviderConfiguration.Ollama(url = DEFAULT_OLLAMA_URL)
    }

    ProviderType.Anthropic -> when (val configuration = provider.configuration) {
        is ModelProviderConfiguration.Anthropic -> configuration
        else -> ModelProviderConfiguration.Anthropic(apiKey = "")
    }

    ProviderType.OpenAI -> when (val configuration = provider.configuration) {
        is ModelProviderConfiguration.OpenAI -> configuration
        else -> ModelProviderConfiguration.OpenAI(apiKey = "")
    }

    ProviderType.Google -> when (val configuration = provider.configuration) {
        is ModelProviderConfiguration.Google -> configuration
        else -> ModelProviderConfiguration.Google(apiKey = "")
    }
}

@Composable
private fun AddProviderBox(
    existingNames: Set<String>,
    onAdd: (NamedModelProvider) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(ProviderType.Ollama) }
    var ollamaUrl by remember { mutableStateOf(DEFAULT_OLLAMA_URL) }
    var anthropicKey by remember { mutableStateOf("") }
    var openAIKey by remember { mutableStateOf("") }
    var googleKey by remember { mutableStateOf("") }

    fun isValid(): Boolean = name.isNotBlank() && !existingNames.contains(name) && when (type) {
        ProviderType.Ollama -> ollamaUrl.isNotBlank()
        ProviderType.Anthropic -> anthropicKey.isNotBlank()
        ProviderType.OpenAI -> openAIKey.isNotBlank()
        ProviderType.Google -> googleKey.isNotBlank()
    }

    SettingsBox(
        title = "Add provider",
        description = "Create a new model provider entry.",
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    isError = name.isBlank() || existingNames.contains(name),
                )

                GenericSelector(
                    label = "Type",
                    selectedItem = type,
                    onSelectItem = { type = it },
                    options = ProviderType.entries,
                    itemLabeler = { it.name },
                    modifier = Modifier.widthIn(min = 180.dp).weight(0.7f),
                )

                FilledTonalIconButton(
                    onClick = {
                        val configuration: ModelProviderConfiguration = when (type) {
                            ProviderType.Ollama -> ModelProviderConfiguration.Ollama(url = ollamaUrl)
                            ProviderType.Anthropic -> ModelProviderConfiguration.Anthropic(apiKey = anthropicKey)
                            ProviderType.OpenAI -> ModelProviderConfiguration.OpenAI(apiKey = openAIKey)
                            ProviderType.Google -> ModelProviderConfiguration.Google(apiKey = googleKey)
                        }
                        onAdd(NamedModelProvider(name = name, configuration = configuration))

                        // Reset form
                        name = ""
                        type = ProviderType.Ollama
                        ollamaUrl = DEFAULT_OLLAMA_URL
                        anthropicKey = ""
                    },
                    enabled = isValid(),
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add provider")
                }
            }

            when (type) {
                ProviderType.Ollama -> {
                    OutlinedTextField(
                        value = ollamaUrl,
                        onValueChange = { ollamaUrl = it },
                        label = { Text("Ollama base URL") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                ProviderType.Anthropic -> {
                    OutlinedTextField(
                        value = anthropicKey,
                        onValueChange = { anthropicKey = it },
                        label = { Text("Anthropic API key") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                ProviderType.OpenAI -> {
                    OutlinedTextField(
                        value = openAIKey,
                        onValueChange = { openAIKey = it },
                        label = { Text("OpenAI API key") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                ProviderType.Google -> {
                    OutlinedTextField(
                        value = googleKey,
                        onValueChange = { googleKey = it },
                        label = { Text("Google API key") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            if (name.isBlank()) {
                Text(
                    text = "Name cannot be empty",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            } else if (existingNames.contains(name)) {
                Text(
                    text = "Name must be unique",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

private fun ModelProviderConfiguration.toType(): ProviderType = when (this) {
    is ModelProviderConfiguration.Ollama -> ProviderType.Ollama
    is ModelProviderConfiguration.Anthropic -> ProviderType.Anthropic
    is ModelProviderConfiguration.OpenAI -> ProviderType.OpenAI
    is ModelProviderConfiguration.Google -> ProviderType.Google
}
