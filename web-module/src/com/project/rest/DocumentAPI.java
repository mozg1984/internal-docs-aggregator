package com.project.rest;

import com.project.configuration.Configurator;
import com.project.utility.FileIdGenerator;
import com.project.queue.ProcessingQueue;
import com.project.queue.RedisQueue;

import org.json.JSONObject;
import com.project.index.DocumentSearcher;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;

import javax.ws.rs.core.MediaType;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;
import java.nio.file.FileSystems;

@Path("/doc-api")
public class DocumentAPI {
    @GET
    @Path("/search/{query}")
    @Produces("application/json")
    public Response search(@PathParam("query") String query) {
        DocumentSearcher searcher = new DocumentSearcher();
        List<String> documents = searcher.searchBy(query);
        return Response.status(200).entity(documents.toString()).build();
    }
    
    @GET
    @Path("/get/{service}/{id}")
    public Response get(@PathParam("service") String service, @PathParam("id") String id) {
        StreamingOutput fileStream =  new StreamingOutput() {
            @Override
            public void write(OutputStream output) throws IOException, WebApplicationException {
                try {
                    // Firstable, try to find file in buffer
                    java.nio.file.Path path = Paths.get(getDocPathInBuffer(service, id));

                    // If document does not exists in buffer try to find it in storage
                    if (!Files.exists(path)) {
                        path = Paths.get(getDocPathInStorage(service, id));
                    }

                    byte[] data = Files.readAllBytes(path);
                    output.write(data);
                    output.flush();
                } catch (Exception e) {
                    throw new WebApplicationException("Document Not Found");
                }
            }
        };
        
        return Response
                .ok(fileStream, MediaType.APPLICATION_OCTET_STREAM)
                .header("content-disposition", "attachment; filename = document")
                .build();
    }

    @POST
    @Path("/save")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces("application/json")
    public Response save(final FormDataMultiPart multiPart) {
        FormDataBodyPart attributesPart = multiPart.getField("attributes");
        String attributes = attributesPart.getValue();

        List<FormDataBodyPart> documentParts = multiPart.getFields("document");
        List<String> ids = new ArrayList<>();
        JSONObject documentAttributes;
        
        for (FormDataBodyPart part : documentParts) {
            InputStream inputStream = part.getEntityAs(InputStream.class);
            FormDataContentDisposition fileDetail = part.getFormDataContentDisposition();

            documentAttributes = new JSONObject(attributes);
            String service = documentAttributes.getString("service");

            String id = new FileIdGenerator().generateFor(service);
            String fileName = fileDetail.getFileName();

            documentAttributes.put("id", id);
            documentAttributes.put("name", fileName);

            if (writeToBuffer(inputStream, documentAttributes)) {
                ids.add(
                    new JSONObject()
                        .put("name", fileName)
                        .put("id", id)
                        .toString()
                );
            }
        }

        return Response.status(200).entity(ids.toString()).build();
    }

    private boolean writeToBuffer(InputStream uploadedInputStream, JSONObject documentAttributes) {
        boolean result = true;
        
        String id = documentAttributes.getString("id");
        String service = documentAttributes.getString("service");
        String uploadedDocumentLocation = getDocPathInBuffer(service, id);

		try {
            ProcessingQueue queue = new RedisQueue();

            // Saves document into buffer
            java.nio.file.Path path = FileSystems.getDefault().getPath(uploadedDocumentLocation);
            Files.copy(uploadedInputStream, path);

            // Enqueue attributes into Redis queue for processing
            documentAttributes.put("action", "index");
            queue.enqueue(documentAttributes.toString());
		} catch (IOException e) {
            e.printStackTrace();
            result = false;
        }
        
        return result;
    }
    
    private String getDocPathInBuffer(String service, String id) {
        String bufferPath = Configurator.getString("storage.buffer.files");
        return bufferPath + "/" + service + "/" + id;
    }

    private String getDocPathInStorage(String service, String id) {
        String storagePath = Configurator.getString("storage.files");
        return storagePath + "/" + service + "/" + id;
    }
}