package za.co.psybergate.chatterbox.application.port.out.vendor.factory;

import za.co.psybergate.chatterbox.application.common.exception.ApplicationException;
import za.co.psybergate.chatterbox.application.domain.event.model.OutboundEvent;
import za.co.psybergate.chatterbox.application.port.out.vendor.model.VendorPayloadDefinitionPort;

import java.util.Map;

public interface VendorFactoryPort {

    /// From a given [Map] of property values, create and populate the
    /// payload representation
    VendorPayloadDefinitionPort buildDefinition(Map<String, String> values);

    /// From a given [OutboundEvent] create a [Map] and leverage implementation factory
    /// to create the internal payload representation
    VendorPayloadDefinitionPort buildDefinition(OutboundEvent outboundEvent);

    String getAsPayloadString(OutboundEvent outboundEvent) throws ApplicationException;

}
