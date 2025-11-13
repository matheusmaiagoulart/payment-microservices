package com.matheus.payments.instant.Infra.MongoDB;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;

@Configuration
public class MongoDbConfig {

    // Remove _class field from MongoDB documents
    @Autowired
    public void setMapKeyConverter(MappingMongoConverter converter) {
        converter.setTypeMapper(new DefaultMongoTypeMapper(null));
    }
}
