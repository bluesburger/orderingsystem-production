install: up create-queues defining-dead-letter-queue-rule starting-message-move-task list-queues

up:
	@ echo Up service
	@ docker compose up -d --build

create-queues:
	@ echo Creating queues
	@ docker compose exec localstack awslocal sqs create-queue --queue-name localstack-queue.fifo --attributes "FifoQueue=true" \
		--output text --color auto
	@ docker compose exec localstack awslocal sqs create-queue --queue-name localstack-queue-dead-letter \
		--output text --color auto
	@ docker compose exec localstack awslocal sqs create-queue --queue-name localstack-queue-recovery \
		--output text --color auto

defining-dead-letter-queue-rule:
	@ echo Defining Dead Letter Queue Attributes
	@ docker compose exec localstack awslocal sqs set-queue-attributes \
		--queue-url http://sqs.us-east-1.localhost.localstack.cloud:4566/000000000000/localstack-queue.fifo \
		--attributes '{ "RedrivePolicy": "{\"deadLetterTargetArn\":\"arn:aws:sqs:us-east-1:000000000000:localstack-queue-dead-letter\",\"maxReceiveCount\":\"1\"}" }' \
		--output text

starting-message-move-task:
	@ echo Starting Message Move Task
	@ docker compose exec localstack awslocal sqs start-message-move-task \
        --source-arn arn:aws:sqs:us-east-1:000000000000:localstack-queue-dead-letter \
        --destination-arn arn:aws:sqs:us-east-1:000000000000:localstack-queue-recovery \
		--output text

list-queues:
	@ echo Showing existant queues
	@ docker compose exec localstack awslocal sqs list-queues \
		--output text

down:
	@ echo Down services
	@ docker compose down