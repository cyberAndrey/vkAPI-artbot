package core.utils.tasks;

import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import core.utils.DataBase;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

public class DeleteUser implements Runnable {

    Integer userId;
    VkApiClient apiClient;
    GroupActor actor;
    Random random;

    public DeleteUser(Integer userId, VkApiClient apiClient, GroupActor actor) {
        this.userId = userId;
        this.apiClient = apiClient;
        this.actor = actor;
        this.random = new Random();
    }

    @Override
    public void run() {
        try {
            if (DataBase.checkUser(userId)) {

                ResultSet photos = DataBase.getPhotosOfAuthor(userId.toString());
                while (photos.next()) {
                    String neededId = photos.getString(1);
                    DataBase.deleteFromTags(Integer.parseInt(neededId));
                    DataBase.deleteFromPhotos(Integer.parseInt(neededId));
                }
                DataBase.deleteFromUsers(userId);

                apiClient.messages().send(actor)
                        .message("Ваш профиль был успешно удален.")
                        .userId(userId)
                        .randomId(random.nextInt(10000))
                        .execute();

                } else {
                    apiClient.messages().send(actor)
                            .message("Вы не зарегистрированы.")
                            .userId(userId)
                            .randomId(random.nextInt(10000))
                            .execute();
                }
        } catch (ApiException | ClientException | SQLException e) {
            System.out.println(e.getMessage());
        }

    }

}

