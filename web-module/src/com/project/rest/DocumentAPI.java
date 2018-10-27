package com.project.rest;

import com.project.configuration.Configurator;
import com.project.utility.FileIdGenerator;
import com.project.queue.ProcessingQueue;
import com.project.queue.RedisQueue;

import org.json.JSONObject;
import org.json.JSONException;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.media.multipart.BodyPartEntity;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.nio.file.Files;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileSystems;



@Path("/doc-api")
public class DocumentAPI {
    
    @GET
    @Path("/get/{service}/{id}")
    @Produces(MediaType.TEXT_PLAIN)
    public String test(@PathParam("service") String service, @PathParam("id") String id) {
        
        System.out.println("service = " + service);
        System.out.println("id = " + id);

        return "Download requested file";
    }

    @POST
    @Path("/save")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces("application/json")
    public Response upload(final FormDataMultiPart multiPart) {
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
                ids.add(new JSONObject().put("name", fileName).put("id", id).toString());
            }
        }

        return Response.status(200).entity(ids.toString()).build();
    }

    private boolean writeToBuffer(InputStream uploadedInputStream, JSONObject documentAttributes) {
        boolean result = true;
        
        String id = documentAttributes.getString("id");
        String service = documentAttributes.getString("service");
        
        String bufferPath = Configurator.getString("storage.buffer.files");
        String uploadedDocumentLocation = bufferPath + "/" + service + "/" + id;

		try {
            ProcessingQueue queue = new RedisQueue();

            // Saves document into buffer
            java.nio.file.Path path = FileSystems.getDefault().getPath(uploadedDocumentLocation);
            Files.copy(uploadedInputStream, path);

            // Enqueue attributes into Redis queue for processing
            queue.enqueue(documentAttributes.toString());
		} catch (IOException e) {
            e.printStackTrace();
            result = false;
        }
        
        return result;
	}
}