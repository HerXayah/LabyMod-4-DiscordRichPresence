package sarah.egirl.yamete;

import net.labymod.api.addon.AddonConfig;
import net.labymod.api.client.gui.screen.widget.widgets.input.ButtonWidget.ButtonSetting;
import net.labymod.api.client.gui.screen.widget.widgets.input.SwitchWidget.SwitchSetting;
import net.labymod.api.client.gui.screen.widget.widgets.input.TextFieldWidget.TextFieldSetting;
import net.labymod.api.configuration.loader.annotation.ConfigName;
import net.labymod.api.configuration.loader.annotation.SpriteSlot;
import net.labymod.api.configuration.loader.property.ConfigProperty;
import net.labymod.api.configuration.settings.Setting;
import net.labymod.api.util.MethodOrder;
import java.awt.*;
import java.io.IOException;
import java.net.URI;

@ConfigName("settings")
public class DiscordRPConfiguration extends AddonConfig {
  @SpriteSlot(size = 32, x = 1)
  @SwitchSetting
  private final ConfigProperty<Boolean> enabled = new ConfigProperty<>(true);

  @TextFieldSetting
  private final ConfigProperty<String> discordID = new ConfigProperty<>("");

  @MethodOrder(after = "enabled")
  @ButtonSetting
  public void button(Setting setting) throws IOException {
    DiscordRPAddon.getInstance().logger().info("Nothing here.");
  }

  @Override
  public ConfigProperty<Boolean> enabled() {
    return this.enabled;
  }

  public ConfigProperty<String> discordID() {
    return this.discordID;
  }
}
