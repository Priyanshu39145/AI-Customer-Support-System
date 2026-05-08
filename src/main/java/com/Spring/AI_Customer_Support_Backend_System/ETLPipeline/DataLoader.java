package com.Spring.AI_Customer_Support_Backend_System.ETLPipeline;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.JsonReader;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
//This class only works for loading the data and return the List of Documents ---
public class DataLoader {
//    //We get the pdf as resource file from the resources section
//    @Value("classpath:cricket_rules.pdf")
//    private Resource pdfResource;

    //Loading documents from pdf
    public List<Document> loadDocumentsFromPDF(Resource pdfResource)    {
        //We are making a pdfreader ---- using a PagePdfDocumentReader --- It is an implementation of DocumentReader ---
        //We give it the pdfResource and the configuration --- by PdfDocumentReaderConfig ---
        //Inside the config we define multiple things ---
        //We can also use ParagraphPdfDocumentReader ---
        // The ParagraphPdfDocumentReader uses the PDF catalog (e.g. TOC) information to split the input PDF into text paragraphs
        // and output a single Document per paragraph. NOTE: Not all PDF documents contain
        //the PDF catalog.
        //We can also use TikaDocumentReader ---
        //The TikaDocumentReader uses Apache Tika to extract text from a variety of document formats, such as PDF, DOC/DOCX, PPT/PPT, and HTML.
        //We need to have the tika document reader dependency to use it
        var pdfReader = new PagePdfDocumentReader(pdfResource,
                PdfDocumentReaderConfig.builder()
                        .withPageTopMargin(0) // Configures --- the reader margin to 0 --- meaning we start reading from level 0
                        //Here we define the extract text formatter configuration ---
                        //We define the number of bottom text lines to delete to 0 ---
                        .withPageExtractedTextFormatter(ExtractedTextFormatter.builder()
                                .withNumberOfBottomTextLinesToDelete(0)
                                .build())
                        //Here we define each document will contain how many pages of the pdf content
                        .withPagesPerDocument(1)
                        .build()
        );
        return pdfReader.read(); // Here we read the data from the pdf and return the list of documents received
    }

}
