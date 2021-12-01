package net.lianbian.config;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusProperties;
import net.lianbian.core.XmlMapperReload;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class XmlMapperReloadConfig {

    @Bean
    @Profile("dev")
    public XmlMapperReload xmlMapperReload(SqlSessionFactory sqlSessionFactory, MybatisPlusProperties mybatisPlusProperties) {
        return new XmlMapperReload(mybatisPlusProperties.resolveMapperLocations(), sqlSessionFactory, 2, true);
    }
}
