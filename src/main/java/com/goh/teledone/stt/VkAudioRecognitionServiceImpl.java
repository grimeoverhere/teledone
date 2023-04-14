package com.goh.teledone.stt;

import com.goh.teledone.stt.model.ResultStatus;
import com.goh.teledone.stt.model.VkAsrResponse;
import com.goh.teledone.stt.model.VkAsrResult;
import com.goh.teledone.stt.model.VkRecognitionResponse;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.ServiceActor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Objects;

import static java.lang.Thread.sleep;

/**
 * Сервис, взаимодействующий с сервисом распознавания речи.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VkAudioRecognitionServiceImpl implements VkAudioRecognitionService {

    private static final String ASR_GET_UPLOAD_URL_PATH = "asr.getUploadUrl";
    private static final String ASR_PROCESS_PATH = "asr.process";
    private static final String ASR_CHECK_STATUS_PATH = "asr.checkStatus";

    /**
     * Задержка перед запросом результатов распознавания.
     */
    @Value("${integration.vk.stt-poll-interval}")
    private int pollInterval;

    @NonNull
    private VkApiClient vkApiClient;
    @NonNull
    private ServiceActor serviceActor;
    /**
     * Сервис получения информации о файле из Telegram.
     */
    @NonNull
    private TelegramFileService telegramFileService;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUploadURL() {

        RestTemplate restTemplate = new RestTemplate();

        MultiValueMap<String, String> map = prepareRequestParams();

        ResponseEntity<VkRecognitionResponse> response = sendRequest(restTemplate, map);
        String uploadUrl = response.getBody().getResponse().getUploadUrl();

        log.info(String.format("Upload URL: %s", uploadUrl));
        return uploadUrl;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String sendToRecognition(final Update message) {
        RestTemplate restTemplate = new RestTemplate();
        ByteArrayResource fileData = prepareFileData(message);

        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("file", fileData);

        ResponseEntity<String> stringResponseEntity = restTemplate.postForEntity(getUploadURL(), map, String.class);

        return stringResponseEntity.getBody();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String startVoiceRecognition(final String messageInfo) {

        MultiValueMap<String, String> map = prepareRequestParams();
        map.add("model", "spontaneous");
        map.add("audio", messageInfo);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<VkAsrResponse> response = restTemplate.postForEntity(vkApiClient.getApiEndpoint() + ASR_PROCESS_PATH,
                map,
                VkAsrResponse.class);

        return response.getBody().getResponse().getTaskId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public VkAsrResult.TextResponse getResponseFromService(final String taskId) {
        RestTemplate restTemplate = new RestTemplate();

        MultiValueMap<String, String> map = prepareRequestParams();
        map.add("task_id", taskId);

        ResponseEntity<VkAsrResult> response = restTemplate.postForEntity(vkApiClient.getApiEndpoint() + ASR_CHECK_STATUS_PATH,
                map,
                VkAsrResult.class);
        return response.getBody().getResponse();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String pollForText(final String taskId) {
        Boolean continueChecks = Boolean.TRUE;
        VkAsrResult.TextResponse result = new VkAsrResult.TextResponse();

        result = pollService(taskId, continueChecks, result);

        return handleServiceResponse(result);
    }

    private ResponseEntity<VkRecognitionResponse> sendRequest(final RestTemplate restTemplate,
                                                              final MultiValueMap<String, String> map) {
        ResponseEntity<VkRecognitionResponse> response = restTemplate.postForEntity(vkApiClient.getApiEndpoint() + ASR_GET_UPLOAD_URL_PATH,
                map,
                VkRecognitionResponse.class);
        return response;
    }

    /**
     * Вспомогательный метод для обработки ответов сервиса.
     *
     * @param response ответ сервиса
     * @return результат распознавания, либо сообщение об ошибке
     */
    private static String handleServiceResponse(final VkAsrResult.TextResponse response) {
        if (response.getStatus().equals(ResultStatus.INTERNAL_ERROR.getStatus())) {
            return "Внутренняя ошибки сервиса распознавания речи ВКонтакте";
        }
        if (response.getStatus().equals(ResultStatus.RECOGNITION_ERROR.getStatus())) {
            return "Ошибка распознавания речи, сложности в распознавании. "
                    + "Попробуйте говорить чётче или снизить фоновые шумы";
        }
        if (response.getStatus().equals(ResultStatus.TRANSCODING_ERROR.getStatus())) {
            return "Ошибка перекодирования аудиозаписи во внутренний формат. "
                    + "Попробуйте загрузить аудиозапись в другом поддерживаемом формате";
        }
        if (Objects.equals(response.getText(), "")
                && response.getStatus().equals(ResultStatus.FINISHED.getStatus())) {
            return "Мне не удалось распознать голос, попробуйте другой файл.";
        }

        return response.getText();
    }

    /**
     * Вспомогательный метод для опроса сервиса распознавания.
     *
     * @param taskId         идентификатор задачи
     * @param continueChecks флаг продолжения опроса
     * @param result         результат
     * @return ответ сервиса типа {@link VkAsrResult.TextResponse}
     */
    private VkAsrResult.TextResponse pollService(final String taskId, final Boolean continueChecks, final VkAsrResult.TextResponse result) {
        VkAsrResult.TextResponse response = result;
        Boolean process = continueChecks;

        while (Boolean.TRUE.equals(process)) {
            log.info("Распозначание файла в процессе...");
            response = getResponseFromService(taskId);

            try {
                sleep(pollInterval);
            } catch (InterruptedException ex) {
                log.error("Ошибка при попытке поставить поток на паузу");
                ex.printStackTrace();
            }

            if (response != null && !response.getStatus().equals(ResultStatus.PROCESSING.getStatus())) {
                process = Boolean.FALSE;
                log.info("Распознавание файла завершено");
            }
        }
        return response;
    }

    /**
     * Вспомогательный метод для подготовки голосового файла к отправке.
     *
     * @param message сообщение
     * @return объект типа {@link ByteArrayResource}
     */
    @SneakyThrows
    private ByteArrayResource prepareFileData(final Update message) {
        return new ByteArrayResource(telegramFileService.simpleDownloadFile(message)) {
            @Override
            public String getFilename() {
                return "filename";
            }
        };
    }

    /**
     * Вспомогательный метод для подготовки параметров запроса.
     *
     * @return карта параметров
     */
    private MultiValueMap<String, String> prepareRequestParams() {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("access_token", serviceActor.getAccessToken());
        map.add("v", vkApiClient.getVersion());
        return map;
    }
}
