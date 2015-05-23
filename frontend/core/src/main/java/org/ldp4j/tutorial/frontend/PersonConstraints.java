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
 *   Artifact    : org.ldp4j.tutorial.frontend:frontend-core:1.0.0-SNAPSHOT
 *   Bundle      : frontend-core-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.tutorial.frontend;

import java.net.URI;

import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.data.DataSetFactory;
import org.ldp4j.application.data.ExternalIndividual;
import org.ldp4j.application.data.ManagedIndividual;
import org.ldp4j.application.data.ManagedIndividualId;
import org.ldp4j.application.data.Name;
import org.ldp4j.application.data.NamingScheme;
import org.ldp4j.application.data.constraints.Constraints;
import org.ldp4j.application.data.constraints.Constraints.Cardinality;
import org.ldp4j.application.data.constraints.Constraints.PropertyConstraint;
import org.ldp4j.application.data.constraints.Constraints.Shape;
import org.ldp4j.application.ext.InconsistentContentException;
import org.ldp4j.application.ext.UnsupportedContentException;
import org.ldp4j.tutorial.application.api.Person;

import com.google.common.base.Objects;

public final class PersonConstraints implements PersonVocabulary {

	private PersonConstraints() {
	}

	private static Constraints createConstraints(Person currentPerson) {
		Name<String> name=NamingScheme.getDefault().name("");
		if(currentPerson!=null) {
			name=AgendaApplicationUtils.name(currentPerson);
		}

		DataSet tmp=DataSetFactory.createDataSet(name);

		PropertyConstraint emailConstraint = null;
		if(currentPerson!=null) {
			ExternalIndividual emailIndividual=
				tmp.individual(
					URI.create(currentPerson.getEmail()),
					ExternalIndividual.class);

			emailConstraint=
				Constraints.
					propertyConstraint(URI.create(EMAIL)).
						withValue(emailIndividual);
		} else {
			emailConstraint=
				Constraints.
					propertyConstraint(URI.create(EMAIL)).
						withCardinality(Cardinality.mandatory());
		}

		Shape shape=
			Constraints.
				shape().
					withPropertyConstraint(
						emailConstraint).
					withPropertyConstraint(
						Constraints.
							propertyConstraint(URI.create(NAME)).
								withCardinality(Cardinality.mandatory())).
					withPropertyConstraint(
						Constraints.
							propertyConstraint(URI.create(LOCATION)).
								withCardinality(Cardinality.mandatory())).
					withPropertyConstraint(
						Constraints.
							propertyConstraint(URI.create(WORKPLACE_HOMEPAGE)).
								withCardinality(Cardinality.mandatory()));
		Constraints constraints =
			Constraints.
				constraints();
		if(currentPerson!=null) {
			ManagedIndividualId personIndividualId=ManagedIndividualId.createId(name, PersonHandler.ID);
			ManagedIndividual personIndividual=tmp.individual(personIndividualId,ManagedIndividual.class);
			constraints.withNodeShape(personIndividual,shape);
		} else {
			constraints.withTypeShape(URI.create(PERSON),shape);
		}
		return constraints;
	}

	public static void validate(Typed<Person> typedPerson) throws UnsupportedContentException {
		Person person=typedPerson.get();
		if(!typedPerson.hasType(PERSON) || person.getEmail()==null || person.getName()==null || person.getLocation()==null || person.getWorkplaceHomepage()==null) {
			throw new UnsupportedContentException("Incomplete person definition",createConstraints(null));
		}
	}

	public static void checkConstraints(Person currentPerson, Typed<Person> updatedPerson) throws InconsistentContentException {
		if(!Objects.equal(currentPerson.getEmail(),updatedPerson.get().getEmail())) {
			throw new InconsistentContentException("Person email cannot be modified",createConstraints(currentPerson));
		}
	}

}