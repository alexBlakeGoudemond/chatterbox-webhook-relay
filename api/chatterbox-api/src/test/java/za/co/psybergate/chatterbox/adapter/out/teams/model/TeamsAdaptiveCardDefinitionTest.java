package za.co.psybergate.chatterbox.adapter.out.teams.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TeamsAdaptiveCardDefinitionTest {

    @Test
    void testDefaultValues() {
        TeamsAdaptiveCardDefinition definition = new TeamsAdaptiveCardDefinition();
        assertEquals("message", definition.getType());

        TeamsAdaptiveCardDefinition.Attachment attachment = new TeamsAdaptiveCardDefinition.Attachment();
        assertEquals("application/vnd.microsoft.card.adaptive", attachment.getContentType());

        TeamsAdaptiveCardDefinition.Attachment.Content content = new TeamsAdaptiveCardDefinition.Attachment.Content();
        assertEquals("AdaptiveCard", content.getType());
        assertEquals("http://adaptivecards.io/schemas/adaptive-card.json", content.getSchema());
        assertEquals("1.0", content.getVersion());

        TeamsAdaptiveCardDefinition.Attachment.MsTeams msTeams = new TeamsAdaptiveCardDefinition.Attachment.MsTeams();
        assertEquals("Full", msTeams.getWidth());

        TeamsAdaptiveCardDefinition.Attachment.BodyItem bodyItem = new TeamsAdaptiveCardDefinition.Attachment.BodyItem();
        assertEquals("TextBlock", bodyItem.getType());
        assertTrue(bodyItem.isWrap());
    }

    @Test
    void testSettersAndGetters() {
        TeamsAdaptiveCardDefinition definition = new TeamsAdaptiveCardDefinition();
        TeamsAdaptiveCardDefinition.Attachment attachment = new TeamsAdaptiveCardDefinition.Attachment();
        TeamsAdaptiveCardDefinition.Attachment.Content content = new TeamsAdaptiveCardDefinition.Attachment.Content();
        TeamsAdaptiveCardDefinition.Attachment.MsTeams msTeams = new TeamsAdaptiveCardDefinition.Attachment.MsTeams();
        TeamsAdaptiveCardDefinition.Attachment.BodyItem bodyItem = new TeamsAdaptiveCardDefinition.Attachment.BodyItem();

        setValues(bodyItem, msTeams, content, attachment, definition);

        assertEquals("notification", definition.getType());
        assertEquals(1, definition.getAttachments().size());

        TeamsAdaptiveCardDefinition.Attachment actualAttachment = definition.getAttachments().get(0);
        assertEquals("custom/type", actualAttachment.getContentType());

        TeamsAdaptiveCardDefinition.Attachment.Content actualContent = actualAttachment.getContent();
        assertEquals("CustomContent", actualContent.getType());
        assertEquals("2.0", actualContent.getVersion());
        assertEquals("http://custom-schema.json", actualContent.getSchema());
        assertEquals("Small", actualContent.getMsteams().getWidth());

        assertEquals(1, actualContent.getBody().size());
        TeamsAdaptiveCardDefinition.Attachment.BodyItem actualBodyItem = actualContent.getBody().get(0);
        assertEquals("Hello World", actualBodyItem.getText());
        assertEquals("CustomType", actualBodyItem.getType());
        assertFalse(actualBodyItem.isWrap());
    }

    private void setValues(TeamsAdaptiveCardDefinition.Attachment.BodyItem bodyItem, TeamsAdaptiveCardDefinition.Attachment.MsTeams msTeams, TeamsAdaptiveCardDefinition.Attachment.Content content, TeamsAdaptiveCardDefinition.Attachment attachment, TeamsAdaptiveCardDefinition definition) {
        bodyItem.setText("Hello World");
        bodyItem.setType("CustomType");
        bodyItem.setWrap(false);

        msTeams.setWidth("Small");

        content.setMsteams(msTeams);
        content.setBody(List.of(bodyItem));
        content.setType("CustomContent");
        content.setVersion("2.0");
        content.setSchema("http://custom-schema.json");

        attachment.setContent(content);
        attachment.setContentType("custom/type");

        definition.setType("notification");
        definition.setAttachments(List.of(attachment));
    }

}
