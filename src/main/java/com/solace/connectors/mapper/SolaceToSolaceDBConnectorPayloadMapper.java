package com.solace.connectors.mapper;

import com.solace.connector.core.service.SimpleFromSolaceBinderAwarePayloadMapper;

import java.util.Set;

/**
 * Payload mapper to map payloads from Solace to solace-db-source.
 */
public class SolaceToSolaceDBConnectorPayloadMapper extends SimpleFromSolaceBinderAwarePayloadMapper {

    @Override
    public boolean supports(Set<String> inputBinders, Set<String> outputBinders) {
        return inputBinders.contains("solace") && outputBinders.contains("solace-db");
    }

}
