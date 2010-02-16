/*
 * Licensed to Elastic Search and Shay Banon under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. Elastic Search licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.action.terms;

import com.google.common.collect.Iterators;
import org.elasticsearch.action.support.broadcast.BroadcastOperationResponse;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.elasticsearch.action.terms.FieldTermsFreq.*;

/**
 * The response of terms request. Includes a list of {@link FieldTermsFreq} which include
 * the field and all its term / doc freq pair.
 *
 * @author kimchy (Shay Banon)
 */
public class TermsResponse extends BroadcastOperationResponse implements Iterable<FieldTermsFreq> {

    private long numDocs;

    private long maxDoc;

    private long numDeletedDocs;

    private FieldTermsFreq[] fieldsTermsFreq;

    private transient Map<String, FieldTermsFreq> fieldsTermsFreqMap;

    TermsResponse() {
    }

    TermsResponse(int successfulShards, int failedShards, FieldTermsFreq[] fieldsTermsFreq,
                  long numDocs, long maxDoc, long numDeletedDocs) {
        super(successfulShards, failedShards);
        this.fieldsTermsFreq = fieldsTermsFreq;
        this.numDocs = numDocs;
        this.maxDoc = maxDoc;
        this.numDeletedDocs = numDeletedDocs;
    }

    /**
     * The total number of documents.
     */
    public long numDocs() {
        return this.numDocs;
    }

    /**
     * The total maximum number of documents (including deletions).
     */
    public long maxDoc() {
        return this.maxDoc;
    }

    /**
     * The number of deleted docs.
     */
    public long deletedDocs() {
        return this.numDeletedDocs;
    }

    /**
     * Iterates over the {@link FieldTermsFreq}.
     */
    @Override public Iterator<FieldTermsFreq> iterator() {
        return Iterators.forArray(fieldsTermsFreq);
    }

    /**
     * The {@link FieldTermsFreq} for the specified field name, <tt>null</tt> if
     * there is none.
     *
     * @param fieldName The field name to return the field terms freq for
     * @return The field terms freq
     */
    public FieldTermsFreq field(String fieldName) {
        return fieldsAsMap().get(fieldName);
    }

    /**
     * All the {@link FieldTermsFreq}.
     */
    public FieldTermsFreq[] fields() {
        return this.fieldsTermsFreq;
    }

    /**
     * The pair of field name to {@link FieldTermsFreq} as map for simpler usage.
     */
    public Map<String, FieldTermsFreq> fieldsAsMap() {
        if (fieldsTermsFreqMap != null) {
            return fieldsTermsFreqMap;
        }
        Map<String, FieldTermsFreq> fieldsTermsFreqMap = new HashMap<String, FieldTermsFreq>();
        for (FieldTermsFreq fieldTermsFreq : fieldsTermsFreq) {
            fieldsTermsFreqMap.put(fieldTermsFreq.fieldName(), fieldTermsFreq);
        }
        this.fieldsTermsFreqMap = fieldsTermsFreqMap;
        return fieldsTermsFreqMap;
    }

    @Override public void readFrom(DataInput in) throws IOException, ClassNotFoundException {
        super.readFrom(in);
        numDocs = in.readLong();
        maxDoc = in.readLong();
        numDeletedDocs = in.readLong();
        fieldsTermsFreq = new FieldTermsFreq[in.readInt()];
        for (int i = 0; i < fieldsTermsFreq.length; i++) {
            fieldsTermsFreq[i] = readFieldTermsFreq(in);
        }
    }

    @Override public void writeTo(DataOutput out) throws IOException {
        super.writeTo(out);
        out.writeLong(numDocs);
        out.writeLong(maxDoc);
        out.writeLong(numDeletedDocs);
        out.writeInt(fieldsTermsFreq.length);
        for (FieldTermsFreq fieldTermsFreq : fieldsTermsFreq) {
            fieldTermsFreq.writeTo(out);
        }
    }
}
