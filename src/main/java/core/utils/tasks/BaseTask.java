package core.utils.tasks;

import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.messages.Message;
import com.vk.api.sdk.objects.messages.responses.GetHistoryResponse;
import com.vk.api.sdk.objects.photos.responses.SaveResponse;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class BaseTask implements Runnable {
    Integer userId;
    VkApiClient apiClient;
    Random random;
    GroupActor actor;

    public BaseTask(Integer userId, VkApiClient apiClient, GroupActor actor) {
        this.userId = userId;
        this.apiClient = apiClient;
        this.random = new Random();
        this.actor = actor;
    }

    protected String getNick(String msg){
        String regex = "(\\S+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(msg);
        matcher.find();
        return matcher.group(0);
    }

    protected String createPOST(String uri, String filename) throws Exception {
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

    protected List<SaveResponse> loadPhoto(String response, String caption, UserActor actor) throws Exception {
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

    protected String downloadForURL(String ulr) {
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
