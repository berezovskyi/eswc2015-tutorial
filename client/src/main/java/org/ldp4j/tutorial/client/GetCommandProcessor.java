/**
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   This file is part of the LDP4j Project:
 *     http://www.ldp4j.org/
 *
 *   Center for Open Middleware
 *     http://www.centeropenmiddleware.com/
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Copyright (C) 2014 Center for Open Middleware.
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *             http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Artifact    : org.ldp4j.tutorial.client:eswc-2015-client:1.0.0-SNAPSHOT
 *   Bundle      : eswc-2015-client-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.tutorial.client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.util.EntityUtils;

final class GetCommandProcessor extends AbstractLdpCommandProcessor {

	private String location;
	private String entity;

	@Override
	public boolean canExecute(CommandContext context) {
		boolean result=false;
		try {
			this.location=requireTargetResource(context);
			this.entity=requireEntity(context);
			result=true;
		} catch (CommandRequirementException e) {
			console().error("ERROR: %s%n",e.getMessage());
		}
		return result;
	}

	private String requireEntity(CommandContext context) {
		String entity = context.entity();
		if(entity==null) {
			entity=ShellUtil.nextResourceFile();
		}
		return entity;
	}

	@Override
	protected void processResponse(HttpResponse response) throws IOException {
		Links links = getLinks(response);
		if(!links.hasLink("type",URI.create("http://www.w3.org/ns/ldp#Resource"))) {
			console().error("Not a LDP resource%n");
			ShellUtil.showLinks(console(),links);
			return;
		}

		int statusCode = response.getStatusLine().getStatusCode();
		Resource resource = refreshResource(this.location,response);
		if(statusCode==200) {
			repository().updateResource(resource);
			console().message("Resource retrieved:%n");
			ShellUtil.showResourceMetadata(console(),resource);
			ShellUtil.showLinks(console(),links);
			ShellUtil.showResourceContent(console(),resource);
			persist(this.entity,resource.entity());
		} else {
			processUnexpectedResponse(response, "Cannot retrieve resource");
		}
	}

	private void persist(String entityResource, String entity) {
		File file=new File(entityResource);
		FileOutputStream fos=null;
		try {
			fos=new FileOutputStream(file);
			IOUtils.write(entity, fos);
			console().
				message("Content persisted to ").
				metadata(entityResource).
				data("%n");
		} catch (IOException e) {
			console().
				error("ERROR: Could not persist entity (%s)", e.getMessage());
		} finally {
			IOUtils.closeQuietly(fos);
		}

	}

	@Override
	protected HttpUriRequest getRequest(CommandContext options, RequestConfig config) {
		console().
			message("Retrieving resource...%n");

		HttpGet method = new HttpGet(this.location);
		method.setHeader("Accept", contentType(options)+"; charset=utf-8");
		method.setConfig(config);

		return method;
	}

	private Resource refreshResource(String location, HttpResponse httpResponse) throws IOException {
		Resource resource=repository().resolveResource(location);
		if(resource==null) {
			resource=repository().createResource(location);
		}
		HttpEntity entity = httpResponse.getEntity();
		if(entity!=null) {
			resource.
				withContentType(entity.getContentType().getValue()).
				withEntity(EntityUtils.toString(entity));
		}
		Header etag=httpResponse.getFirstHeader("ETag");
		if(etag!=null) {
			resource.withEntityTag(etag.getValue());
		}
		Header lastModified=httpResponse.getFirstHeader("Last-Modified");
		if(lastModified!=null) {
			resource.withLastModified(lastModified.getValue());
		}
		return resource;
	}

	private String contentType(CommandContext options) {
		String rawContentType=options.contentType();
		if(rawContentType==null) {
			rawContentType="text/turtle";
		}
		return rawContentType;
	}

}