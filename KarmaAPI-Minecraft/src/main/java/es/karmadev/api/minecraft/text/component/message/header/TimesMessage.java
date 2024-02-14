package es.karmadev.api.minecraft.text.component.message.header;

import es.karmadev.api.kson.JsonObject;
import es.karmadev.api.minecraft.text.TextMessageType;
import es.karmadev.api.minecraft.text.component.Component;
import es.karmadev.api.minecraft.text.component.title.Times;
import es.karmadev.api.minecraft.text.component.title.TimesComponent;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.*;

/**
 * Represents a title times
 */
@RequiredArgsConstructor
public class TimesMessage implements TimesComponent {

    @NonNull
    private Times fadeIn;
    @NonNull
    private Times show;
    @NonNull
    private Times fadeOut;

    @Setter
    private boolean simpleFormat = true;

    private final List<Component> extra = new ArrayList<>();

    /**
     * Get the text message type
     *
     * @return the text message type
     */
    @Override
    public TextMessageType getType() {
        return TextMessageType.TIMES;
    }

    /**
     * Update the text message type, please note
     * if the current/new type is {@link TextMessageType#TIMES},
     * this could result in unexpected behaviour
     *
     * @param newType the new type
     */
    @Override
    public void setType(final TextMessageType newType) {
        if (newType.equals(TextMessageType.TIMES)) return;
        throw new IllegalStateException("Cannot convert from times message to " + newType.name());
    }

    /**
     * Get the raw text
     *
     * @return the raw text
     */
    @Override
    public String getRaw() {
        String fadeIn = this.fadeIn.toString();
        String show = this.show.toString();
        String fadeOut = this.fadeOut.toString();

        if (simpleFormat) {
            /*
            If we are simple-formatting, we will
            replace values in a way a human can
            understand
             */

            if (fadeIn.equals("10"))
                fadeIn = "reset";

            if (show.equals("70"))
                show = "reset";

            if (fadeOut.equals("20"))
                fadeOut = "reset";

            if (this.fadeIn.equals(Times.NONE))
                fadeIn = "none";

            if (this.show.equals(Times.NONE))
                show = "none";

            if (this.fadeOut.equals(Times.NONE))
                fadeOut = "none";
        }

        return "<times fadeIn=\"" + fadeIn + "\" show=\"" + show + "\" fadeOut=\"" + fadeOut + "\">";
    }

    /**
     * Add an extra component to
     * the current one
     *
     * @param other the component to add
     */
    @Override
    public void addExtra(final Component other) {
        extra.add(other);
    }

    /**
     * Get the component extra elements
     *
     * @return the extra components
     */
    @Override
    public Collection<? extends Component> getExtra() {
        return Collections.unmodifiableList(extra);
    }

    /**
     * Get the component as a
     * json string
     *
     * @param pretty if the json should be pretty
     * @return the json component
     */
    @Override
    public String toJson(final boolean pretty) {
        JsonObject main = JsonObject.newObject("", "");
        JsonObject times = JsonObject.newObject("", "");

        main.put("type", TextMessageType.TIMES.name());

        times.put("fadeIn", fadeIn.getTicks());
        times.put("stay", show.getTicks());
        times.put("fadeOut", fadeOut.getTicks());

        main.put("times", times);

        return main.toString(pretty);
    }

    /**
     * Set the times
     *
     * @param fadeIn  the fade in time
     * @param show    the showtime
     * @param fadeOut the fade out time
     */
    @Override
    public void setTimes(final @NonNull Times fadeIn, final @NonNull Times show, final @NonNull Times fadeOut) {
        this.fadeIn = fadeIn;
        this.show = show;
        this.fadeOut = fadeOut;
    }

    /**
     * Set the times
     *
     * @param fadeIn the fade in time
     */
    @Override
    public void setFadeIn(final @NonNull Times fadeIn) {
        this.fadeIn = fadeIn;
    }

    /**
     * Set the times
     *
     * @param show the showtime
     */
    @Override
    public void setShow(final @NonNull Times show) {
        this.show = show;
    }

    /**
     * Set the times
     *
     * @param fadeOut the fade out time
     */
    @Override
    public void setFadeOut(final @NonNull Times fadeOut) {
        this.fadeOut = fadeOut;
    }

    /**
     * Get the times fade in
     *
     * @return the fade in time
     */
    @Override @NonNull
    public Times getFadeIn() {
        return fadeIn;
    }

    /**
     * Get the times show
     *
     * @return the showtime
     */
    @Override @NonNull
    public Times getShow() {
        return show;
    }

    /**
     * Get the times fade out
     *
     * @return the fade out time
     */
    @Override @NonNull
    public Times getFadeOut() {
        return fadeOut;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof TimesComponent)) return false;

        TimesComponent that = (TimesComponent) o;
        return Objects.equals(fadeIn, that.getFadeIn()) && Objects.equals(show, that.getShow()) && Objects.equals(fadeOut, that.getFadeOut());
    }

    @Override
    public int hashCode() {
        return Objects.hash(fadeIn, show, fadeOut);
    }
}
