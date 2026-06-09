package com.Spring.AI_Customer_Support_Backend_System.ETLPipeline;

import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
//It takes care of transformming the list of documents before the generation stage ---
public class DataTransformer {

    public List<Document> transform(List<Document> documents) {
        //We use TokenTextSplitter here --- it splits the document into multiple documents of the given token size ---
        //It has various default parameters like ---
        // DEFAULT_CHUNK_SIZE, MIN_CHUNK_SIZE_CHARS , MIN_CHUNK_LENGTH_TO_EMBED(Discard chunks shorter than this), MAX_NUM_CHUNKS ,
        // KEEP_SEPARATOR(boolean --- if true and separates of separator
        //We can initialize these parameters from the constructor --- If default constructor --- then default values given ---
        var splitter = new TokenTextSplitter(
                300,   // chunk size (tokens)
                50,    // overlap
                10,    // min chunk size
                1000,  // max chunks
                true,  // keep separators
                List.of('.', '?', '!', '\n') // punctuation
        );
        List<Document> updatedDocuments = splitter.transform(documents);
        return updatedDocuments;
    }
    //Now we can add these documents inside the vector database ---
}
//Done
