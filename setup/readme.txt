1. creat tables and view:
SOURCE_PASSENGER.SQL      -- PARENT TABLE
PASS_ADDRESS.SQL                --CHILD TABLE

MESSAGE_LOAD.SQL              --SINGLE TABLE

CUSTOMER.SQL                      --TABLE FOR VIEW
CUSTOMER_VIEW.SQL           --VIEW

2. create queues and subscription
haSourcePassenger        -->DXB/sourcepassenger           connector_config.properties  TOPIC
haMessageLoadLVQ      -->Lvq/messageLoad                  application.yml  output  &  connector_config.properties   LVQ_TOPIC

haCustomer                    -->DXB/customer                        connector_config.properties  TOPIC
haCustomerLVQ             -->Lvq/customer                         application.yml  output  &  connector_config.properties   LVQ_TOPIC

haMessageLoad             -->DXB/messageLoad                  connector_config.properties  TOPIC
haMessageLoadLVQ      -->Lvq/messageLoad                    application.yml  output  &  connector_config.properties   LVQ_TOPIC


3.  package and run
mvn clean package -Dmaven.test.skip=true
java -jar SolaceDbPullConnector.jar

4. see the status of each node
check status
http://localhost:8085/actuator/leaderelection
http://localhost:8085/actuator/health