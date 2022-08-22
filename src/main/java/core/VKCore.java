package core;

import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.messages.*;
import com.vk.api.sdk.objects.photos.responses.SaveResponse;
import com.vk.api.sdk.queries.messages.MessagesGetLongPollHistoryQuery;
import core.Main;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import core.utils.Strings;
import core.utils.tasks.AddNewPhoto;
import core.utils.tasks.DeleteUser;
import core.utils.tasks.Registration;
import core.utils.tasks.ViewPhotos;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;


import java.io.*;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import core.utils.DataBase;

import static core.utils.CreateKeyboard.*;

public class VKCore {

    VkApiClient apiClient;
    GroupActor actor;
    String token;
    int groupId;
    UserActor user;
    Integer timestamp;
    public static Random random = new Random();


    public VKCore() {

        try{
            final Properties properties = new Properties();
            properties.load(Main.class.getClassLoader().getResourceAsStream(Strings.CONFIG_FILE));
            this.token = properties.getProperty(Strings.TOKEN);
            this.groupId = Integer.parseInt(properties.getProperty(Strings.GROUP_ID));
            this.actor = new GroupActor(groupId, token);
            this.user = new UserActor(Integer.parseInt(properties.getProperty(Strings.USER_ID)),
                                        properties.getProperty(Strings.ACCESS));
            this.apiClient = new VkApiClient(new HttpTransportClient());
            this.timestamp = apiClient.messages().getLongPollServer(actor).execute().getTs();

            DataBase.createConnection();

        } catch (IOException exp) {
            System.out.println(exp.getMessage());
        } catch (ApiException | ClientException exp) {
            System.out.println("Проблемы с подключением к vkapi");
        }
    }

    public void start() {
        while (true) {
            MessagesGetLongPollHistoryQuery historyQuery = apiClient.messages().getLongPollHistory(actor).ts(timestamp);
            try {
                List<Message> messages = historyQuery.execute().getMessages().getItems();
                if (!messages.isEmpty()) {
                    messages.forEach(message -> {
                        if (message.getText().equals("/reg")) {
                            Registration reg = new Registration(message.getFromId(), apiClient, actor, user);
                            Thread thread = new Thread(reg);
                            thread.start();
                        }
                        if (message.getText().equalsIgnoreCase("/del")) {
                            DeleteUser del = new DeleteUser(message.getFromId(), apiClient, actor);
                            Thread thread = new Thread(del);
                            thread.start();
                        }
                        if (message.getText().equals("/add")) {
                            AddNewPhoto add = new AddNewPhoto(message.getFromId(), apiClient, actor, user);
                            Thread thread = new Thread(add);
                            thread.start();
                        }
                        if (message.getText().startsWith("/view")){
                            String authorsId = getNameFromView(message);
                            ViewPhotos view = new ViewPhotos(message.getFromId(), apiClient, actor, authorsId);
                            Thread thread = new Thread(view);
                            thread.start();
                        }
                        if(message.getText().equals("link")){
                            sendLink(message);
                        }
                        if(message.getText().equals("help")){
                            sendHelp(message);
                        }
                    });
                }
                timestamp = apiClient.messages().getLongPollServer(actor).execute().getTs();
                Thread.sleep(1000);
            } catch (ApiException | ClientException | InterruptedException exp) {
                System.out.println(exp.getMessage());
            }
        }
    }


    private void sendLink(Message message) {
        try {
            apiClient.messages().send(actor)
                    .message("https://vk.com/idogidoghut")
                    .userId(message.getFromId())
                    .randomId(random.nextInt(10000))
                    .execute();
        } catch (ApiException | ClientException e) {
            System.out.println(e.getMessage());
        }
    }

    private void sendHelp(Message message) {
        try {
            apiClient.messages().send(actor)
                    .message("Список команд:\n"+
                            "/reg - регистрация аккаунта\n" +
                            "/add - добавление новой фотографии\n" +
                            "/view - демонстрация фотографий\n" +
                            "/view ПСЕВДОНИМ - демонстрация фотографий конкретного пользователя\n" +
                            "/del - удаление аккаунта\n" +
                            "Link - ссылка на нашу группу")
                    .userId(message.getFromId())
                    .randomId(random.nextInt(10000))
                    .execute();
        } catch (ApiException | ClientException e) {
            System.out.println(e.getMessage());
        }
    }

    private String getNameFromView(Message message) {
        try {
            return message.getText().split(" ")[1];
        } catch (NullPointerException e) {
            System.out.println(e.getMessage());
        }
        return "sorry";
    }

}