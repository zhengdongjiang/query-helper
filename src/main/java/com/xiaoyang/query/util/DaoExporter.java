package com.xiaoyang.query.util;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.google.common.io.Files;
import com.mysema.codegen.CodeWriter;
import com.mysema.codegen.JavaWriter;
import com.mysema.codegen.model.ClassType;
import com.mysema.codegen.model.SimpleType;
import com.querydsl.core.types.Path;
import com.querydsl.sql.RelationalPathBase;

public class DaoExporter {

	private File targetFolder;

	private String packageName;

	private String implPackageName;

	private String scanPackageName;

	private String interfacePrefix = "Dao";

	private String implPrefix = "DaoImpl";

	private com.mysema.codegen.model.Type idaoType;

	private com.mysema.codegen.model.Type abstractDaoType;

	private Logger logger = LoggerFactory.getLogger(DaoExporter.class);

	public void serialize() {
		PackageScan packageScan = new PackageScan();
		if (StringUtils.isEmpty(scanPackageName)) {
			throw new RuntimeException("scanPackageName is not null");
		}
		List<Class<?>> classs = packageScan.scan(scanPackageName);
		if (classs != null &&  !classs.isEmpty()) {
			for (Class<?> _class : classs) {
				// 判断类是否是RelationalPathBase的子类
				if (_class.getSuperclass() == RelationalPathBase.class) {
					Type entityType = ((ParameterizedType) (_class.getGenericSuperclass())).getActualTypeArguments()[0];
					if (entityType instanceof Class) {
						Class<?> entityClass = (Class<?>) entityType;
						@SuppressWarnings("unchecked")
						List<? extends Path<?>> list = null;
						try {
							list = ((RelationalPathBase<?>) _class.getConstructor(String.class)
									.newInstance(entityClass.getSimpleName().toLowerCase())).getPrimaryKey()
											.getLocalColumns();
						} catch (Exception e) {
							throw new RuntimeException("domain get primaryKey error", e);
						}
						Class<?> primaryKey = null;
						for (Path<?> p : list) {
							primaryKey = p.getType();
						}
						ClassType warpClassType = new ClassType(_class);// 域模型
						ClassType entityClassType = new ClassType(entityClass);// entity
						ClassType primaryKeyClassType = new ClassType(primaryKey);// 主键
						// IDAO接口
						SimpleType _idaoType = new SimpleType(idaoType, warpClassType, entityClassType,
								primaryKeyClassType);
						// abstractDao
						SimpleType _abstrType = new SimpleType(abstractDaoType, warpClassType, entityClassType,
								primaryKeyClassType);
						// 生成的名称默认为entity名称加上dao 如User 名称为UserDao
						String fileSuffix = ".java";
						String interfaceName = entityClass.getSimpleName() + interfacePrefix;
						String interfaceImplName = entityClass.getSimpleName() + implPrefix;
						if (null == targetFolder) {
							throw new RuntimeException("targetFolder is  null");
						}
						if (StringUtils.isBlank(packageName)) {
							throw new RuntimeException("packageName is  null");
						}
						// 生成dao
						File daoTargetFile = new File(targetFolder,
								packageName.replace(".", "/") + "/" + interfaceName + fileSuffix);
						// 检测文件夹是否存在
						dirExist(new File(targetFolder, packageName.replace(".", "/")));
						StringWriter daoSw = new StringWriter();
						CodeWriter daoWriter = new JavaWriter(daoSw);
						try {
							daoWriter.packageDecl(packageName);
							daoWriter.imports(_class, entityClass, primaryKey, _idaoType.getJavaClass());
							daoWriter.beginInterface(new SimpleType(interfaceName), _idaoType);
							daoWriter.end();
							write(daoSw, daoTargetFile);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						// 生成dao实现类
						File daoImplTargetFile = new File(targetFolder,
								implPackageName.replace(".", "/") + "/" + interfaceImplName + fileSuffix);
						// 检测文件夹是否存在
						dirExist(new File(targetFolder, implPackageName.replace(".", "/")));
						StringWriter daoImplSw = new StringWriter();
						CodeWriter daoImplWriter = new JavaWriter(daoImplSw);
						try {
							daoImplWriter.packageDecl(implPackageName);
							daoImplWriter.imports(_class, entityClass, primaryKey, _abstrType.getJavaClass(),
									Repository.class);
							daoImplWriter.importClasses(packageName + "." + interfaceName);
							daoImplWriter.annotation(Repository.class);
							daoImplWriter.beginClass(new SimpleType(interfaceImplName), _abstrType,
									new SimpleType(interfaceName));
							daoImplWriter.end();
							write(daoImplSw, daoImplTargetFile);
						} catch (IOException e) {
							e.printStackTrace();
						}

					}
				}
			}
		}
	}

	/**
	 * 如果目标目录不存在,则创建
	 */
	private void dirExist(String path) {
		dirExist(new File(path));
	}

	private void dirExist(File file) {
		if (!file.exists()) {
			file.mkdirs();
		}
	}

	/**
	 * 生成java文件
	 * 
	 * @param sw
	 * @param targetFile
	 * @throws IOException
	 */
	private void write(StringWriter sw, File targetFile) throws IOException {
		byte[] bytes = sw.toString().getBytes("UTF-8");
		// && targetFile.length() == bytes.length
		if (targetFile.exists()) {
			logger.debug("{} exist,skip create", targetFile.getName());
		} else {
			Files.write(bytes, targetFile);
			logger.debug("Exported {} successfully", targetFile.getName());
		}
	}

	public File getTargetFolder() {
		return targetFolder;
	}

	public void setTargetFolder(File targetFolder) {
		this.targetFolder = targetFolder;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
		this.implPackageName = packageName + ".impl";
	}

	public String getScanPackageName() {
		return scanPackageName;
	}

	public void setScanPackageName(String scanPackageName) {
		this.scanPackageName = scanPackageName;
	}

	public String getInterfacePrefix() {
		return interfacePrefix;
	}

	public void setInterfacePrefix(String interfacePrefix) {
		this.interfacePrefix = interfacePrefix;
	}

	public String getImplPrefix() {
		return implPrefix;
	}

	public void setImplPrefix(String implPrefix) {
		this.implPrefix = implPrefix;
	}

	public com.mysema.codegen.model.Type getIdaoType() {
		return idaoType;
	}

	public void setIdaoType(com.mysema.codegen.model.Type idaoType) {
		this.idaoType = idaoType;
	}

	public com.mysema.codegen.model.Type getAbstractDaoType() {
		return abstractDaoType;
	}

	public void setAbstractDaoType(com.mysema.codegen.model.Type abstractDaoType) {
		this.abstractDaoType = abstractDaoType;
	}

	public Logger getLogger() {
		return logger;
	}

	public void setLogger(Logger logger) {
		this.logger = logger;
	}

}
