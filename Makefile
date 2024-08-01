install: down up

down-all: down down-local sonarqube-down

build-image:
	@ docker compose -f docker-compose-local.yml build application
	@ docker rmi 637423186279.dkr.ecr.us-east-1.amazonaws.com/ordering-system-prod:latest
	@ docker tag ordering-system-prod:latest 637423186279.dkr.ecr.us-east-1.amazonaws.com/ordering-system-prod:latest

build:
	@ .\mvnw clean install -Ppackage

build-image-local:
	@ docker build -f .\Dockerfile.local -t ordering-system-prod:latest .
	@ docker rmi -f 637423186279.dkr.ecr.us-east-1.amazonaws.com/ordering-system-prod:latest
	@ docker tag ordering-system-prod:latest 637423186279.dkr.ecr.us-east-1.amazonaws.com/ordering-system-prod:latest

up:
	@ echo Up service
	@ docker compose up -d --build
	
publish-ecr:
	@ docker push 637423186279.dkr.ecr.us-east-1.amazonaws.com/ordering-system-prod:latest

up-local:
	@ echo Up service
	@ docker compose -f docker-compose-local.yml up -d	
	
up-local-app:
	@ echo Up service
	@ docker compose -f docker-compose-local.yml up -d application
	
down:
	@ echo Down services
	@ docker compose down --volumes
	
down-local:
	@ echo Down services
	@ docker compose -f docker-compose-local.yml down --volumes --remove-orphans
	
down-local-app:
	@ echo Down application container
	@ docker compose -f docker-compose-local.yml down application --volumes --remove-orphans

sonarqube-up:
	@ docker compose -f sonarqube.yml up -d

sonarqube-down:
	@ docker compose -f sonarqube.yml down --volumes

sonarqube-publish:
	@ .\mvnw sonar:sonar
	
sonarqube-analyze: build sonarqube-publish

send-message-to-queue:
	docker compose exec localstack awslocal sqs send-message \
		--queue-url http://sqs.us-east-1.localhost.localstack.cloud:4566/000000000000/${queue-name} \
		--message-group-id "${queue-name}" \
		--message-deduplication-id "${uuid}-${status}" \
		--message-body "{ \"orderId\": \"${uuid}\", \"status\": \"${status}\" }"
		
send-message-to-queue-order-ordered-event-success:
	make send-message-to-queue queue-name=order-ordered-event.fifo uuid=${uuid} status=RESERVED

send-message-to-queue-order-ordered-event-failed:
	make send-message-to-queue queue-name=order-ordered-event.fifo uuid=${uuid} status=FAILED


send-message-to-queue-bill-performed-event-success:
	make send-message-to-queue queue-name=bill-performed-event.fifo uuid=${uuid} status=PAID

send-message-to-queue-bill-performed-event-failed:
	make send-message-to-queue queue-name=bill-performed-event.fifo uuid=${uuid} status=FAILED


send-message-to-queue-invoice-issued-event-success:
	make send-message-to-queue queue-name=invoice-issued-event.fifo uuid=${uuid} status=INVOICE_ISSUED

send-message-to-queue-invoice-issued-event-failed:
	make send-message-to-queue queue-name=invoice-issued-event.fifo uuid=${uuid} status=FAILED


send-message-to-queue-order-scheduled-event-success:
	make send-message-to-queue queue-name=order-scheduled-event.fifo uuid=${uuid} status=SCHEDULED

send-message-to-queue-order-scheduled-event-failed:
	make send-message-to-queue queue-name=order-scheduled-event.fifo uuid=${uuid} status=FAILED

create-localstack-queues:
	@ docker compose exec localstack awslocal sqs create-queue --queue-name queue-order-stock-command.fifo --attributes "FifoQueue=true"
	
	@ docker compose exec localstack awslocal sqs create-queue --queue-name queue-perform-billing-command.fifo --attributes "FifoQueue=true"

	@ docker compose exec localstack awslocal sqs create-queue --queue-name queue-invoice-command.fifo --attributes "FifoQueue=true"

	@ docker compose exec localstack awslocal sqs create-queue --queue-name queue-schedule-order-command.fifo --attributes "FifoQueue=true"

	@ docker compose exec localstack awslocal sqs create-queue --queue-name queue-cancel-order-stock-command.fifo --attributes "FifoQueue=true"

list-all-queues:
	@ docker compose exec localstack awslocal sqs list-queues

count-all-queues: list-all-attributes-bill-performed-event list-all-attributes-order-scheduled-event list-all-attributes-order-created-event list-all-attributes-order-ordered-event list-all-attributes-invoice-issued-event list-all-attributes-queue-order-stock-command list-all-attributes-queue-perform-billing-command list-all-attributes-queue-invoice-command list-all-attributes-queue-schedule-order-command list-all-attributes-queue-cancel-order-stock-command

list-all-attributes-bill-performed-event:
	@ docker compose exec localstack awslocal sqs get-queue-attributes --queue-url http://sqs.us-east-1.localhost.localstack.cloud:4566/000000000000/bill-performed-event.fifo --attribute-names ApproximateNumberOfMessages QueueArn

