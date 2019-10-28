/*
 * Copyright 2019 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.gov.gchq.palisade.reader.request;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import uk.gov.gchq.palisade.RequestId;
import uk.gov.gchq.palisade.exception.ForbiddenException;
import uk.gov.gchq.palisade.service.Service;

/**
 * This class is the type of request for retrieving an object from the cache service. The parameter type on this class
 * is used to specify the type of the cache retrieval.
 *
 * @param <V> the type of object that is expected to be in the cache
 */
@JsonIgnoreProperties(value = {"originalRequestId"})
public class GetCacheRequest<V> extends CacheRequest {

    public GetCacheRequest() {
    }

    /**
     * {@inheritDoc}
     */
    public GetCacheRequest key(final String key) {
        super.key(key);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public GetCacheRequest service(final Class<? extends Service> service) {
        super.service(service);
        return this;
    }

    @Override
    public RequestId getOriginalRequestId() {
        throw new ForbiddenException("Should not call GetCacheRequest.getOriginalRequestId()");
    }

    @Override
    public void setOriginalRequestId(final RequestId originalRequestId) {
        throw new ForbiddenException("Should not call GetCacheRequest.setOriginalRequestId()");
    }

}
