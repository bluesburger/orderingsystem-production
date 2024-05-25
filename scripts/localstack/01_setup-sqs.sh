#!/bin/bash

awslocal sqs create-queue --queue-name order-registered.fifo --attributes "FifoQueue=true"

awslocal sqs create-queue --queue-name order-confirmed.fifo --attributes "FifoQueue=true"

awslocal sqs create-queue --queue-name order-canceled.fifo --attributes "FifoQueue=true"


awslocal sqs create-queue --queue-name order-paid.fifo --attributes "FifoQueue=true"

awslocal sqs create-queue --queue-name order-failed-on-payment.fifo --attributes "FifoQueue=true"


awslocal sqs create-queue --queue-name order-scheduled.fifo --attributes "FifoQueue=true"

awslocal sqs create-queue --queue-name order-failed-delivery.fifo --attributes "FifoQueue=true"

awslocal sqs create-queue --queue-name order-performed-delivery.fifo --attributes "FifoQueue=true"


awslocal sqs create-queue --queue-name order-invoice-issued.fifo --attributes "FifoQueue=true"

awslocal sqs create-queue --queue-name order-invoice-failed-issued.fifo --attributes "FifoQueue=true"


echo "Filas Criadas"