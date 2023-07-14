package com.solace.connectors.mapper;

import com.solace.connector.core.service.SimpleToSolaceBinderAwarePayloadMapper;

import java.util.Set;

/**
* Payload mapper to map payloads from solace-db-source to Solace.
*/
public class SolaceDBConnectorToSolacePayloadMapper extends SimpleToSolaceBinderAwarePayloadMapper {

    @Override
    public boolean supports(Set<String> inputBinders, Set<String> outputBinders) {
        return inputBinders.contains("solace-db") && outputBinders.contains("solace");
    }

}
