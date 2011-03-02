/*
 * Copyright (c) 2011 Derek Murray <Derek.Murray@cl.cam.ac.uk>
 *
 * Permission to use, copy, modify, and distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */
package com.asgow.ciel.references;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.asgow.ciel.protocol.CielProtos.NetworkLocation;
import com.asgow.ciel.protocol.CielProtos.Reference.Builder;
import com.asgow.ciel.protocol.CielProtos.Reference.ReferenceType;

public class ConcreteReference extends Reference {

	private final HashSet<Netloc> locationHints;
	private final long sizeHint;
	
	public ConcreteReference (String id, long sizeHint) {
		super(id);
		this.locationHints = new HashSet<Netloc>();
		this.sizeHint = sizeHint; 
	}
	
	public ConcreteReference (String id, long sizeHint, Iterable<Netloc> locations) {
		this(id, sizeHint);
		for (Netloc location : locations) {
			this.addLocation(location);
		}
	}
	
	public ConcreteReference (String id, long sizeHint, Netloc location) {
		this(id, sizeHint);
		this.addLocation(location);
	}
	
	public ConcreteReference (com.asgow.ciel.protocol.CielProtos.Reference ref) {
		this(ref.getId(), ref.getSizeHint());
		for (NetworkLocation netloc : ref.getLocationHintsList()) {
			this.addLocation(new Netloc(netloc));
		}
	}
	
	public void addLocation(Netloc netloc) {
		this.locationHints.add(netloc);
	}
	
	public Iterable<Netloc> getLocationHints() {
		return Collections.unmodifiableSet(this.locationHints);
	}
	
	public long getSizeHint() {
		return this.sizeHint;
	}
	
	public boolean isConsumable() {
		return true;
	}

	@Override
	public Builder buildProtoBuf(Builder builder) {
		builder.setType(ReferenceType.CONCRETE).setSizeHint(this.sizeHint);
		for (Netloc hint : this.locationHints) {
			builder.addLocationHints(hint.asProtoBuf());
		}		
		return builder;
	}
	
}