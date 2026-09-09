package zucc.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Displays a single message with a speaker label and compact avatar.
 */
public final class DialogBox extends HBox {
    /**
     * Creates a styled message row for one speaker.
     *
     * @param message message body to display.
     * @param speaker readable name of the speaker.
     * @param avatarText letters displayed in the speaker avatar.
     * @param isUser {@code true} when the message was entered by the user.
     */
    private DialogBox(String message, String speaker, String avatarText, boolean isUser) {
        getStyleClass().add("dialog-row");
        setAlignment(isUser ? Pos.TOP_RIGHT : Pos.TOP_LEFT);

        Label speakerLabel = new Label(speaker);
        speakerLabel.getStyleClass().add("speaker-label");

        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setMinHeight(USE_PREF_SIZE);
        messageLabel.getStyleClass().add("message-text");

        VBox messageCard = new VBox(speakerLabel, messageLabel);
        messageCard.getStyleClass().addAll("message-card", isUser ? "user-card" : "zucc-card");
        messageCard.setMaxWidth(520.0);

        Label avatarLabel = new Label(avatarText);
        avatarLabel.getStyleClass().add("avatar-text");
        StackPane avatar = new StackPane(avatarLabel);
        avatar.getStyleClass().addAll("avatar", isUser ? "user-avatar" : "zucc-avatar");

        if (isUser) {
            getChildren().addAll(messageCard, avatar);
            getStyleClass().add("user-dialog");
        } else {
            getChildren().addAll(avatar, messageCard);
            getStyleClass().add("zucc-dialog");
        }
    }

    /**
     * Creates a dialog row for a command entered by the user.
     *
     * @param message command text to display.
     * @return styled user dialog row.
     */
    public static DialogBox createUserDialog(String message) {
        return new DialogBox(message, "YOU", "Y", true);
    }

    /**
     * Creates a dialog row for a response from Zucc.
     *
     * @param message response text to display.
     * @return styled Zucc dialog row.
     */
    public static DialogBox createZuccDialog(String message) {
        return new DialogBox(message, "ZUCC", "Z", false);
    }
}
