package core.utils.tasks;

import com.vk.api.sdk.actions.Photos;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.messages.Message;
import com.vk.api.sdk.objects.messages.responses.GetHistoryResponse;
import com.vk.api.sdk.objects.photos.Photo;
import com.vk.api.sdk.objects.photos.responses.GetUploadServerResponse;
import com.vk.api.sdk.objects.photos.responses.SaveResponse;
import com.vk.api.sdk.queries.photos.PhotosGetQuery;
import core.utils.DataBase;

import java.net.URI;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Random;


public class AddNewPhoto extends BaseTask implements Runnable {

    UserActor user;
    String nick;

    public AddNewPhoto(Integer userId, VkApiClient apiClient, GroupActor actor, UserActor user) {
        super(userId, apiClient, actor);
        this.user = user;
        try {
            ResultSet set = DataBase.getNick(userId.toString());
            set.next();
            this.nick = set.getString(1);
        } catch (SQLException e){}
    }

    @Override
    public void run() {
        try {
            if (DataBase.checkUser(userId)) {
                GetHistoryResponse historyQuery = apiClient.messages().getHistory(actor).userId(userId).execute();
                Message msg = historyQuery.getItems().get(0);
                if (msg.getAttachments().size() != 0) {
                    try {
                        Registration reg = new Registration(msg.getFromId(), apiClient, actor, user);
                        GetUploadServerResponse gusr = apiClient.photos().getUploadServer(user).albumId(280673016).groupId(206249029).execute();
                        Photo photo = msg.getAttachments().get(0).getPhoto();
                        Photos photos = new Photos(apiClient);

                        PhotosGetQuery result = photos.get(user).albumId("280673016").ownerId(-206249029).rev(true).count(1);

                        String url = photo.getSizes().get(4).getUrl().toString();
                        url.replace("/impf", "");
                        String filename = reg.downloadForURL(url);
                        // добавление в альбом
                        URI uri = gusr.getUploadUrl();
                        try {
                            String res = reg.createPOST(uri.toString(), filename);
                            List<SaveResponse> res2 = reg.loadPhoto(res, nick, user);
                            System.out.println(res2);

                            // Добавление записи в фото
//                            DataBase.insertToPhotos(photo.getId().toString(), msg.getFromId().toString());
                            DataBase.insertToPhotos(result.photoIds().execute().getItems().get(0).getId().toString(), msg.getFromId().toString());

                            apiClient.messages().send(actor)
                                    .message("Фото добавлено.")
                                    .userId(userId)
                                    .randomId(random.nextInt(10000))
                                    .execute();

                        } catch (Exception e) {
                            System.out.println(e.getMessage() + "!!!!");
                        }
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                        apiClient.messages().send(actor)
                                .message("Прикрепи ФОТО чел")
                                .userId(userId)
                                .randomId(random.nextInt(10000))
                                .execute();
                    }
                } else {
                    apiClient.messages().send(actor)
                            .message("Прикрепите фото")
                            .userId(userId)
                            .randomId(random.nextInt(10000))
                            .execute();
                }
            } else {
                apiClient.messages().send(actor)
                        .message("Вы не зарегистрированы.")
                        .userId(userId)
                        .randomId(random.nextInt(10000))
                        .execute();
            }
        } catch (ApiException | ClientException e) {
            System.out.println(e.getMessage());
        }
    }
}
