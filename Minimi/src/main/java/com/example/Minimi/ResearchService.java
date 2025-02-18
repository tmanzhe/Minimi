package com.example.Minimi;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class ResearchService {

    @Value("${gemini.api.url}")
    private String geminiApiUrl;
    @Value("${gemini.api.key}")

    private String getGeminiApiKey;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    //get isntance of webclient
    public ResearchService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder.build();
        this.objectMapper = objectMapper;
    }


    // Processes the AI prompt content
    public String processContent(ResearchRequest request) {
        // Build prompt
        String prompt = buildPrompt(request);

        // Query the AI model API, parse the response, and return the result.
        // This is tuned to Gemini API; "contents" and "parts" are arrays.
        //// Processes the AI prompt content
        //public String processContent(ResearchRequest request) {
        //    // Build prompt
        //    String prompt = buildPrompt(request);
        //
        //    // Query the AI model API, parse the response, and return the result.
        //    // This is tuned to Gemini API; "contents" and "parts" are arrays.
        //    Map<String, Object> requestBody = Map.of(
        //        "contents", new Object[] {
        //            Map.of(
        //                "parts", new Object[] {
        //                    Map.of("text", prompt)
        //                }
        //            )
        //        }
        //    );
        //
        //    // Here you would send 'requestBody' to the Gemini API and handle the response.
        //    return prompt;
        //}
        Map<String, Object> requestBody = Map.of(
                "contents", new Object[] {
                        Map.of(
                                "parts", new Object[] {
                                        Map.of("text", prompt)
                                }
                        )
                }
        );

        String response = webClient.post()
                .uri(geminiApiUrl + getGeminiApiKey)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();
                //parse response
                //return it
        return extractTextFromResponse(response);

    }

    private String extractTextFromResponse(String response) {
        try{
            GeminiResponse geminiResponse = objectMapper.readValue(response, GeminiResponse.class);
            //just trying to get the text from gemini json response. so its basic parsing
            if(geminiResponse.getCandidates() != null && !geminiResponse.getCandidates().isEmpty()){
                GeminiResponse.Candidate firstCandidate = geminiResponse.getCandidates().get(0);
                if(firstCandidate.getContent() != null && firstCandidate.getContent().getParts() != null && !firstCandidate.getContent().getParts().isEmpty()){
                    return firstCandidate.getContent().getParts().get(0).getText();
                }
            }
            return "No content found in the response...";
        }catch (Exception e){
            return "Error Parsing: " + e.getMessage();
        }
    }


    // Build the prompt based on the research request
    private String buildPrompt(ResearchRequest request) throws IllegalArgumentException {
        StringBuilder prompt = new StringBuilder();
        String operation = request.getOperation().trim().toLowerCase();

        switch (operation) {
            case "summarize":
                prompt.append("Given the following texts, provide a succinct, clear, and structured summary. ");
                prompt.append("Organize your summary with headings, highlight the main points, and use as few words as possible. ");
                prompt.append("Here is the content:\n\n");
                break;

            case "questions":
                prompt.append("Based on the following texts, generate a comprehensive list of questions that reinforce understanding of the material. ");
                prompt.append("Ensure the questions cover all key aspects in a succinct manner. ");
                prompt.append("Here is the content:\n\n");
                break;

            case "suggest":
                prompt.append("From the following texts, propose related topics in this field. ");
                prompt.append("Format your suggestions with concise headings, keeping your output minimal yet informative. ");
                prompt.append("Here is the content:\n\n");
                break;

            case "analyze":
                prompt.append("Examine the following texts and analyze the key arguments. ");
                prompt.append("Identify strengths, weaknesses, and any potential biases in a concise manner. ");
                prompt.append("Here is the content:\n\n");
                break;

            case "elaborate":
                prompt.append("Using the following texts, expand upon the ideas presented, offering additional details or related insights. ");
                prompt.append("Keep your explanation clear and concise. ");
                prompt.append("Here is the content:\n\n");
                break;

            default:
                throw new IllegalArgumentException("Unknown Operation: " + request.getOperation());
        }

        prompt.append(request.getContent());
        return prompt.toString();
    }
}
