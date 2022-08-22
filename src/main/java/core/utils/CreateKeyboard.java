package core.utils;

import com.vk.api.sdk.objects.messages.*;
import java.util.List;

public class CreateKeyboard {

    static Keyboard keyboard;
    static KeyboardButton button;

    public CreateKeyboard(){}

    public static Keyboard createInlineKeyboard(List<List<KeyboardButton>> buttons){
        keyboard = new Keyboard();
        keyboard.setInline(true);
        keyboard.setButtons(buttons);
        return keyboard;
    }

    public static Keyboard createKeyboard(List<List<KeyboardButton>> buttons){
        keyboard = new Keyboard();
        keyboard.setInline(false);
        keyboard.setButtons(buttons);
        return keyboard;
    }

    public static KeyboardButton createButton(TemplateActionTypeNames type,KeyboardButtonColor color, String text){
        button = new KeyboardButton();
        button.setAction(new KeyboardButtonAction().setType(type).setLabel(text));
        button.setColor(color);
        return button;
    }
    public static KeyboardButton createButton(TemplateActionTypeNames type, String text, String link){
        button = new KeyboardButton();
        button.setAction(new KeyboardButtonAction().setType(type).setLabel(text).setLink(link));
        return button;
    }

    public static KeyboardButton createButton(TemplateActionTypeNames type, String text){
        button = new KeyboardButton();
        button.setAction(new KeyboardButtonAction().setType(type).setLabel(text));
        return button;
    }
}
