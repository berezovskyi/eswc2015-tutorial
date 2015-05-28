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

final class ShowResourceCommandProcessor extends AbstractCommandProcessor {

	@Override
	public boolean canExecute(CommandContext context) {
		boolean result=false;
		if(!context.hasTarget()) {
			console().error("ERROR: No target resource specified%n");
		} else if(context.hasOptions()) {
			console().error("ERROR: No command options allowed%n");
		} else {
			result=true;
		}
		return result;
	}

	@Override
	public boolean execute(CommandContext options) {
		Resource resource=repository().resolveResource(options.target());
		if(resource==null) {
			console().error("ERROR: Unknown resource '").metadata(options.target()).error("'%n");
		} else {
			console().message("Cached resource [").message(options.target()).message("]%n");
			ShellConsole console1 = console();
			ShellUtil.showResourceMetadata(console1, resource);
			ShellUtil.showResourceContent(console1, resource);
		}
		return true;
	}

}