package core.utils.tasks;

import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.messages.KeyboardButton;
import com.vk.api.sdk.objects.messages.KeyboardButtonColor;
import com.vk.api.sdk.objects.messages.Message;
import com.vk.api.sdk.objects.messages.TemplateActionTypeNames;
import com.vk.api.sdk.objects.messages.responses.GetHistoryResponse;
import core.utils.DataBase;
import core.utils.Strings;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import static core.utils.CreateKeyboard.createButton;
import static core.utils.CreateKeyboard.createInlineKeyboard;

public class ViewPhotos implements Runnable {
    Integer userId;
    String author;
    VkApiClient apiClient;
    GroupActor actor;
    Random random;
    Message ans;
    Integer lastMessageId;
    Date created;
    String authorId;

    public ViewPhotos(Integer userId, VkApiClient apiClient, GroupActor actor, String author) {
        this.userId = userId;
        this.author = author;
        this.apiClient = apiClient;
        this.actor = actor;
        this.random = new Random();
        this.created = new Date();

        this.authorId = DataBase.checkUser(author);

    }

    @Override
    public void run() {
        if (author != null) {
            try {
                String id_cur = DataBase.checkUser(author);
                if (id_cur != null) {
                    //DataBase.checkUser()

                    ResultSet photos = DataBase.getPhotosOfAuthor(authorId);
                    while (photos.next()) {
                        List<KeyboardButton> line1 = new LinkedList<>();
                        List<List<KeyboardButton>> keyboard = new LinkedList<>();
                        line1.add(createButton(TemplateActionTypeNames.TEXT, KeyboardButtonColor.POSITIVE, "Следующий"));
                        keyboard.add(line1);

                        apiClient.messages().send(actor)
                                .attachment(Strings.GROUP_URL + photos.getString(1) + "%2Falbum-206249029_280673016")
                                .userId(userId)
                                .keyboard(createInlineKeyboard(keyboard))
                                .randomId(random.nextInt(10000))
                                .execute();
                        Thread.sleep(1000);

                        GetHistoryResponse historyQuery = apiClient.messages().getHistory(actor).userId(userId).execute();
                        this.lastMessageId = historyQuery.getItems().get(0).getId();
                        Date timestamp;
                        do {
                            if (getAnswer(lastMessageId))
                                break;
                            Thread.sleep(5000);
                            timestamp = new Date();
                        } while (timestamp.getTime() - created.getTime() < 180000);
                        if (!ans.getText().equalsIgnoreCase("следующий"))
                            break;
                    }
                } else {
                    apiClient.messages().send(actor)
                            .message("Пользователь не найден")
                            .userId(userId)
                            .randomId(random.nextInt(10000))
                            .execute();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                ResultSet photos = DataBase.getAllPhotos();
                while (photos.next()) {
                    List<KeyboardButton> line1 = new LinkedList<>();
                    List<List<KeyboardButton>> keyboard = new LinkedList<>();
                    line1.add(createButton(TemplateActionTypeNames.TEXT, KeyboardButtonColor.POSITIVE, "Следующий"));
                    keyboard.add(line1);

                    apiClient.messages().send(actor)
                            .attachment(Strings.GROUP_URL + photos.getString(1) + "%2Falbum-206249029_280673016")
                            .userId(userId)
                            .keyboard(createInlineKeyboard(keyboard))
                            .randomId(random.nextInt(10000))
                            .execute();
                    Thread.sleep(1000);

                    GetHistoryResponse historyQuery = apiClient.messages().getHistory(actor).userId(userId).execute();
                    this.lastMessageId = historyQuery.getItems().get(0).getId();
                    Date timestamp;
                    do {
                        if (getAnswer(lastMessageId))
                            break;
                        Thread.sleep(1000);
                        timestamp = new Date();
                    } while (timestamp.getTime() - created.getTime() < 180000);
                    if (!ans.getText().equalsIgnoreCase("следующий"))
                        break;
                }
            } catch (SQLException | ApiException | ClientException | InterruptedException e) {}

        }
    }

    private boolean getAnswer(Integer lastMessageId) throws ApiException, ClientException {
        GetHistoryResponse historyQuery = apiClient.messages().getHistory(actor).userId(userId).execute();
        Integer msg = historyQuery.getItems().get(0).getId();
        if (!lastMessageId.equals(msg)) {
            ans = historyQuery.getItems().get(0);
            return true;
        }

        return false;
    }
}
