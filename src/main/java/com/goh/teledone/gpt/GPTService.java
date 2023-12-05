package com.goh.teledone.gpt;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.image.CreateImageRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GPTService {

    @Value("${integration.gpt.token}")
    private String token;

    public String sendMessageToGPT(String text) {
        OpenAiService service = new OpenAiService(token, Duration.ofSeconds(30));

        log.info("Streaming chat completion...");
        final List<ChatMessage> messages = new ArrayList<>();
        final ChatMessage systemMessage = new ChatMessage(ChatMessageRole.SYSTEM.value(), "Please rewrite the following message using the SMART methodology, but write the answer in the same language as the request and also separate the main points with a new line: ");
        messages.add(systemMessage);
        final ChatMessage userMessage = new ChatMessage(ChatMessageRole.USER.value(), text);
        messages.add(userMessage);

        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
                .builder()
                .model("gpt-3.5-turbo-0613")
                .messages(messages)
                .n(1)
                .maxTokens(250)
                .logitBias(new HashMap<>())
                .build();

        ChatMessage responseMessage = service.createChatCompletion(chatCompletionRequest).getChoices().get(0).getMessage();
        System.out.println(responseMessage.getContent());

        service.shutdownExecutor();

        return responseMessage.getContent();
    }

}
