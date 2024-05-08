#!/bin/bash

awslocal sqs create-queue --queue-name order-paid.fifo --attributes "FifoQueue=true"

awslocal sqs create-queue --queue-name order-in-production.fifo --attributes "FifoQueue=true"

awslocal sqs create-queue --queue-name order-produced.fifo --attributes "FifoQueue=true"

awslocal sqs create-queue --queue-name order-delivering.fifo --attributes "FifoQueue=true"

awslocal sqs create-queue --queue-name order-delivered.fifo --attributes "FifoQueue=true"

awslocal sqs create-queue --queue-name order-canceled.fifo --attributes "FifoQueue=true"

echo "Filas Criadas"