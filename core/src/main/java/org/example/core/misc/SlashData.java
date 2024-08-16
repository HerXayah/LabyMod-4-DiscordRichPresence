package org.example.core.misc;

import org.example.core.ExampleAddon;
import org.example.core.listener.WebSocketManager;

/**
 * @author https://github.com/PrincessAkira (Sarah) Today is the 8/16/2024 @7:04 PM This project is
 * named labymod4-addon-template
 * @description Another day of Insanity
 */
public class SlashData {


  private static final WebSocketManager manager = WebSocketManager.getInstance();
  private static SlashData instance;
  private static ExampleAddon addon = ExampleAddon.getInstance();


  public static SlashData getInstance() {
    if (instance == null) {
      instance = new SlashData();
    }
    return instance;
  }

  public String getGameName() {
    String data = manager.getActivities();
    String gameName = "";
    try {
      gameName = manager.objectMapper.readTree(data).get("data").get("activities").get(0).get("name").asText();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return gameName;
  }

  public String getGameDetails() {
    String data = manager.getActivities();
    String gameDetails = "";
    try {
      gameDetails = manager.objectMapper.readTree(data).get("data").get("activities").get(0).get("details").asText();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return gameDetails;
  }

  public String getGameState() {
    String gameState = "";
    String data = manager.getActivities();
    try {
      gameState = manager.objectMapper.readTree(data).get("data").get("activities").get(0).get("state").asText();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return gameState;
  }

}
