package uk.gov.gchq.palisade.service.data.s3;

import uk.gov.gchq.palisade.Generated;

public class S3Bucket {

    private final String bucketName;
    private final String bucketKey;

    public S3Bucket(final String bucketName, final String bucketKey){
        this.bucketName = bucketName;
        this.bucketKey = bucketKey;
    }

    @Generated
    public String getBucketName() {
        return bucketName;
    }

    @Generated
    public String getBucketKey(){
        return bucketKey;
    }
}
