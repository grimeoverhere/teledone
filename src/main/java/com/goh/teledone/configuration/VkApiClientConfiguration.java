package com.goh.teledone.configuration;

import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.ServiceActor;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VkApiClientConfiguration {

    @Bean
    public VkApiClient getVkApiClient() {
        TransportClient transportClient = HttpTransportClient.getInstance();
        return new VkApiClient(transportClient);
    }

    @Bean
    public ServiceActor getServiceActor(
            //ID приложения
            @Value("${integration.vk.app-id}") Integer appId,
            //защищенный_ключ
            @Value("${integration.vk.client-secret}") String clientSecret,
            //сервисный_ключ_доступа
            @Value("${integration.vk.service-access-key}") String serviceAccessKey
    ) {
         return new ServiceActor(appId, clientSecret, serviceAccessKey);
    }

    //implement if we do need user auth
    //according to this issue we don't need auth for service access key, just use the key from the site.
    //https://github.com/VKCOM/vk-java-sdk/issues/73
//        ServiceClientCredentialsFlowResponse authResponse = vk.oAuth()
//                .groupAuthorizationCodeFlow(APP_ID, CLIENT_SECRET, "http://www.goh-music.com", )
//                .serviceClientCredentialsFlow(APP_ID, CLIENT_SECRET)
//                .execute();
}
