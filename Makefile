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
	
create-localstack-queues:
	@ docker compose exec localstack awslocal sqs create-queue --queue-name order-created-event.fifo --attributes "FifoQueue=true"
	@ docker compose exec localstack awslocal sqs create-queue --queue-name queue-order-stock-command.fifo --attributes "FifoQueue=true"
	@ docker compose exec localstack awslocal sqs create-queue --queue-name queue-cancel-order-stock-command.fifo --attributes "FifoQueue=true"
	@ docker compose exec localstack awslocal sqs create-queue --queue-name order-stock-failed-event.fifo --attributes "FifoQueue=true"
	@ docker compose exec localstack awslocal sqs create-queue --queue-name queue-order-stock-command.fifo --attributes "FifoQueue=true"
	@ docker compose exec localstack awslocal sqs create-queue --queue-name invoice-issued-event.fifo --attributes "FifoQueue=true"
	@ docker compose exec localstack awslocal sqs create-queue --queue-name order-scheduled-event.fifo --attributes "FifoQueue=true"
	@ docker compose exec localstack awslocal sqs create-queue --queue-name bill-performed-event.fifo --attributes "FifoQueue=true"
	@ docker compose exec localstack awslocal sqs create-queue --queue-name order-ordered-event.fifo --attributes "FifoQueue=true"
	@ docker compose exec localstack awslocal sqs create-queue --queue-name stock-failed-event.fifo --attributes "FifoQueue=true"
	@ docker compose exec localstack awslocal sqs create-queue --queue-name order-failed-delivery.fifo --attributes "FifoQueue=true"
	@ docker compose exec localstack awslocal sqs create-queue --queue-name perform-billing-failed-event.fifo --attributes "FifoQueue=true"
	@ docker compose exec localstack awslocal sqs create-queue --queue-name queue-perform-billing-command.fifo --attributes "FifoQueue=true"
	@ docker compose exec localstack awslocal sqs create-queue --queue-name queue-cancel-bill-command.fifo --attributes "FifoQueue=true"
	@ docker compose exec localstack awslocal sqs create-queue --queue-name queue-invoice-command.fifo --attributes "FifoQueue=true"
	@ docker compose exec localstack awslocal sqs create-queue --queue-name queue-cancel-invoice-command.fifo --attributes "FifoQueue=true"
	@ docker compose exec localstack awslocal sqs create-queue --queue-name queue-schedule-order-command.fifo --attributes "FifoQueue=true"
	@ docker compose exec localstack awslocal sqs create-queue --queue-name queue-order-confirmed-command.fifo --attributes "FifoQueue=true"
	
purge-all-localstack-queues:
	@ docker compose exec localstack awslocal sqs create-queue --queue-name order-created-event.fifo --attributes "FifoQueue=true"
	@ docker compose exec localstack awslocal sqs purge-queue --queue-url http://sqs.us-east-1.localhost.localstack.cloud:4566/000000000000/queue-order-stock-command.fifo
	@ docker compose exec localstack awslocal sqs purge-queue --queue-url http://sqs.us-east-1.localhost.localstack.cloud:4566/000000000000/queue-cancel-order-stock-command.fifo
	@ docker compose exec localstack awslocal sqs purge-queue --queue-url http://sqs.us-east-1.localhost.localstack.cloud:4566/000000000000/order-stock-failed-event.fifo
	@ docker compose exec localstack awslocal sqs purge-queue --queue-url http://sqs.us-east-1.localhost.localstack.cloud:4566/000000000000/queue-order-stock-command.fifo
	@ docker compose exec localstack awslocal sqs purge-queue --queue-url http://sqs.us-east-1.localhost.localstack.cloud:4566/000000000000/invoice-issued-event.fifo
	@ docker compose exec localstack awslocal sqs purge-queue --queue-url http://sqs.us-east-1.localhost.localstack.cloud:4566/000000000000/order-scheduled-event.fifo
	@ docker compose exec localstack awslocal sqs purge-queue --queue-url http://sqs.us-east-1.localhost.localstack.cloud:4566/000000000000/bill-performed-event.fifo
	@ docker compose exec localstack awslocal sqs purge-queue --queue-url http://sqs.us-east-1.localhost.localstack.cloud:4566/000000000000/order-ordered-event.fifo
	@ docker compose exec localstack awslocal sqs purge-queue --queue-url http://sqs.us-east-1.localhost.localstack.cloud:4566/000000000000/stock-failed-event.fifo
	@ docker compose exec localstack awslocal sqs purge-queue --queue-url http://sqs.us-east-1.localhost.localstack.cloud:4566/000000000000/order-failed-delivery.fifo
	@ docker compose exec localstack awslocal sqs purge-queue --queue-url http://sqs.us-east-1.localhost.localstack.cloud:4566/000000000000/perform-billing-failed-event.fifo
	@ docker compose exec localstack awslocal sqs purge-queue --queue-url http://sqs.us-east-1.localhost.localstack.cloud:4566/000000000000/queue-perform-billing-command.fifo
	@ docker compose exec localstack awslocal sqs purge-queue --queue-url http://sqs.us-east-1.localhost.localstack.cloud:4566/000000000000/queue-schedule-order-command.fifo

