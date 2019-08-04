package com.geekchester.vertx.ws;

import io.vavr.collection.HashMap;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.JsonObject;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class WsServer extends AbstractVerticle {
  private static final String wsPath = "/live-edit/ws";
  private static final AtomicReference<HashMap<String, ServerWebSocket>> connections =
      new AtomicReference<>(HashMap.empty());
  final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

  @Override
  public void start() throws Exception {
    super.start();
    startWsServer(vertx);
    System.out.println(String.format("Started ws: %s", wsPath));

    executor.scheduleAtFixedRate(() -> {
      connections.get().values()
          .forEach(ws -> ws.writeTextMessage(new WsPayload<String>("ping", null).jsonify()));
    }, 3000, 5000, TimeUnit.MILLISECONDS);
  }

  private void startWsServer(final Vertx vertx) {
    final HttpServer httpServer = vertx.createHttpServer();
    httpServer.websocketHandler((ws) -> {

      if (ws.path().equals(wsPath)) {
        final String connectionInstanceId = UUID.randomUUID().toString();

        System.out.println(String
            .format("Connection established with: %s with instanceId: %s", ws.remoteAddress().toString(),
                connectionInstanceId));
        ws.writeTextMessage(new WsPayload<>("ws.init", "hello").jsonify());

        connections.updateAndGet(cons -> cons.put(connectionInstanceId, ws));

        ws.closeHandler((closed) -> {
          connections.updateAndGet(cons -> cons.remove(ws.remoteAddress().toString()));
          System.out.println(String.format("Closed %s", ws.remoteAddress().toString()));
        });
      } else {
        ws.reject();
      }
    });

    httpServer.listen(7777);
  }

  private static final class WsPayload<T> {
    public final String id = UUID.randomUUID().toString();
    public final String type;
    public final T payload;

    public WsPayload(String type, T payload) {
      this.type = type;
      this.payload = payload;
    }

    public String jsonify() {
      return JsonObject.mapFrom(this).encode();
    }

    public String getType() {
      return type;
    }

    public T getPayload() {
      return payload;
    }
  }
}
