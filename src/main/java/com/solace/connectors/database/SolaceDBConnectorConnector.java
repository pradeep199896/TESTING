package com.solace.connectors.database;

import com.solace.connectors.database.mapper.SolaceToSolaceDBConnectorPayloadMapper;
import com.solace.connectors.database.mapper.SolaceDBConnectorToSolacePayloadMapper;
import com.solace.connector.core.function.MapPayloadsFunctions;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
* Main class of connector.
*/
@SpringBootApplication
@EnableJpaRepositories(basePackages = {"com.solace.connectors.database.sink.entity","com.solace.connectors.database.source.entity"})
@EntityScan(basePackages = {"com.solace.connectors.database.sink.entity","com.solace.connectors.database.source.entity"})
public class SolaceDBConnectorConnector {

	public static void main(String[] args) {
		SpringApplication.run(SolaceDBConnectorConnector.class, args);
	}

	@Bean
	public MapPayloadsFunctions.BinderAwarePayloadMapper solaceToSolaceDBConnectorPayloadMapper() {
		return new SolaceToSolaceDBConnectorPayloadMapper();
	}

	@Bean
	public MapPayloadsFunctions.BinderAwarePayloadMapper SolaceDBConnectorToSolacePayloadMapper() {
		return new SolaceDBConnectorToSolacePayloadMapper();
	}
}
