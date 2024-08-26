package sarah.egirl.yamete;

import com.fasterxml.jackson.databind.JsonNode;
import net.labymod.api.Laby;
import net.labymod.api.addon.LabyAddon;
import net.labymod.api.client.entity.player.tag.PositionType;
import net.labymod.api.models.addon.annotation.AddonMain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sarah.egirl.yamete.listener.WebSocketManager;
import sarah.egirl.yamete.nametag.DiscordStatusTag;

@AddonMain
public class DiscordRPAddon extends LabyAddon<DiscordRPConfiguration> {

  private static final Logger log = LoggerFactory.getLogger(DiscordRPAddon.class);
  private static DiscordRPAddon instance;
  private static final WebSocketManager manager = WebSocketManager.getInstance();

  public static DiscordRPAddon getInstance() {
    return instance;
  }

  @Override
  protected void enable() {
    instance = this;
    manager.addListener(() -> {
      manager.filterData();
      manager.prepareComponent();
      manager.setIcon();
    });
    this.registerSettingCategory();
    manager.connect();
    this.logger().info("Enabled the Addon");
    this.labyAPI().tagRegistry().register(
        "discord_status",
        PositionType.BELOW_NAME,
        new DiscordStatusTag(
            Laby.references().renderPipeline(),
            Laby.references().renderPipeline().rectangleRenderer()
        )
    );
  }

  @Override
  protected Class<DiscordRPConfiguration> configurationClass() {
    return DiscordRPConfiguration.class;
  }

}
