package io.github.ptitjes.konvo.plugin.roleplay.formats

import de.infix.testBalloon.framework.core.*
import io.github.ptitjes.konvo.plugin.roleplay.formats.fixtures.*
import kotlin.test.*
import kotlin.uuid.*

val JsonCharacterCardTests by testSuite {

    CharacterCardV2Fixtures.all.forEach { (cardName, cardText) ->
        test("can read v2 character card for '$cardName'") {
            val id = Uuid.random().toString()
            val result = cardText.parseCharacterCard(id)
            assertEquals(id, result.id)
            assertEquals(cardName, result.name)
        }
    }

    CharacterCardV3Fixtures.all.forEach { (cardName, cardText) ->
        test("can read v3 character card for '$cardName'") {
            val id = Uuid.random().toString()
            val result = cardText.parseCharacterCard(id)
            assertEquals(id, result.id)
            assertEquals(cardName, result.name)
        }
    }
}
