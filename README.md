# DMR
A sample executable Java program which retrieve the Heap and Non-Heap Utilization from a Wildfly Server. 

## Build process 
Build it using the following command:
```shell
mvn clean install
```
Post successful completion of the build process it will generate a jar at target folder `HealthMonitoring-jar-with-dependencies.jar`

## Execution process
Execute it using the following command 
```shell
java -jar HealthMonitoring-jar-with-dependencies.jar -H 10.10.10.10 -P 9990 -U user -P secret
```
**Note:** Please change the values as per your environement before building this program. 
