package com.project.rest;

import com.project.configuration.Configurator;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
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
import java.util.List;
import java.io.File;
import java.nio.file.Files;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileSystems;



@Path("/doc-api")
public class DocumentAPI {
    @GET
    @Path("/test")
    @Produces(MediaType.TEXT_PLAIN)
    public String test() {
        System.out.println("Test!!!!!");

        return "Test";
    }

    @POST
    @Path("/save")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response upload(final FormDataMultiPart multiPart) {
        List<FormDataBodyPart> bodyParts = multiPart.getFields("document");
        
        System.out.println("Step 1");

        for (FormDataBodyPart part : bodyParts) {
            InputStream inputStream = part.getEntityAs(InputStream.class);
            FormDataContentDisposition fileDetail = part.getFormDataContentDisposition();
            writeToBuffer(inputStream, fileDetail.getFileName());
        } 

        System.out.println("Step 2");

        return Response.status(200).entity("Uploaded").build();
    }

    private void writeToBuffer(InputStream uploadedInputStream, String fileName) {
        String bufferPath = Configurator.getString("storage.buffer.files.dispatchers");
        String uploadedFileLocation = bufferPath + "/" + fileName;

        System.out.println(uploadedFileLocation);

		try {
            java.nio.file.Path path = FileSystems.getDefault().getPath(uploadedFileLocation);
            Files.copy(uploadedInputStream, path);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}