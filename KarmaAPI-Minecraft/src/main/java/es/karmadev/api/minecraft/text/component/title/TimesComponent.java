package es.karmadev.api.minecraft.text.component.title;

import es.karmadev.api.minecraft.text.component.Component;

/**
 * Represents a title component
 */
public interface TimesComponent extends Component {

    /**
     * Set the times
     *
     * @param fadeIn the fade in time
     * @param show   the showtime
     * @param fadeOut the fade out time
     */
    void setTimes(final Times fadeIn, final Times show, final Times fadeOut);

    /**
     * Set the times
     *
     * @param fadeIn the fade in time
     */
    void setFadeIn(final Times fadeIn);

    /**
     * Set the times
     *
     * @param show the showtime
     */
    void setShow(final Times show);

    /**
     * Set the times
     *
     * @param fadeOut the fade out time
     */
    void setFadeOut(final Times fadeOut);

    /**
     * Get the times fade in
     *
     * @return the fade in time
     */
    Times getFadeIn();

    /**
     * Get the times show
     *
     * @return the showtime
     */
    Times getShow();

    /**
     * Get the times fade out
     *
     * @return the fade out time
     */
    Times getFadeOut();
}
