package com.solace.connector.db.pull;

import com.solace.connector.core.service.LifecycleResolver;
import com.solace.connector.db.pull.service.DataCollecttingService;
import com.solace.connector.db.pull.service.TriggerService;
import com.solace.connector.utility.ConnectorUtilities;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@SpringBootApplication(scanBasePackages = {"com.solace.connector.db"})
@ConfigurationPropertiesScan(basePackages = "com.solace.connector.db.config")
@Slf4j
public class SolaceDbPullConnectorApplication {

	static ConfigurableApplicationContext context;

	public static void main(String[] args) {

		context =  SpringApplication.run(SolaceDbPullConnectorApplication.class, args);
		if(DataCollecttingService.ready!=null && !DataCollecttingService.ready.get()) {
			log.error(ConnectorUtilities.exitMessage);
			System.exit(SpringApplication.exit(context, () -> 2));
		}
	}



	@Component
	static class Runner implements CommandLineRunner {

		@Resource
		TriggerService triggerService;
		@Resource
		LifecycleResolver lifecycleResolver;
		@Override
		public void run(String... args) throws Exception {
			if(LifecycleResolver.LeaderElectionState.ACTIVE.equals(lifecycleResolver.getLeaderElectionState())){
				Thread.sleep(5000);
				triggerService.startPolling();
			}
		}
	}


}
	