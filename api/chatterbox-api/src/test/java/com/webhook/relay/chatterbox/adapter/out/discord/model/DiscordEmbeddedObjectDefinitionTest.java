package com.webhook.relay.chatterbox.adapter.out.discord.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DiscordEmbeddedObjectDefinitionTest {

    private DiscordEmbeddedObjectDefinition.EmbeddedObject embeddedObject;

    private DiscordEmbeddedObjectDefinition.EmbeddedObject.Author author;

    @BeforeEach
    void setUp() {
        DiscordEmbeddedObjectDefinition.EmbeddedObject embeddedObject = new DiscordEmbeddedObjectDefinition.EmbeddedObject();
        DiscordEmbeddedObjectDefinition.EmbeddedObject.Author author = new DiscordEmbeddedObjectDefinition.EmbeddedObject.Author();
        this.embeddedObject = embeddedObject;
        this.author = author;
    }

    @Test
    void whenCreate_ThenDefaultValuesExist() {
        assertEquals(6993, embeddedObject.getColor());
        assertEquals("https://github.githubassets.com/images/modules/logos_page/GitHub-Mark.png", author.getIcon_url());
    }

    @Test
    void whenSettersFields_ThenValuesAreCorrectlySet() {
        DiscordEmbeddedObjectDefinition definition = new DiscordEmbeddedObjectDefinition();

        author.setName("Author Name");
        author.setIcon_url("http://custom-icon.png");

        embeddedObject.setTitle("Title");
        embeddedObject.setDescription("Description");
        embeddedObject.setUrl("http://url");
        embeddedObject.setColor(12345);
        embeddedObject.setAuthor(author);

        definition.setEmbeds(List.of(embeddedObject));

        assertEquals(1, definition.getEmbeds().size());
        assertEquals("Title", definition.getEmbeds().getFirst().getTitle());
        assertEquals("Description", definition.getEmbeds().getFirst().getDescription());
        assertEquals("http://url", definition.getEmbeds().getFirst().getUrl());
        assertEquals(12345, definition.getEmbeds().getFirst().getColor());
        assertEquals("Author Name", definition.getEmbeds().getFirst().getAuthor().getName());
        assertEquals("http://custom-icon.png", definition.getEmbeds().getFirst().getAuthor().getIcon_url());
    }

}
