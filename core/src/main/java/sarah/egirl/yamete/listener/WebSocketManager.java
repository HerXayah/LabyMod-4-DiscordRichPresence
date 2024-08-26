package sarah.egirl.yamete.listener;

import java.net.URI;
import java.net.URISyntaxException;
import com.fasterxml.jackson.core.JsonProcessingException;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.gui.icon.Icon;
import sarah.egirl.yamete.DiscordRPAddon;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class WebSocketManager extends WebSocketClient {

  private static volatile WebSocketManager instance;
  private final ObjectMapper objectMapper = new ObjectMapper();
  public volatile Component component;
  private final List<Runnable> listeners = new CopyOnWriteArrayList<>();
  public volatile Icon icon;
  private final AtomicReference<String> latestData = new AtomicReference<>();

  private WebSocketManager(URI serverUri) {
    super(serverUri);
    System.out.println("WebSocketManager created");
    if (instance != null) {
      DiscordRPAddon.getInstance().logger().debug("WebSocketManager already created");
    }
  }

  public static WebSocketManager getInstance() {
    if (instance == null) {
      synchronized (WebSocketManager.class) {
        if (instance == null) {
          try {
            instance = new WebSocketManager(new URI("wss://api.lanyard.rest/socket"));
          } catch (URISyntaxException e) {
            DiscordRPAddon.getInstance().logger().debug(e.getMessage());
          }
        }
      }
    }
    return instance;
  }

  public void addListener(Runnable listener) {
    listeners.add(listener);
  }

  public void removeListener(Runnable listener) {
    listeners.remove(listener);
  }

  private void notifyListeners() {
    for (Runnable listener : listeners) {
      listener.run();
    }
  }

  @Override
  public void onOpen(ServerHandshake handshakedata) {
    try {
      this.send("{\"op\": 2, \"d\": {\"subscribe_to_id\": \"" + DiscordRPAddon.getInstance().configuration().discordID().get() + "\"}}");
    } catch (Exception e) {
      DiscordRPAddon.getInstance().logger().debug("Error sending message: " + e.getMessage());
    }
  }

  @Override
  public void onMessage(String s) {
    handleMessage(s);
  }

  @Override
  public void onClose(int i, String s, boolean b) {
    DiscordRPAddon.getInstance().logger().debug("Closed Connection");
  }

  @Override
  public void onError(Exception e) {
    DiscordRPAddon.getInstance().logger().debug(e.getMessage());
  }

  public void handleLatestData() {
    String data = latestData.get();
    if (data != null) {
      handleMessage(data);
    }
  }

  private synchronized void handleMessage(String data) {
    try {
      JsonNode node = objectMapper.readTree(data);
      String type = node.get("t").asText();
      if (type.equals("INIT_STATE") || type.equals("PRESENCE_UPDATE")) {
        latestData.set(data);
        filterData();
        notifyListeners();
      } else {
        DiscordRPAddon.getInstance().logger().debug("Received unexpected message type: " + type);
      }
    } catch (Exception e) {
      DiscordRPAddon.getInstance().logger().debug("Error processing message: " + e.getMessage());
    }
  }

  public JsonNode getLatestDataAsJsonNode() {
    String data = latestData.get();
    if (data != null) {
      try {
        return objectMapper.readTree(data);
      } catch (JsonProcessingException e) {
        DiscordRPAddon.getInstance().logger().debug(e.getMessage());
      }
    }
    return null;
  }

  public synchronized void filterData() {
    String data = latestData.get();
    if (data != null) {
      try {
        JsonNode node = objectMapper.readTree(data);
        JsonNode activities = node.get("d").get("activities");
        if (activities.isArray()) {
          // Create a new array node to store filtered activities
          List<JsonNode> filteredActivities = new ArrayList<>();
          for (JsonNode activity : activities) {
            int type = activity.get("type").asInt();
            if (type != 1 && type != 2 && type != 3) {
              filteredActivities.add(activity);
            }
          }
          // Replace the activities node with the filtered activities
          ((com.fasterxml.jackson.databind.node.ArrayNode) activities).removeAll();
          ((com.fasterxml.jackson.databind.node.ArrayNode) activities).addAll(filteredActivities);
          latestData.set(objectMapper.writeValueAsString(node));
        }
      } catch (JsonProcessingException e) {
        DiscordRPAddon.getInstance().logger().debug(e.getMessage());
      }
    }
  }

  public void setIcon() {
    JsonNode activities = getLatestDataAsJsonNode().get("d").get("activities");
    if (activities != null && activities.isArray() && !activities.isEmpty()) {
      try {
        JsonNode node = activities.get(0);
        JsonNode assets = node.get("assets");
        String largeImage = assets.get("large_image").asText();
        if (largeImage.startsWith("mp:external")) {
          icon = Icon.url("https://media.discordapp.net/" + largeImage.split(":")[1]);
        } else {
          icon = Icon.url("https://cdn.discordapp.com/app-assets/" + node.get("application_id").asText() + "/" + largeImage + ".png");
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  public void prepareComponent() {
    JsonNode activities = getLatestDataAsJsonNode().get("d").get("activities");
    if (activities != null && activities.isArray() && !activities.isEmpty()) {
      try {
        JsonNode node = activities.get(0);
        String gameName = node.get("name").asText();
        String gameState = node.get("state").asText().length() > 30 ? node.get("state").asText().substring(0, 30) + "..." : node.get("state").asText();
        String gameDetails = node.get("details").asText().length() > 30 ? node.get("details").asText().substring(0, 30) + "..." : node.get("details").asText();
        component = Component.text(gameName + " \n " + gameDetails + " \n " + gameState);
      } catch (Exception e) {
        DiscordRPAddon.getInstance().logger().debug(e.getMessage());
      }
    }
    DiscordRPAddon.getInstance().logger().debug("Created Component");
  }

  public String getActivities() {
    JsonNode data = getLatestDataAsJsonNode();
    if (data != null) {
      JsonNode activities = data.get("d").get("activities");
      return activities != null ? activities.toString() : null;
    }
    return null;
  }
}
