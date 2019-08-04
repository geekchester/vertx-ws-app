# Vertx with WS embedded into Spring boot app

### Local run
- Start app - `gradle bootRun`
- Listen to WS - `wsdump.py ws://localhost:7777/live-edit/ws`

Observe messages received:
```
< {"id":"563af08c-758e-4ea9-9185-79749534d89d","type":"ws.init","payload":"hello"}

< {"id":"48e9a371-3073-4193-ab30-7a7376e7d8e7","type":"ping","payload":null}
```