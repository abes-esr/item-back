package fr.abes.item.batch.mail.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.abes.item.batch.mail.MailDto;
import fr.abes.item.core.constant.Constant;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
public class Mailer {
    private final Environment env;
    @Value("${mail.ws.url}")
    protected String url;


    @Value("${mail.admin}")
    protected String mailAdmin;

    public Mailer(Environment env) {
        this.env = env;
    }


    public void sendMail(String requestJson) {
        RestTemplate restTemplate = new RestTemplate(); //appel ws qui envoie le mail
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(requestJson, headers);

        restTemplate.getMessageConverters()
                .add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));

        try {
            restTemplate.postForObject(url + "htmlMail/", entity, String.class); //appel du ws avec
        } catch (Exception e) {
            log.error(Constant.ERROR_SENDING_MAIL_END_OF_TREATMENT + e);
        }
    }

    protected void sendMailWithAttachment(String requestJson, File f) {
        HttpPost uploadFile = new HttpPost(url + "htmlMailAttachment/");
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addTextBody("mail", requestJson, ContentType.APPLICATION_JSON);

        try {
            builder.addBinaryBody(
                    "attachment",
                    new FileInputStream(f),
                    ContentType.APPLICATION_OCTET_STREAM,
                    f.getName()
            );
        } catch (FileNotFoundException e) {
            log.error(Constant.ERROR_ATTACHMENT_NOT_FOUND + e);
        }

        HttpEntity multipart = builder.build();
        uploadFile.setEntity(multipart);

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            httpClient.execute(uploadFile);
        } catch (IOException e) {
            log.error(Constant.ERROR_ATTACHMENT_UNATTACHABLE + e);
        }
    }

    protected String mailToJSON(String to, String subject, String text) {
        String json = "";
        ObjectMapper mapper = new ObjectMapper();
        MailDto mail = new MailDto();
        mail.setApp("item");
        mail.setTo(to.split(";"));
        mail.setCc(new String[]{});
        mail.setCci(new String[]{});
        mail.setSubject(subject);
        mail.setText(text);
        try {
            json = mapper.writeValueAsString(mail);
        } catch (JsonProcessingException e) {
            log.error(Constant.ERROR_CONVERSION_MAIL_TO_JSON + e);
        }
        return json;
    }

    protected String getEnvironnementExecution() {
        if (env.getActiveProfiles().length>0)
            if (!"prod".equalsIgnoreCase(env.getActiveProfiles()[0])) {
                return env.getActiveProfiles()[0];
            }
            else
                return "";
        return "Local";
    }
}
