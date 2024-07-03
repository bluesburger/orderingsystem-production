#!/bin/bash

awslocal sqs create-queue --queue-name order-created-event.fifo --attributes "FifoQueue=true"

awslocal sqs create-queue --queue-name queue-order-stock-command.fifo --attributes "FifoQueue=true"

awslocal sqs create-queue --queue-name queue-cancel-order-stock-command.fifo --attributes "FifoQueue=true"

awslocal sqs sqs create-queue --queue-name order-stock-failed-event.fifo --attributes "FifoQueue=true"

awslocal sqs create-queue --queue-name queue-order-stock-command.fifo --attributes "FifoQueue=true"

awslocal sqs create-queue --queue-name invoice-issued-event.fifo --attributes "FifoQueue=true"

awslocal sqs create-queue --queue-name order-scheduled-event.fifo --attributes "FifoQueue=true"

awslocal sqs create-queue --queue-name bill-performed-event.fifo --attributes "FifoQueue=true"

awslocal sqs create-queue --queue-name order-ordered-event.fifo --attributes "FifoQueue=true"

awslocal sqs create-queue --queue-name stock-failed-event.fifo --attributes "FifoQueue=true"

awslocal sqs create-queue --queue-name order-failed-delivery.fifo --attributes "FifoQueue=true"

awslocal sqs create-queue --queue-name perform-billing-failed-event.fifo --attributes "FifoQueue=true"

awslocal sqs create-queue --queue-name queue-perform-billing-command.fifo --attributes "FifoQueue=true"

awslocal sqs create-queue --queue-name queue-cancel-bill-command.fifo --attributes "FifoQueue=true"

awslocal sqs create-queue --queue-name queue-invoice-command.fifo --attributes "FifoQueue=true"

awslocal sqs create-queue --queue-name queue-cancel-invoice-command.fifo --attributes "FifoQueue=true"

awslocal sqs create-queue --queue-name queue-schedule-order-command.fifo --attributes "FifoQueue=true"

awslocal sqs create-queue --queue-name queue-order-confirmed-command.fifo --attributes "FifoQueue=true"

echo "Filas Criadas"