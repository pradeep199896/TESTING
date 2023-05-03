package com.solace.connectors.database;

/*
 * Licensed to Solace Corporation under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * Solace licenses this file to You under the Solace Community License, Version 1.0
 * (the "License"); you may not use this file except in compliance with
 * the License. A copy of the License must be provided to you with the source code
 * distribution.

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
