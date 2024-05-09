install: down up

up:
	@ echo Up service
	@ docker compose up -d --build	
	
down:
	@ echo Down services
	@ docker compose down

show-order-paid-queue:
	@ docker compose exec localstack awslocal sqs get-queue-attributes --queue-url http://sqs.us-east-1.localhost.localstack.cloud:4566/000000000000/localstack-queue-order-paid.fifo --attribute-names All
	
show-test-queue:
	@ docker compose exec localstack awslocal sqs get-queue-attributes --queue-url http://sqs.us-east-1.localhost.localstack.cloud:4566/000000000000/localstack-queue.fifo --attribute-names All

show-test-message-queue:
	@ docker compose exec localstack awslocal sqs get-queue-attributes --queue-url http://sqs.us-east-1.localhost.localstack.cloud:4566/000000000000/queuemessage.fifo --attribute-names ApproximateNumberOfMessages

list-queues:
	@ docker compose exec localstack awslocal sqs list-queues
	
check-queues:
	@ curl -s localhost:4566/_localstack/init
	
sonar:
	@ docker run --rm -v "$(pwd):/usr" --network="host" -e SONAR_HOST_URL="http://localhost:9000" -e SONAR_TOKEN="sqa_336a23aa830fa09f862c07ad75d6032f0ce3d003" -e SONAR_SCANNER_OPTS="-Dsonar.projectKey=Orderingsystem-Production" sonarsource/sonar-scanner-cli

sonar-scanner:
	@ mvnw clean verify sonar:sonar \
  		-Dsonar.projectKey=Production \
  		-Dsonar.projectName='Production' \
  		-Dsonar.host.url=http://127.0.0.1:9000 \
  		-Dsonar.token=sqp_b12951f6a83a0b69bb2d0783a39347cbdb1ce8ac
