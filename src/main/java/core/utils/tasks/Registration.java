package core.utils.tasks;

import com.google.gson.Gson;
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
import com.vk.api.sdk.objects.users.Fields;
import com.vk.api.sdk.objects.users.User;
import com.vk.api.sdk.objects.users.responses.GetResponse;
import com.vk.api.sdk.queries.photos.PhotosGetQuery;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import core.utils.DataBase;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Registration implements Runnable {

    Integer userId;
    Integer lastMessageId;
    VkApiClient apiClient;
    GroupActor actor;
    Random random;
    Date created;
    Message ans;
    UserActor user;


    String userName, nick;
    boolean anonymous;

    public Registration(Integer userId, VkApiClient apiClient, GroupActor actor, UserActor userA) {
        this.userId = userId;
        this.apiClient = apiClient;
        this.actor = actor;
        this.random = new Random();
        this.created = new Date();
        this.user = userA;
    }

    @Override
    public void run()
    {
        Gson gson = new Gson();
        List<GetResponse> jsons;
        try {
            if (DataBase.checkUser(userId)) {
                apiClient.messages().send(actor)
                        .message("Вы уже зaрегистрированы.")
                        .userId(userId)
                        .randomId(random.nextInt(10000))
                        .execute();

                return;
            }

            jsons = apiClient.users().get(actor).userIds(userId.toString()).fields(Fields.FIRST_NAME_NOM).execute();
            User userInfo = gson.fromJson(jsons.get(0).toString(), User.class);
            this.userName = userInfo.getFirstName() + " " + userInfo.getLastName();
            apiClient.messages().send(actor)
                    .message("Напишите ваш ник")
                    .userId(userId)
                    .randomId(random.nextInt(10000))
                    .execute();


            GetHistoryResponse historyQuery = apiClient.messages().getHistory(actor).userId(userId).execute();
            this.lastMessageId = historyQuery.getItems().get(0).getId();

            Date timestamp;
            do {
                if (getAnswer(lastMessageId)) {
                    System.out.println(ans.getFromId() + " " + ans.getText());
                    break;
                }
                Thread.sleep(5000);
                timestamp = new Date();
            } while (timestamp.getTime() - created.getTime() < 180000);

            if (ans != null) {
                this.nick = getNick(ans.getText());
                apiClient.messages().send(actor)
                        .message("Хотите ли вы быть анонимным? [Да \\ Нет]")
                        .userId(userId)
                        .randomId(random.nextInt(10000))
                        .execute();


                historyQuery = apiClient.messages().getHistory(actor).userId(userId).execute();
                this.lastMessageId = historyQuery.getItems().get(0).getId();

                do {
                    if (getAnswer(lastMessageId))
                        break;
                    Thread.sleep(5000);
                    timestamp = new Date();
                } while (timestamp.getTime() - created.getTime() < 180000);

                if (ans != null) {
                    this.anonymous = ans.getText().equalsIgnoreCase("да".trim());

                    System.out.println(ans.getText() + " " + ans.getFromId());

                    apiClient.messages().send(actor)
                            .message("Прикрепите одну картинку для портфолио [как фото]")
                            .userId(userId)
                            .randomId(random.nextInt(10000))
                            .execute();

                    historyQuery = apiClient.messages().getHistory(actor).userId(userId).execute();
                    this.lastMessageId = historyQuery.getItems().get(0).getId();

                    do {
                        if (getAnswer(lastMessageId))
                            break;
                        Thread.sleep(5000);
                        timestamp = new Date();
                    } while (timestamp.getTime() - created.getTime() < 180000);

                    if (ans != null) {
                        // Скачивание фото
                        GetUploadServerResponse gusr = apiClient.photos().getUploadServer(user).albumId(280673016).groupId(206249029).execute();
                        Photo photo = ans.getAttachments().get(0).getPhoto();
                        Photos photos = new Photos(apiClient);

                        PhotosGetQuery result = photos.get(user).albumId("280673016").ownerId(-206249029).rev(true).count(1);
                        System.out.println(result.photoIds().execute());




                        String url = photo.getSizes().get(4).getUrl().toString();
                        url.replace("/impf", "");
                        String filename = downloadForURL(url);
                        // добавление в альбом
                        URI uri = gusr.getUploadUrl();
                        try {
                            String res = createPOST(uri.toString(), filename);
                            List<SaveResponse> res2 = loadPhoto(res, nick, user);
                            System.out.println(res2);

                            // Добавление записи в авторы
                            String[] fields = {ans.getFromId().toString(), userName, String.valueOf(anonymous), nick};
                            DataBase.insertIntoAuthors(fields);
                            // Добавление записи в фото
                            DataBase.insertToPhotos(result.photoIds().execute().getItems().get(0).getId().toString(), ans.getFromId().toString());

                            apiClient.messages().send(actor)
                                    .message("Регистрация завершена!")
                                    .userId(userId)
                                    .randomId(random.nextInt(10000))
                                    .execute();

                            return;

                        } catch (Exception e) {
                            System.out.println(e.getMessage() + "!!!!");
                        }


                    }
                    return;
                }

            }

            // Если не нашли сообщение за 3 минуты
            apiClient.messages().send(actor)
                    .message("Время регистрации вышло. Повторите попытку.")
                    .userId(userId)
                    .randomId(random.nextInt(10000))
                    .execute();


        } catch (ClientException | ApiException | InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }

    public String getNick(String msg){
        String regex = "(\\S+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(msg);
        matcher.find();
        return matcher.group(0);
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

    public String createPOST(String uri, String filename) throws Exception {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost uploadFile = new HttpPost(uri);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addTextBody("file1", "101010101010101010", ContentType.TEXT_PLAIN);

// This attaches the file to the POST:
        File f = new File(filename);
        builder.addBinaryBody(
                "file1",
                new FileInputStream(f),
                ContentType.APPLICATION_OCTET_STREAM,
                f.getName()
        );

        HttpEntity multipart = builder.build();
        uploadFile.setEntity(multipart);
        CloseableHttpResponse response = httpClient.execute(uploadFile);
        HttpEntity responseEntity = response.getEntity();
        String str =  IOUtils.toString(responseEntity.getContent(), StandardCharsets.UTF_8);
        str = str.replaceAll("\\\\", "");
        return str;
    }



    public  List<SaveResponse> loadPhoto(String response, String caption, UserActor actor) throws Exception {
        String serverRel = "\"server\":([\\d]+)";
        String hashRel = "\"hash\":\"([\\w]+)\"";
        String photosRel = "photos_list\":\"(.+?)\",\"aid";
        Pattern pattern = Pattern.compile(serverRel);
        Matcher matcher = pattern.matcher(response);
        matcher.find();
        int server = Integer.parseInt(matcher.group(1));
        pattern = Pattern.compile(hashRel);
        matcher = pattern.matcher(response);
        matcher.find();
        String hash = matcher.group(1);
        pattern = Pattern.compile(photosRel);
        matcher = pattern.matcher(response);
        matcher.find();
        String photosList = matcher.group(1);

        List<SaveResponse> res = apiClient.photos().save(actor).caption("Имя автора: "+caption).albumId(280673016).groupId(206249029).server(server).photosList(photosList).hash(hash).execute();
        return res;
    }

    public String downloadForURL(String ulr) {
        Random rand = new Random();
        String fileName = "static/test"+rand.nextInt(100)+".png";
        try{
            URL url = new URL(ulr);
            InputStream inputStream = url.openStream();
            OutputStream outputStream = new FileOutputStream(fileName);
            byte[] buffer = new byte[2048];
            int length = 0;
            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }
            inputStream.close();
            outputStream.close();

        } catch(Exception ignored) {}
        System.out.println("\n******" + fileName + "***\n");
        return fileName;
    }
}
