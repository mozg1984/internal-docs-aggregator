package com.project.rest;

import com.project.index.DocumentOperations;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import javax.ws.rs.core.MediaType;

/**
 * REST API methods:
 * GET:  /doc-api/search/{service}/{query}
 * GET:  /doc-api/get/{service}/{id}
 * POST: /doc-api/create
 * POST: /doc-api/delete
 */
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
        String response = documentOperations.search(service, query);
        return Response.status(200).entity(response).build();
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
        String response = documentOperations.create(multiPart);
        return Response.status(200).entity(response).build();
    }

    @POST
    @Path("/delete")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces("application/json")
    public Response delete(String data) {
        String response = documentOperations.delete(data);
        return Response.status(200).entity(response).build();
    }
}