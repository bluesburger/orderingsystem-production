# Ordering System Production
> Responsável por performar a produção


## Domain Driven Design

> Infrastructure
- OrderStatusUpdatedEventConsumer
- OrderStatusUpdatedEventPublisher
- MongoDbOrderStatusRepository?
- MongoDBConfiguration?
- BeanConfiguration?
- SqsOrderStatusAdapter?

> Application
- OrderStatusService

> Domain
- OrderStatusUpdated