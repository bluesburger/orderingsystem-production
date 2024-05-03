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
