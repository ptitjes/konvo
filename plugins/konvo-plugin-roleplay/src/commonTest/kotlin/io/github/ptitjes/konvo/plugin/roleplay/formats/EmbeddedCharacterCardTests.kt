package io.github.ptitjes.konvo.plugin.roleplay.formats

import de.infix.testBalloon.framework.core.*
import io.github.ptitjes.konvo.plugin.roleplay.formats.fixtures.*
import kotlin.test.*
import kotlin.uuid.*

val EmbeddedCharacterCardTests by testSuite {

    EmbeddedCharacterCardFixtures.all.forEach { (cardName, cardBytes) ->
        test("can read embedded character card for '$cardName'") {
            val id = Uuid.random().toString()
            val result = cardBytes.extractCharacterCard(id)
            assertEquals(id, result.id)
            assertEquals(cardName, result.name)
        }
    }
}
