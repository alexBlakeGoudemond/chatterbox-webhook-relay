package com.webhook.relay.chatterbox.common.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import com.webhook.relay.chatterbox.adapter.out.teams.model.TeamsAdaptiveCardDefinition;

/// Reference Material:
/// [Example MS Teams Webhook JSON](https://learn.microsoft.com/en-us/microsoftteams/platform/webhooks-and-connectors/how-to/add-incoming-webhook?tabs=newteams%2Cdotnet#example)
///
/// Example:
/// ```
/// {
///   "type": "message",
///   "attachments": [
///     {
///       "contentType": "application/vnd.microsoft.card.adaptive",
///       "content": {
///         "type": "AdaptiveCard",
///         "msteams": {
///           "width": "Full"
///         },
///         "body": [
///           {
///             "type": "TextBlock",
///             "text": "📢 Push Event for alexBlakeGoudemond/chatterbox",
///             "wrap": true
///           },
///           {
///             "type": "TextBlock",
///             "text": "⁉️ Details: [Test message Is here!](http://localhost:abcd) created by alexBlakeGoudemond",
///             "wrap": true
///  }
///         ],
///         "version": "1.0",
///         "$schema": "http://adaptivecards.io/schemas/adaptiveson"
///    }
//     }

///   ]
/// }
/// ```
@Data
@ConfigurationProperties(prefix = "chatterbox.deliveries.teams")
public class ChatterboxDeliveryTeamsProperties {

    private TeamsAdaptiveCardDefinition adaptiveCardDefinition;

}
