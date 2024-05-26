#!/bin/bash

awslocal sqs create-queue --queue-name order-registered --attributes "FifoQueue=true"

awslocal sqs create-queue --queue-name order-confirmed --attributes "FifoQueue=true"

awslocal sqs create-queue --queue-name order-canceled --attributes "FifoQueue=true"


awslocal sqs create-queue --queue-name order-paid --attributes "FifoQueue=true"

awslocal sqs create-queue --queue-name order-failed-on-payment --attributes "FifoQueue=true"


awslocal sqs create-queue --queue-name order-scheduled --attributes "FifoQueue=true"

awslocal sqs create-queue --queue-name order-failed-delivery --attributes "FifoQueue=true"

awslocal sqs create-queue --queue-name order-performed-delivery --attributes "FifoQueue=true"


awslocal sqs create-queue --queue-name order-invoice-issued --attributes "FifoQueue=true"

awslocal sqs create-queue --queue-name order-invoice-failed-issued --attributes "FifoQueue=true"


echo "Filas Criadas"