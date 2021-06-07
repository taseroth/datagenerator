quick and dirty java app to constantly fire transactions against a neo4j database. Uses spring boot and comes with a docker build file and docker-compose example.

The data itself is generated via Faker API.

The spring boot app needs 3 parameters (in that order):
* number of concurrent threads to spawn
* batch size (number of data changes in one transaction)
* sleep time in milliseconds between transactions
