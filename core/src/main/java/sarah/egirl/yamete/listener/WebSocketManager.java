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
import java.util.concurrent.atomic.AtomicReference;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class WebSocketManager extends WebSocketClient {

  private static WebSocketManager instance;
  public final ObjectMapper objectMapper = new ObjectMapper();
  public Component component;
  public boolean dataExists = false;
  private final List<Runnable> listeners = new ArrayList<>();
  public Icon icon;
  public final AtomicReference<String> latestData = new AtomicReference<>();

  public WebSocketManager(URI serverUri) {
    super(serverUri);
    System.out.println("WebSocketManager created");
    if(instance != null) {
      throw new RuntimeException("WebSocketManager already exists");
    }
  }

  public static WebSocketManager getInstance() {
    if (instance == null) {
      try {
        instance = new WebSocketManager(new URI("wss://api.lanyard.rest/socket"));
      } catch (URISyntaxException e) {
        e.printStackTrace();
      }
    }
    return instance;
  }

  public void addListener(Runnable listener) {
    listeners.add(listener);
    dataExists = true;
  }

  public void removeListener(Runnable listener) {
    listeners.remove(listener);
  }

  private void notifyListeners(String data) {
    for (Runnable listener : listeners) {
      listener.run();
    }
  }

  @Override
  public void onOpen(ServerHandshake handshakedata) {
    System.out.println("opened connection");
    try {
      this.send("{\"op\": 2, \"d\": {\"subscribe_to_id\": \"" + DiscordRPAddon.getInstance().configuration().discordID().get() + "\"}}");
      dataExists = true;
    } catch (Exception e) {
      System.err.println("Error sending message: " + e.getMessage());
    }

  }

  @Override
  public void onMessage(String s) {
    System.out.println("received: " + s);
    handleMessage(s);
  }

  @Override
  public void onClose(int i, String s, boolean b) {
    System.out.println("closed: " + s);

  }

  @Override
  public void onError(Exception e) {
    System.out.println("error: " + e.getMessage());
    
  }

  public void handleLatestData() {
    String data = latestData.get();
    if (data != null) {
      handleMessage(data);
    }
  }

  public String getType() {
    String data = latestData.get();
    String type = null;
    if (data != null) {
      try {
        JsonNode node = objectMapper.readTree(data);
        type = node.get("t").asText();
      } catch (JsonProcessingException e) {
        throw new RuntimeException(e);
      }
    }
    return type;
  }

  private void handleMessage(String data) {
    try {
      JsonNode node = objectMapper.readTree(data);
      String type = node.get("t").asText();
      if (type.equals("INIT_STATE") || type.equals("PRESENCE_UPDATE")) {
        latestData.set(data);
        filterData();
        notifyListeners(data);
      } else {
        System.out.println("Received unexpected message type: " + type);
      }
    } catch (Exception e) {
      System.err.println("Error processing message: " + e.getMessage());
      e.printStackTrace();
    }
  }

  public String getLatestData() {
    return latestData.get();
  }

  public void filterData() {
    // filter if data.activities[0].type is 1 or 2, remove them from the data array
    // get all activities in an array
    // remove all activities with type 1 or 2
    String data = latestData.get();
    if (data != null) {
      try {
        JsonNode node = objectMapper.readTree(data);
        JsonNode activities = node.get("d").get("activities");
        for (int i = 0; i < activities.size(); i++) {
          JsonNode activity = activities.get(i);
          if (activity.get("type").asInt() == 1 || activity.get("type").asInt() == 2 || activity.get("type").asInt() == 3) {
            System.out.println("Removing activity: " + activity.toString());
            ((com.fasterxml.jackson.databind.node.ArrayNode) activities).remove(i);
          }
        }
        latestData.set(objectMapper.writeValueAsString(node));
      } catch (JsonProcessingException e) {
        throw new RuntimeException(e);
      }
    }
  }

  public void setIcon() {
    String activities = getActivities();
    if (activities != null) {
      try {
        JsonNode node = objectMapper.readTree(activities).get(0);
        JsonNode assets = node.get("assets");
        String largeImage = assets.get("large_image").asText();
        if (largeImage.startsWith("mp:external")) {
          icon = Icon.url("https://media.discordapp.net/" + largeImage.split(":")[1]);
        } else {
          icon = Icon.url("https://cdn.discordapp.com/app-assets/" + node.get("application_id").asText() + "/" + largeImage + ".png");
        }
        System.out.println("Icon set to: " + icon.toString());
      } catch (JsonProcessingException e) {
        throw new RuntimeException(e);
      }
    }
    System.out.println("Icon set");
  }

  public void prepareComponent() {
    String activities = getActivities();
    if (activities != null) {
      try {
        JsonNode node = objectMapper.readTree(activities).get(0);
        String gameName = node.get("name").asText();
        String gameState = node.get("state").asText().length() > 30 ? node.get("state").asText().substring(0, 30) + "..." : node.get("state").asText();
        String gameDetails = node.get("details").asText().length() > 30 ? node.get("details").asText().substring(0, 30) + "..." : node.get("details").asText();
        component = Component.text(gameName + " \n " + gameDetails + " \n " + gameState);
        System.out.println("Component set to: " + component.toString());
      } catch (JsonProcessingException e) {
        throw new RuntimeException(e);
      }
    }
    System.out.println("Component prepared");
  }

  public String getActivities() {
    String data = latestData.get();
    String activities = null;
    if (data != null) {
      try {
        JsonNode node = objectMapper.readTree(data);
        activities = node.get("d").get("activities").toString();
      } catch (JsonProcessingException e) {
        throw new RuntimeException(e);
      }
    }
    return activities;
  }
}

