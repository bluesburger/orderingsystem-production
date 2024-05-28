# Ordering System Production
Responsável por performar a produção

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=bluesburger_orderingsystem-production&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=bluesburger_orderingsystem-production)


## Para utilizar

Instalação
- Instalar o make conforme tutorial
- Rodar localmente o comando `make`

Desinstalação
- Rodar localmente o comando `make down`

## Próximos passos:

- Incluir persistência de evento com Pedido, Data e Tipo

-----

## SAGA Orquestrada

```mermaid
	PEDIDO->>ESTOQUE
```

1. Serviço PEDIDO recebe solicitação para CRIAR PEDIDO
	- persiste na própria database
	- cria novo evento na fila order-created.fifo

2. PRODUCTION consome fila order-created.fifo
	- persiste evento na database
	- command para servioço de ESTOQUE: RESERVAR_ITEM_ESTOQUE

3. Serviço ESTOQUE executa reserva: 
	- persiste reserva na database
	- cria novo evento na fila "pedido-reservado.fifo"
		
4. Serviço PRODUCTION consome fila pedido-reservado.fifo
	- faz requisição para atualizar pedido para novo STEP e FASE
	- persiste evento na database
	- faz requisição assíncrona (command) para serviço de PAGAMENTO
	
5. Serviço PAGAMENTO executa pagamento:
	- persiste pagamento na database própria
	- cria novo evento cobranca-realizada.fifo
	
6. Serviço PRODUCTION consome fila cobranca-realizada.fifo
	- faz requisição para atualizar pedido para novo STEP e FASE
	- persiste evento na database
	- faz requisição assíncrona (command) para serviço NOTA_FISCAL
	
7. Serviço NOTA_FISCAL executa emissão de NF:
	- persiste metadados na própria database
	- cria novo evento na fila nota-fiscal-emitida.fifo
	
8. Serviço PRODUCTION consome fila nota-fiscal-emitida.fifo
	- faz requisição para atualizar pedido para novo STEP e FASE
	- persiste evento na database
	- cria novo evento na fila "pedido-confirmado.fifo"
	
9. Serviço PRODUCTION consome fila pedido-confirmado.fifo
	- faz requisição para atualizar pedido para novo STEP e FASE
	- persiste evento na database
		
	
#### Referências

- [github aws-doc-sdk-examples](https://github.com/awsdocs/aws-doc-sdk-examples/tree/main/java)