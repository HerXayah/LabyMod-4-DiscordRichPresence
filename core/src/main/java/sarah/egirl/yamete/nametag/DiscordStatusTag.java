package sarah.egirl.yamete.nametag;

import net.labymod.api.Laby;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.entity.player.Player;
import net.labymod.api.client.entity.player.tag.tags.NameTag;
import net.labymod.api.client.gui.HorizontalAlignment;
import net.labymod.api.client.render.RenderPipeline;
import net.labymod.api.client.render.draw.RectangleRenderer;
import net.labymod.api.client.render.font.RenderableComponent;
import net.labymod.api.client.render.matrix.Stack;
import sarah.egirl.yamete.listener.WebSocketManager;
import org.jetbrains.annotations.Nullable;

/**
 * @author https://github.com/PrincessAkira (Sarah) Today is the 8/16/2024 @7:26 PM This project is
 * named labymod4-addon-template
 * @description Another day of Insanity
 */
public class DiscordStatusTag extends NameTag {

  private final RenderPipeline renderPipeline;
  private final RectangleRenderer rectangleRenderer;
  private final WebSocketManager manager = WebSocketManager.getInstance();

  public DiscordStatusTag(RenderPipeline renderPipeline, RectangleRenderer rectangleRenderer) {
    this.renderPipeline = Laby.references().renderPipeline();
    this.rectangleRenderer = this.renderPipeline.rectangleRenderer();
  }

  @Override
  protected @Nullable RenderableComponent getRenderableComponent() {
    if (!(this.entity instanceof Player) || this.entity.isCrouching()) {
      return null;
    }

    Player player = (Player) this.entity;

    HorizontalAlignment alignment;
    alignment = HorizontalAlignment.LEFT;

    Component component = manager.component;
    if (component == null) {
      return null;
    }

    return RenderableComponent.of(component, alignment);
  }

  @Override
  protected void renderText(
      Stack stack,
      RenderableComponent component,
      boolean discrete,
      int textColor,
      int backgroundColor,
      float x,
      float y
  ) {
    float width = this.getWidth();
    float height = this.getHeight();
    this.rectangleRenderer.renderRectangle(
        stack,
        x,
        y,
        width,
        height,
        backgroundColor
    );

    float textX = x;
    if (this.manager.icon != null) {
      this.renderPipeline.renderSeeThrough(this.entity, () ->
          this.manager.icon.render(stack, x + 1, y + 1, height - 2)
      );

      textX += height + 1;
    }

    super.renderText(stack, component, discrete, textColor, 0, textX, y + 1);
  }

  @Override
  public float getScale() {
    return 0.75F;
  }

  @Override
  public float getWidth() {
    if (this.manager.icon == null) {
      return super.getWidth();
    }

    return super.getWidth() + this.getHeight();
  }

  @Override
  public float getHeight() {
    return super.getHeight() + 1;
  }
}
