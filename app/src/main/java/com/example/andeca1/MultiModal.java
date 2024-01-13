//package com.example.andeca1;
//import com.google.cloud.vertexai.VertexAI;
//import com.google.cloud.vertexai.api.Blob;
//import com.google.cloud.vertexai.api.Content;
//import com.google.cloud.vertexai.api.GenerateContentResponse;
//import com.google.cloud.vertexai.api.GenerationConfig;
//import com.google.cloud.vertexai.api.Part;
//import com.google.cloud.vertexai.generativeai.preview.GenerativeModel;
//import com.google.cloud.vertexai.generativeai.preview.ResponseStream;
//import com.google.protobuf.ByteString;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Base64;
//import java.util.List;
//
//public class MultiModal {
//    public static void main(String[] args) throws IOException {
//        try (VertexAI vertexAi = new VertexAI("booming-landing-410605", "asia-southeast1"); ) {
//            GenerationConfig generationConfig =
//                    GenerationConfig.newBuilder()
//                            .setMaxOutputTokens(2048)
//                            .setTemperature(0.9F)
//                            .setTopP(1)
//                            .build();
//            GenerativeModel model = new GenerativeModel("gemini-pro", generationConfig, vertexAi);
//
//            List<Content> contents = new ArrayList<>();
//            contents.add(Content.newBuilder().setRole("user").addParts(Part.newBuilder().setText("Hello")).build());
////            contents.add(Content.newBuilder().setRole("model").addParts(Part.newBuilder().setText("Greetings! How may I assist you today?")).build());
//
//            ResponseStream<GenerateContentResponse> responseStream = model.generateContentStream(contents);
//            // Do something with the response
//            responseStream.stream().forEach(System.out::println);
//        }
//    }
//}