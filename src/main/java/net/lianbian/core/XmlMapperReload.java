package net.lianbian.core;

import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.core.toolkit.GlobalConfigUtils;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.core.toolkit.SystemClock;
import org.apache.ibatis.binding.MapperRegistry;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.builder.xml.XMLMapperEntityResolver;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.executor.keygen.SelectKeyGenerator;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.parsing.XPathParser;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.security.MessageDigest;
import java.util.*;

public class XmlMapperReload implements Runnable {
    /**
     * 记录jar包存在的mapper
     */
    private static final Map<String, List<Resource>> JAR_MAPPER = new HashMap<>();
    private final SqlSessionFactory sqlSessionFactory;
    private final Resource[] mapperLocations;
    /**
     * xml文件目录
     */
    private Set<String> fileSet;

    private Long beforeTime = 0L;
    /**
     * 延迟加载时间
     */
    private int delaySeconds = 10;
    /**
     * 刷新间隔时间
     */
    private int sleepSeconds = 20;
    /**
     * 是否开启刷新mapper
     */
    private final boolean enabled;

    private Configuration configuration;

    /**
     * 已经重新加载过的mapper xml md5
     */
    private static final Map<String, String> reloadMapperXmlMaps = new HashMap<>();

    public XmlMapperReload(Resource[] mapperLocations, SqlSessionFactory sqlSessionFactory, int delaySeconds,
                           int sleepSeconds, boolean enabled) {
        this.mapperLocations = mapperLocations.clone();
        this.sqlSessionFactory = sqlSessionFactory;
        this.delaySeconds = delaySeconds;
        this.enabled = enabled;
        this.sleepSeconds = sleepSeconds;
        this.configuration = sqlSessionFactory.getConfiguration();
        this.run();
    }

    @Override
    public void run() {
        final GlobalConfig globalConfig = GlobalConfigUtils.getGlobalConfig(configuration);
        /*
         * 启动 XML 热加载
         */
        if (enabled) {
            beforeTime = SystemClock.now();
            final XmlMapperReload runnable = this;
            new Thread(() -> {
                if (fileSet == null) {
                    fileSet = new HashSet<>();
                    if (mapperLocations != null) {
                        for (Resource mapperLocation : mapperLocations) {
                            try {
                                if (ResourceUtils.isJarURL(mapperLocation.getURL())) {
                                    String key = new UrlResource(ResourceUtils.extractJarFileURL(mapperLocation.getURL()))
                                            .getFile().getPath();
                                    fileSet.add(key);
                                    if (JAR_MAPPER.get(key) != null) {
                                        JAR_MAPPER.get(key).add(mapperLocation);
                                    } else {
                                        List<Resource> resourcesList = new ArrayList<>();
                                        resourcesList.add(mapperLocation);
                                        JAR_MAPPER.put(key, resourcesList);
                                    }
                                } else {
                                    fileSet.add(mapperLocation.getFile().getPath());
                                }
                            } catch (IOException ioException) {
                                ioException.printStackTrace();
                            }
                        }
                    }
                }
                try {
                    Thread.sleep(delaySeconds * 1000);
                } catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                }
                do {
                    try {
                        for (String filePath : fileSet) {
                            File file = new File(filePath);
                            if (this.checkRefresh(filePath, file)) {
                                List<Resource> removeList = JAR_MAPPER.get(filePath);
                                if (removeList != null && !removeList.isEmpty()) {
                                    for (Resource resource : removeList) {
                                        runnable.refresh(resource);
                                    }
                                } else {
                                    runnable.refresh(new FileSystemResource(file));
                                }
                            }
                        }
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                    try {
                        Thread.sleep(sleepSeconds * 1000);
                    } catch (InterruptedException interruptedException) {
                        interruptedException.printStackTrace();
                    }

                } while (true);
            }, "mybatis-plus MapperRefresh").start();
        }
    }

    /**
     * 检查是否刷新
     *
     * @param file
     * @return
     */
    private boolean checkRefresh(String filePath, File file) {
        if(!file.isFile()) {
            return false;
        }

        if(file.lastModified() < this.beforeTime) {
            return false;
        }

        String fileOldMd5 = reloadMapperXmlMaps.getOrDefault(filePath, "");
        String fileNewMd5 = this.getMd5(file);
        if(fileOldMd5.equals(fileNewMd5)) {
            return false;
        }

        reloadMapperXmlMaps.put(filePath, fileNewMd5);
        return true;
    }

