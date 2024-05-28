
# Ordering System Production

Responsável por performar a produção

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=bluesburger_orderingsystem-production&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=bluesburger_orderingsystem-production)

---

<h2>Para utilizar</h2>

Instalação
- Instalar o make conforme tutorial
- Rodar localmente o comando `make`

Desinstalação
- Rodar localmente o comando `make down`

-----

<h2>SAGA Orquestrada (Caminho Feliz)</h2>

<img src="./assets/saga-orquestrada.png" alt="Saga Orquestrada!" style="width:1024px; display: block; margin: auto;" />

**Serviços**:
- MenuService
- OrderService
- ProductionService (SAGA)
- StockService
- PaymentService
- NotaFiscalService

**Eventos**:
- OrderCreatedEvent
- OrderOrderedEvent
- BillPerformedEvent
- InvoiceIssuedEvent

**Comandos**:
- OrderStockCommand
- PerformBillingCommand
- IssueInvoiceCommand

---

<h2>Dependências</h2>:

**OrderService**
_Publica_:
	- OrdeCreatedEvent

**StockService**:
_Consome_:
	- OrderStockCommand
_Publica_:
	- OrderOrderedEvent
	- OrderStockFailedCommand

**PaymentService**:
_Consome_:
	- PerformBillingCommand
_Publica_:
	- BillPerformedEvent
	- PerformBillingFailedCommand
	
**NotaFiscalService**:
_Consome_:
	- IssueInvoiceCommand	
_Publica_:
	- InvoiceIssuedEvent
	- IssueInvoiceFailedCommand
	
**ProductionService**:
_Consome_:
	- OrderCreatedEvent
	- OrderOrderedEvent
	- BillPerformedEvent
	- InvoiceIssuedEvent
	
_Publica_:
	- OrderStockCommand
	- PerformBillingCommand
	- IssueInvoiceCommand

---
	
<h2>Referências</h2>

- [github aws-doc-sdk-examples](https://github.com/awsdocs/aws-doc-sdk-examples/tree/main/java)