count-all-messages:
	docker compose exec localstack awslocal sqs get-queue-attributes --queue-url http://sqs.us-east-1.localhost.localstack.cloud:4566/000000000000/queue-order-stock-command.fifo --attribute-names ApproximateNumberOfMessages ApproximateNumberOfMessagesNotVisible
	docker compose exec localstack awslocal sqs get-queue-attributes --queue-url http://sqs.us-east-1.localhost.localstack.cloud:4566/000000000000/queue-order-stock-command.fifo --attribute-names ApproximateNumberOfMessages ApproximateNumberOfMessagesNotVisible
	docker compose exec localstack awslocal sqs get-queue-attributes --queue-url http://sqs.us-east-1.localhost.localstack.cloud:4566/000000000000/queue-cancel-order-stock-command.fifo --attribute-names ApproximateNumberOfMessages ApproximateNumberOfMessagesNotVisible
	docker compose exec localstack awslocal sqs get-queue-attributes --queue-url http://sqs.us-east-1.localhost.localstack.cloud:4566/000000000000/order-stock-failed-event.fifo --attribute-names ApproximateNumberOfMessages ApproximateNumberOfMessagesNotVisible
	docker compose exec localstack awslocal sqs get-queue-attributes --queue-url http://sqs.us-east-1.localhost.localstack.cloud:4566/000000000000/queue-order-stock-command.fifo --attribute-names ApproximateNumberOfMessages ApproximateNumberOfMessagesNotVisible
	docker compose exec localstack awslocal sqs get-queue-attributes --queue-url http://sqs.us-east-1.localhost.localstack.cloud:4566/000000000000/invoice-issued-event.fifo --attribute-names ApproximateNumberOfMessages ApproximateNumberOfMessagesNotVisible
	docker compose exec localstack awslocal sqs get-queue-attributes --queue-url http://sqs.us-east-1.localhost.localstack.cloud:4566/000000000000/order-scheduled-event.fifo --attribute-names ApproximateNumberOfMessages ApproximateNumberOfMessagesNotVisible
	docker compose exec localstack awslocal sqs get-queue-attributes --queue-url http://sqs.us-east-1.localhost.localstack.cloud:4566/000000000000/bill-performed-event.fifo --attribute-names ApproximateNumberOfMessages ApproximateNumberOfMessagesNotVisible
	docker compose exec localstack awslocal sqs get-queue-attributes --queue-url http://sqs.us-east-1.localhost.localstack.cloud:4566/000000000000/order-ordered-event.fifo --attribute-names ApproximateNumberOfMessages ApproximateNumberOfMessagesNotVisible
	docker compose exec localstack awslocal sqs get-queue-attributes --queue-url http://sqs.us-east-1.localhost.localstack.cloud:4566/000000000000/stock-failed-event.fifo --attribute-names ApproximateNumberOfMessages ApproximateNumberOfMessagesNotVisible
	docker compose exec localstack awslocal sqs get-queue-attributes --queue-url http://sqs.us-east-1.localhost.localstack.cloud:4566/000000000000/order-failed-delivery.fifo --attribute-names ApproximateNumberOfMessages ApproximateNumberOfMessagesNotVisible
	docker compose exec localstack awslocal sqs get-queue-attributes --queue-url http://sqs.us-east-1.localhost.localstack.cloud:4566/000000000000/perform-billing-failed-event.fifo --attribute-names ApproximateNumberOfMessages ApproximateNumberOfMessagesNotVisible
	docker compose exec localstack awslocal sqs get-queue-attributes --queue-url http://sqs.us-east-1.localhost.localstack.cloud:4566/000000000000/queue-perform-billing-command.fifo --attribute-names ApproximateNumberOfMessages ApproximateNumberOfMessagesNotVisible
	docker compose exec localstack awslocal sqs get-queue-attributes --queue-url http://sqs.us-east-1.localhost.localstack.cloud:4566/000000000000/queue-schedule-order-command.fifo --attribute-names ApproximateNumberOfMessages ApproximateNumberOfMessagesNotVisible
	