list-all-attributes-order-scheduled-event:
	@ docker compose exec localstack awslocal sqs get-queue-attributes --queue-url http://sqs.us-east-1.localhost.localstack.cloud:4566/000000000000/order-scheduled-event.fifo --attribute-names ApproximateNumberOfMessages QueueArn

list-all-attributes-order-created-event:
	@ docker compose exec localstack awslocal sqs get-queue-attributes --queue-url http://sqs.us-east-1.localhost.localstack.cloud:4566/000000000000/order-created-event.fifo --attribute-names ApproximateNumberOfMessages QueueArn

list-all-attributes-order-ordered-event:
	@ docker compose exec localstack awslocal sqs get-queue-attributes --queue-url http://sqs.us-east-1.localhost.localstack.cloud:4566/000000000000/order-ordered-event.fifo --attribute-names ApproximateNumberOfMessages QueueArn

list-all-attributes-invoice-issued-event:
	@ docker compose exec localstack awslocal sqs get-queue-attributes --queue-url http://sqs.us-east-1.localhost.localstack.cloud:4566/000000000000/invoice-issued-event.fifo --attribute-names ApproximateNumberOfMessages QueueArn

list-all-attributes-queue-order-stock-command:
	@ docker compose exec localstack awslocal sqs get-queue-attributes --queue-url http://sqs.us-east-1.localhost.localstack.cloud:4566/000000000000/queue-order-stock-command.fifo --attribute-names ApproximateNumberOfMessages QueueArn

list-all-attributes-queue-perform-billing-command:
	@ docker compose exec localstack awslocal sqs get-queue-attributes --queue-url http://sqs.us-east-1.localhost.localstack.cloud:4566/000000000000/queue-perform-billing-command.fifo --attribute-names ApproximateNumberOfMessages QueueArn

list-all-attributes-queue-invoice-command:
	@ docker compose exec localstack awslocal sqs get-queue-attributes --queue-url http://sqs.us-east-1.localhost.localstack.cloud:4566/000000000000/queue-invoice-command.fifo --attribute-names ApproximateNumberOfMessages QueueArn

list-all-attributes-queue-schedule-order-command:
	@ docker compose exec localstack awslocal sqs get-queue-attributes --queue-url http://sqs.us-east-1.localhost.localstack.cloud:4566/000000000000/queue-schedule-order-command.fifo --attribute-names ApproximateNumberOfMessages QueueArn

list-all-attributes-queue-cancel-order-stock-command:
	@ docker compose exec localstack awslocal sqs get-queue-attributes --queue-url http://sqs.us-east-1.localhost.localstack.cloud:4566/000000000000/queue-cancel-order-stock-command.fifo --attribute-names ApproximateNumberOfMessages QueueArn

purge-all-queues:
	@ docker compose exec localstack awslocal sqs purge-queue --queue-url http://sqs.us-east-1.localhost.localstack.cloud:4566/000000000000/bill-performed-event.fifo
	@ docker compose exec localstack awslocal sqs purge-queue --queue-url http://sqs.us-east-1.localhost.localstack.cloud:4566/000000000000/order-scheduled-event.fifo
	@ docker compose exec localstack awslocal sqs purge-queue --queue-url http://sqs.us-east-1.localhost.localstack.cloud:4566/000000000000/order-created-event.fifo
	@ docker compose exec localstack awslocal sqs purge-queue --queue-url http://sqs.us-east-1.localhost.localstack.cloud:4566/000000000000/order-ordered-event.fifo
	@ docker compose exec localstack awslocal sqs purge-queue --queue-url http://sqs.us-east-1.localhost.localstack.cloud:4566/000000000000/invoice-issued-event.fifo
	@ docker compose exec localstack awslocal sqs purge-queue --queue-url http://sqs.us-east-1.localhost.localstack.cloud:4566/000000000000/queue-order-stock-command.fifo
	@ docker compose exec localstack awslocal sqs purge-queue --queue-url http://sqs.us-east-1.localhost.localstack.cloud:4566/000000000000/queue-perform-billing-command.fifo
	@ docker compose exec localstack awslocal sqs purge-queue --queue-url http://sqs.us-east-1.localhost.localstack.cloud:4566/000000000000/queue-invoice-command.fifo
	@ docker compose exec localstack awslocal sqs purge-queue --queue-url http://sqs.us-east-1.localhost.localstack.cloud:4566/000000000000/queue-schedule-order-command.fifo
	@ docker compose exec localstack awslocal sqs purge-queue --queue-url http://sqs.us-east-1.localhost.localstack.cloud:4566/000000000000/queue-cancel-order-stock-command.fifo

up-dev:
	@ docker compose -f docker-compose-local.yml up -d

down-dev:
	@ docker compose -f docker-compose-local.yml down -v

logs-dev:
	@ docker compose -f docker-compose-local.yml logs -f localstack