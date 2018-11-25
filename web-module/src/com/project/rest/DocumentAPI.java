package com.project.rest;

import com.project.configuration.Configurator;
import com.project.DocumentParser;
import com.project.utility.FileIdGenerator;
import com.project.utility.FileHasher;
import com.project.utility.DocumentIdentifier;
import com.project.index.DocumentOperations;
import com.project.queue.ProcessingQueue;
import com.project.queue.RedisQueue;

import org.json.JSONObject;
import com.project.DocumentSearcher;

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
    private DocumentOperations documentOperations;
    
    public DocumentAPI() {
        documentOperations = new DocumentOperations();
    }
    
    @GET
    @Path("/search/{service}/{query}")
    @Produces("application/json")
    public Response search(@PathParam("service") String service, @PathParam("query") String query) {
        String documents = documentOperations.search(service, query);
        return Response.status(200).entity(documents).build();
    }
    
    @GET
    @Path("/get/{service}/{id}")
    public Response get(@PathParam("service") String service, @PathParam("id") String id) {
        StreamingOutput fileStream = documentOperations.read(service, id);        
        return Response
                .ok(fileStream, MediaType.APPLICATION_OCTET_STREAM)
                .header("content-disposition", "attachment; filename = document")
                .build();
    }

    @POST
    @Path("/create")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces("application/json")
    public Response create(final FormDataMultiPart multiPart) {
        String ids = documentOperations.create(multiPart);
        return Response.status(200).entity(ids).build();
    }

    // delete
}