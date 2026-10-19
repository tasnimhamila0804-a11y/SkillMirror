package com.skillmirror.ai;

/** Raw completion contract of an LLM provider. */
public interface AiClient {

    record AiCompletion(String text, int promptTokens, int completionTokens, String model, long latencyMs) {}

    boolean supportsLiveCalls();

    AiCompletion complete(String systemPrompt, String userPrompt) throws AiException;
}