    /**
     * 获取文件值的md5
     * @param file
     * @return
     */
    private String getMd5(File file) {
        FileInputStream fileInputStream = null;
        try {
            MessageDigest MD5 = MessageDigest.getInstance("MD5");
            fileInputStream = new FileInputStream(file);
            byte[] buffer = new byte[8192];
            int length;
            while ((length = fileInputStream.read(buffer)) != -1) {
                MD5.update(buffer, 0, length);
            }
            return new String(MD5.digest());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 刷新mapper
     *
     * @throws Exception
     */
    @SuppressWarnings("rawtypes")
    private void refresh(Resource resource) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        this.configuration = sqlSessionFactory.getConfiguration();
        boolean isSupper = configuration.getClass().getSuperclass() == Configuration.class;
        try {
            Field loadedResourcesField = isSupper ? configuration.getClass().getSuperclass().getDeclaredField("loadedResources")
                    : configuration.getClass().getDeclaredField("loadedResources");
            loadedResourcesField.setAccessible(true);
            Set loadedResourcesSet = ((Set) loadedResourcesField.get(configuration));
            XPathParser xPathParser = new XPathParser(resource.getInputStream(), true, configuration.getVariables(),
                    new XMLMapperEntityResolver());
            XNode context = xPathParser.evalNode("/mapper");
            String namespace = context.getStringAttribute("namespace");
            Field field = MapperRegistry.class.getDeclaredField("knownMappers");
            field.setAccessible(true);
            Map mapConfig = (Map) field.get(configuration.getMapperRegistry());
            mapConfig.remove(Resources.classForName(namespace));
            loadedResourcesSet.remove(resource.toString());
            configuration.getCacheNames().remove(namespace);
            cleanParameterMap(context.evalNodes("/mapper/parameterMap"), namespace);
            cleanResultMap(context.evalNodes("/mapper/resultMap"), namespace);
            cleanKeyGenerators(context.evalNodes("insert|update"), namespace);
            cleanSqlElement(context.evalNodes("/mapper/sql"), namespace);
            XMLMapperBuilder xmlMapperBuilder = new XMLMapperBuilder(resource.getInputStream(),
                    sqlSessionFactory.getConfiguration(),
                    resource.toString(), sqlSessionFactory.getConfiguration().getSqlFragments());
            xmlMapperBuilder.parse();
            System.out.println("refresh: '" + resource + "', success!");
        } catch (IOException e) {
            System.out.println("Refresh IOException :" + e.getMessage());
        } finally {
            ErrorContext.instance().reset();
        }
    }

    /**
     * 清理parameterMap
     *
     * @param list
     * @param namespace
     */
    private void cleanParameterMap(List<XNode> list, String namespace) {
        for (XNode parameterMapNode : list) {
            String id = parameterMapNode.getStringAttribute("id");
            configuration.getParameterMaps().remove(namespace + StringPool.DOT + id);
        }
    }

    /**
     * 清理resultMap
     *
     * @param list
     * @param namespace
     */
    private void cleanResultMap(List<XNode> list, String namespace) {
        for (XNode resultMapNode : list) {
            String id = resultMapNode.getStringAttribute("id", resultMapNode.getValueBasedIdentifier());
            configuration.getResultMapNames().remove(id);
            configuration.getResultMapNames().remove(namespace + StringPool.DOT + id);
            clearResultMap(resultMapNode, namespace);
        }
    }

    private void clearResultMap(XNode xNode, String namespace) {
        for (XNode resultChild : xNode.getChildren()) {
            if ("association".equals(resultChild.getName()) || "collection".equals(resultChild.getName())
                    || "case".equals(resultChild.getName())) {
                if (resultChild.getStringAttribute("select") == null) {
                    configuration.getResultMapNames().remove(
                            resultChild.getStringAttribute("id", resultChild.getValueBasedIdentifier()));
                    configuration.getResultMapNames().remove(
                            namespace + StringPool.DOT + resultChild.getStringAttribute("id", resultChild.getValueBasedIdentifier()));
                    if (resultChild.getChildren() != null && !resultChild.getChildren().isEmpty()) {
                        clearResultMap(resultChild, namespace);
                    }
                }
            }
        }
    }

    /**
     * 清理selectKey
     *
     * @param list
     * @param namespace
     */
    private void cleanKeyGenerators(List<XNode> list, String namespace) {
        for (XNode context : list) {
            String id = context.getStringAttribute("id");
            configuration.getKeyGeneratorNames().remove(id + SelectKeyGenerator.SELECT_KEY_SUFFIX);
            configuration.getKeyGeneratorNames().remove(namespace + StringPool.DOT + id + SelectKeyGenerator.SELECT_KEY_SUFFIX);
        }
    }

    /**
     * 清理sql节点缓存
     *
     * @param list
     * @param namespace
     */
    private void cleanSqlElement(List<XNode> list, String namespace) {
        for (XNode context : list) {
            String id = context.getStringAttribute("id");
            configuration.getSqlFragments().remove(id);
            configuration.getSqlFragments().remove(namespace + StringPool.DOT + id);
        }
    }
}
